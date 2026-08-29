package com.bendercasino.service;

import com.bendercasino.client.DeckClient;
import com.bendercasino.dto.AskResultResponse;
import com.bendercasino.dto.PeixinhoStateResponse;
import com.bendercasino.exception.GameNotFoundException;
import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidGameStateException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.Card;
import com.bendercasino.model.PeixinhoSession;
import com.bendercasino.model.Player;
import com.bendercasino.repository.PlayerRepository;
import com.bendercasino.repository.InMemoryPeixinhoRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PeixinhoService {

    private final DeckClient deckClient;
    private final InMemoryPeixinhoRepository sessionRepository;
    private final PlayerRepository playerRepository;
    private final PeixinhoBot bot;

    public PeixinhoService(DeckClient deckClient,
                           InMemoryPeixinhoRepository sessionRepository,
                           PlayerRepository playerRepository,
                           PeixinhoBot bot) {
        this.deckClient        = deckClient;
        this.sessionRepository = sessionRepository;
        this.playerRepository  = playerRepository;
        this.bot               = bot;
    }


    public PeixinhoStateResponse start(UUID playerId, int bet) {
        if (sessionRepository.findByPlayerId(playerId).isPresent()) {
            throw new InvalidGameStateException("Game is already started.");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
        if (!player.canAfford(bet)) {
            throw new InsufficientBalanceException(player.getName(), player.getBalance(), bet);
        }
        player.debit(bet);
        playerRepository.save(player);

        UUID botId = UUID.randomUUID();
        var deck = deckClient.newShuffledDeck(1);
        List<Card> allCards  = deckClient.draw(deck.deckId(), 14);
        List<Card> playerHand = new ArrayList<>(allCards.subList(0, 7));
        List<Card> botHand    = new ArrayList<>(allCards.subList(7, 14));
        List<Card> lagoa      = new ArrayList<>(deckClient.draw(deck.deckId(), 52 - 14));
        Map<UUID, List<Card>> hands = new HashMap<>();
        hands.put(playerId, playerHand);
        hands.put(botId, botHand);
        Map<UUID, Integer> bets = Map.of(playerId, bet, botId, 0);
        List<UUID> order = List.of(playerId, botId);
        PeixinhoSession session = new PeixinhoSession(order, hands, lagoa, bets);
        sessionRepository.save(playerId, session);
        checkAndLowerBooks(session, playerId);
        checkAndLowerBooks(session, botId);
        return toStateResponse(session, playerId);
    }

    public AskResultResponse ask(UUID playerId, UUID targetId, String cardValue) {
        PeixinhoSession session = sessionRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new GameNotFoundException(playerId));

        if (session.getStatus().equals("FINISHED")) {
            throw new InvalidGameStateException("The game has already ended.");
        }

        if (!session.currentPlayerId().equals(playerId)) {
            throw new InvalidGameStateException("Not your turn");
        }

        List<Card> playerHand = session.getHands().get(playerId);

        if (!PeixinhoRules.canAsk(playerHand, cardValue)) {
            throw new InvalidGameStateException("Need to haver the same card value.");
        }

        List<Card> targetHand   = session.getHands().get(targetId);
        List<Card> transferred  = PeixinhoRules.cardsOfValue(targetHand, cardValue);
        boolean gotCards        = !transferred.isEmpty();
        boolean formedBook      = false;
        boolean drewFromDeck    = false;
        Card drawnCard          = null;

        session.addHistory(new PeixinhoBot.AskHistoryEntry(playerId, targetId, cardValue));

        if (gotCards) {
            targetHand.removeAll(transferred);
            playerHand.addAll(transferred);
            formedBook = checkAndLowerBooks(session, playerId);
            session.setLastAction(playerId + " receive " + transferred.size() + " card(s) of " + cardValue);
        } else {

            if (!session.getDeck().isEmpty()) {
                drawnCard = session.getDeck().remove(0);
                playerHand.add(drawnCard);
                drewFromDeck = true;
                formedBook = checkAndLowerBooks(session, playerId);

                if (drawnCard.value().equals(cardValue)) {
                    session.setLastAction("Fish " + cardValue + " — play again!");
                } else {
                    session.setLastAction("Fish. Pass.");
                    session.nextTurn();
                    runBotTurn(session, playerId);
                }
            } else {
                session.setLastAction("Ho no is empty. Pass.");
                session.nextTurn();
                runBotTurn(session, playerId);
            }
        }

        if (PeixinhoRules.isGameOver(session.getBooks().size())) {
            session.setStatus("FINISHED");
            Map<UUID, Integer> bookCounts = new HashMap<>();
            session.getPlayerOrder().forEach(id ->
                    bookCounts.put(id, PeixinhoRules.countBooks(session.getBooks(), id)));
            UUID winnerId = PeixinhoRules.winner(bookCounts);
            if (winnerId.equals(playerId)) {
                Player winner = playerRepository.findById(playerId)
                        .orElseThrow(() -> new PlayerNotFoundException(playerId));
                int totalPot = session.getBets().values().stream().mapToInt(Integer::intValue).sum();
                winner.credit(totalPot);
                playerRepository.save(winner);
            }
        }

        sessionRepository.save(playerId, session);

        return new AskResultResponse(
                gotCards,
                transferred.stream().map(c -> new com.bendercasino.dto.CardDto(
                        c.code(), c.value(), c.suit(), c.image())).toList(),
                drewFromDeck,
                drawnCard != null ? new com.bendercasino.dto.CardDto(
                        drawnCard.code(), drawnCard.value(), drawnCard.suit(), drawnCard.image()) : null,
                formedBook,
                session.getLastAction(),
                toStateResponse(session, playerId)
        );
    }

    private void runBotTurn(PeixinhoSession session, UUID humanPlayerId) {
        UUID botId = session.getPlayerOrder().stream()
                .filter(id -> !id.equals(humanPlayerId))
                .findFirst().orElse(null);

        if (botId == null || !session.currentPlayerId().equals(botId)) return;

        boolean botTurn = true;
        while (botTurn && !PeixinhoRules.isGameOver(session.getBooks().size())) {
            List<Card> botHand = session.getHands().get(botId);
            if (botHand.isEmpty()) break;

            PeixinhoBot.BotDecision decision = bot.decide(
                    botHand,
                    List.of(humanPlayerId),
                    session.getBooks(),
                    session.getHistory()
            );

            if ("__FISH__".equals(decision.cardValue())) {
                if (!session.getDeck().isEmpty()) {
                    Card drawn = session.getDeck().remove(0);
                    botHand.add(drawn);
                    checkAndLowerBooks(session, botId);
                }
                session.nextTurn();
                botTurn = false;
                continue;
            }

            List<Card> targetHand  = session.getHands().get(decision.targetId());
            List<Card> transferred = PeixinhoRules.cardsOfValue(targetHand, decision.cardValue());

            session.addHistory(new PeixinhoBot.AskHistoryEntry(
                    botId, decision.targetId(), decision.cardValue()));

            if (!transferred.isEmpty()) {
                targetHand.removeAll(transferred);
                botHand.addAll(transferred);
                checkAndLowerBooks(session, botId);

            } else {

                if (!session.getDeck().isEmpty()) {
                    Card drawn = session.getDeck().remove(0);
                    botHand.add(drawn);
                    checkAndLowerBooks(session, botId);
                    if (!drawn.value().equals(decision.cardValue())) {
                        session.nextTurn();
                        botTurn = false;
                    }
                } else {
                    session.nextTurn();
                    botTurn = false;
                }
            }
        }
    }

    private boolean checkAndLowerBooks(PeixinhoSession session, UUID playerId) {
        List<Card> hand = session.getHands().get(playerId);
        boolean formed  = false;

        List<String> values = hand.stream().map(Card::value).distinct().toList();
        for (String value : values) {
            if (PeixinhoRules.isBook(hand, value)) {
                hand.removeIf(c -> c.value().equals(value));
                session.addBook(new com.bendercasino.dto.BookDto(playerId, value));
                formed = true;
            }
        }
        return formed;
    }

    public PeixinhoStateResponse getState(UUID playerId) {
        PeixinhoSession session = sessionRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new GameNotFoundException(playerId));
        return toStateResponse(session, playerId);
    }

    private PeixinhoStateResponse toStateResponse(PeixinhoSession session, UUID playerId) {
        Map<UUID, Integer> opponentSizes = new HashMap<>();
        session.getHands().forEach((id, hand) -> {
            if (!id.equals(playerId)) opponentSizes.put(id, hand.size());
        });

        List<com.bendercasino.dto.CardDto> playerHand = session.getHands()
                .get(playerId).stream()
                .map(c -> new com.bendercasino.dto.CardDto(
                        c.code(), c.value(), c.suit(), c.image()))
                .toList();

        return new PeixinhoStateResponse(
                session.getGameId(),
                session.currentPlayerId(),
                playerHand,
                opponentSizes,
                session.getDeck().size(),
                session.getBooks(),
                session.getStatus(),
                session.getLastAction(),
                session.currentPlayerId().equals(playerId)
        );
    }
}
