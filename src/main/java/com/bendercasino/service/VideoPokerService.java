package com.bendercasino.service;

import com.bendercasino.client.DeckClient;
import com.bendercasino.dto.DrawRequest;
import com.bendercasino.exception.GameNotFoundException;
import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.InvalidGameStateException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.Card;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.model.PokerHandCategory;
import com.bendercasino.model.PokerHandEvaluator;
import com.bendercasino.model.PokerPaytable;
import com.bendercasino.model.VideoPokerState;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service("videopoker")
public class VideoPokerService implements GameService {

    private final DeckClient deckClient;
    private final PlayerRepository playerRepository;
    private final InMemoryGameSessionRepository sessionRepository;
    private final JokeService jokeService;

    public VideoPokerService(DeckClient deckClient,
                             PlayerRepository playerRepository,
                             InMemoryGameSessionRepository sessionRepository,
                             JokeService jokeService) {
        this.deckClient = deckClient;
        this.playerRepository = playerRepository;
        this.sessionRepository = sessionRepository;
        this.jokeService = jokeService;
    }

    @Override
    public GameSession start(UUID playerId, int bet) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        if (bet <= 0) {
            throw new InvalidBetException(bet);
        }

        var existingSession = sessionRepository.findByPlayerId(playerId);
        if (existingSession.isPresent() && !existingSession.get().isFinished()) {
            throw new InvalidGameStateException("Player already has an unfinished game");
        }

        if (!player.canAfford(bet)) {
            throw new InsufficientBalanceException(player.getName(), player.getBalance(), bet);
        }

        var deck = deckClient.newShuffledDeck(1);
        List<Card> cards = deckClient.draw(deck.deckId(), 5);

        player.debit(bet);

        GameSession session = new GameSession(playerId, deck.deckId(), "videopoker", bet);
        session.setState(new VideoPokerState(cards));

        playerRepository.save(player);
        sessionRepository.save(session);
        return session;
    }

    @Override
    public GameSession act(UUID gameId, String action, Object payload) {
        GameSession session = sessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        if (!"draw".equals(action)) {
            throw new InvalidGameStateException("Unknown action: " + action);
        }
        return applyDraw(session, payload);
    }

    @Override
    public GameSession state(UUID gameId) {
        return sessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    private GameSession applyDraw(GameSession session, Object payload) {
        if (session.getStatus() != GameStatus.PLAYER_TURN) {
            throw new InvalidGameStateException("Cannot draw: game already finished");
        }

        List<Integer> held = heldIndexes(payload);
        validateHeld(held);

        VideoPokerState poker = pokerState(session);
        List<Card> current = poker.getCards();
        int replacementsNeeded = 5 - held.size();
        List<Card> replacements = replacementsNeeded == 0
                ? List.of()
                : deckClient.draw(session.getDeckId(), replacementsNeeded);

        List<Card> finalHand = new ArrayList<>();
        int nextReplacement = 0;
        Set<Integer> keep = new HashSet<>(held);
        for (int i = 0; i < 5; i++) {
            if (keep.contains(i)) {
                finalHand.add(current.get(i));
            } else {
                finalHand.add(replacements.get(nextReplacement));
                nextReplacement++;
            }
        }

        PokerHandCategory category = PokerHandEvaluator.evaluate(finalHand);
        int betAmount = session.getBet().amount();
        int payout = PokerPaytable.payout(category, betAmount);

        Player player = playerRepository.findById(session.getPlayerId()).orElseThrow();
        if (payout > 0) {
            player.credit(payout);
            player.registerWin();
        } else {
            player.registerLoss();
        }

        poker.setCards(finalHand);
        poker.setCategory(category);
        session.setBet(session.getBet().withPayout(payout));
        session.setStatus(GameStatus.FINISHED);

        playerRepository.save(player);
        sessionRepository.save(session);
        return session;
    }

    private void validateHeld(List<Integer> held) {
        Set<Integer> unique = new HashSet<>();
        for (int index : held) {
            if (index < 0 || index > 4) {
                throw new InvalidGameStateException("Held card index must be 0-4: " + index);
            }
            if (!unique.add(index)) {
                throw new InvalidGameStateException("Duplicate held card index: " + index);
            }
        }
    }

    private List<Integer> heldIndexes(Object payload) {
        if (payload == null) {
            return List.of();
        }
        if (payload instanceof DrawRequest request) {
            return request.held() == null ? List.of() : request.held();
        }
        if (payload instanceof List<?> list) {
            return toIndexes(list);
        }
        if (payload instanceof Map<?, ?> map && map.get("held") instanceof List<?> list) {
            return toIndexes(list);
        }
        throw new InvalidGameStateException("Draw payload must list held card indexes");
    }

    private List<Integer> toIndexes(List<?> list) {
        List<Integer> held = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Number number)) {
                throw new InvalidGameStateException("Held indexes must be numbers");
            }
            held.add(number.intValue());
        }
        return held;
    }

    private VideoPokerState pokerState(GameSession session) {
        return (VideoPokerState) session.getState();
    }
}
