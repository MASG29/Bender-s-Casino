package com.bendercasino.service;

import com.bendercasino.client.DeckClient;
import com.bendercasino.exception.GameNotFoundException;
import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.InvalidGameStateException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.BlackjackState;
import com.bendercasino.model.Card;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Outcome;
import com.bendercasino.model.Player;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service("blackjack")
public class BlackjackService implements GameService {

    private final DeckClient deckClient;
    private final PlayerRepository playerRepository;
    private final InMemoryGameSessionRepository sessionRepository;
    private final JokeService jokeService;

    public BlackjackService(DeckClient deckClient,
                            PlayerRepository playerRepository,
                            InMemoryGameSessionRepository sessionRepository,
                            JokeService jokeService) {
        this.deckClient = deckClient;
        this.playerRepository = playerRepository;
        this.sessionRepository = sessionRepository;
        this.jokeService = jokeService;
    }

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

        var deck = deckClient.newShuffledDeck(6);
        List<Card> cards = deckClient.draw(deck.deckId(), 4);

        player.debit(bet);

        GameSession session = new GameSession(playerId, deck.deckId(), "blackjack", bet);
        session.setState(new BlackjackState());
        state(session).getPlayerHand().add(cards.get(0));
        state(session).getPlayerHand().add(cards.get(1));
        state(session).getDealerHand().add(cards.get(2));
        state(session).getDealerHand().add(cards.get(3));

        boolean playerBlackjack = state(session).getPlayerHand().isBlackjack();
        boolean dealerBlackjack = state(session).getDealerHand().isBlackjack();

        if (playerBlackjack || dealerBlackjack) {
            resolveBlackjack(player, session, playerBlackjack, dealerBlackjack);
        }

        playerRepository.save(player);
        sessionRepository.save(session);
        return session;
    }

    public GameSession hit(UUID playerId) {
        GameSession session = sessionRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new GameNotFoundException(playerId));
        return applyHit(session);
    }

    private GameSession applyHit(GameSession session) {
        if (session.getStatus() != GameStatus.PLAYER_TURN) {
            throw new InvalidGameStateException("Cannot hit: game not in player turn");
        }

        List<Card> drawn = deckClient.draw(session.getDeckId(), 1);
        state(session).getPlayerHand().add(drawn.get(0));

        Player player = playerRepository.findById(session.getPlayerId()).orElseThrow();
        if (state(session).getPlayerHand().isBusted()) {
            resolvePlayerBust(player, session);
        }

        playerRepository.save(player);
        sessionRepository.save(session);
        return session;
    }

    public GameSession stand(UUID playerId) {
        GameSession session = sessionRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new GameNotFoundException(playerId));
        return applyStand(session);
    }

    private GameSession applyStand(GameSession session) {
        if (session.getStatus() != GameStatus.PLAYER_TURN) {
            throw new InvalidGameStateException("Cannot stand: game not in player turn");
        }

        while (state(session).getDealerHand().value() < 17) {
            List<Card> drawn = deckClient.draw(session.getDeckId(), 1);
            state(session).getDealerHand().add(drawn.get(0));
        }

        Player player = playerRepository.findById(session.getPlayerId()).orElseThrow();
        resolveStand(player, session);

        playerRepository.save(player);
        sessionRepository.save(session);
        return session;
    }

    public GameSession getState(UUID playerId) {
        return sessionRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new GameNotFoundException(playerId));
    }

    @Override
    public GameSession act(UUID gameId, String action, Object payload) {
        GameSession session = sessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        return switch (action) {
            case "hit" -> applyHit(session);
            case "stand" -> applyStand(session);
            default -> throw new InvalidGameStateException("Unknown action: " + action);
        };
    }

    @Override
    public GameSession state(UUID gameId) {
        return sessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    private BlackjackState state(GameSession session) {
        return (BlackjackState) session.getState();
    }

    private void resolveBlackjack(Player player, GameSession session, boolean playerBlackjack, boolean dealerBlackjack) {
        session.setStatus(GameStatus.FINISHED);
        int betAmount = session.getBet().amount();

        if (playerBlackjack && dealerBlackjack) {
            state(session).setOutcome(Outcome.PUSH);
            session.setBet(session.getBet().withPayout(betAmount));
            player.credit(betAmount);
            player.registerPush();
        } else if (playerBlackjack) {
            state(session).setOutcome(Outcome.PLAYER_BLACKJACK);
            int payout = betAmount * 5 / 2;
            session.setBet(session.getBet().withPayout(payout));
            player.credit(payout);
            player.registerBlackjack();
        } else {
            state(session).setOutcome(Outcome.DEALER_WIN);
            session.setBet(session.getBet().withPayout(0));
            player.registerLoss();
        }
    }

    private void resolvePlayerBust(Player player, GameSession session) {
        session.setStatus(GameStatus.FINISHED);
        state(session).setOutcome(Outcome.PLAYER_BUST);
        session.setBet(session.getBet().withPayout(0));
        player.registerLoss();
    }

    private void resolveStand(Player player, GameSession session) {
        session.setStatus(GameStatus.FINISHED);
        int betAmount = session.getBet().amount();
        int playerValue = state(session).getPlayerHand().value();
        int dealerValue = state(session).getDealerHand().value();

        if (state(session).getDealerHand().isBusted()) {
            state(session).setOutcome(Outcome.DEALER_BUST);
            int payout = betAmount * 2;
            session.setBet(session.getBet().withPayout(payout));
            player.credit(payout);
            player.registerWin();
        } else if (playerValue > dealerValue) {
            state(session).setOutcome(Outcome.PLAYER_WIN);
            int payout = betAmount * 2;
            session.setBet(session.getBet().withPayout(payout));
            player.credit(payout);
            player.registerWin();
        } else if (dealerValue > playerValue) {
            state(session).setOutcome(Outcome.DEALER_WIN);
            session.setBet(session.getBet().withPayout(0));
            player.registerLoss();
        } else {
            state(session).setOutcome(Outcome.PUSH);
            int payout = betAmount;
            session.setBet(session.getBet().withPayout(payout));
            player.credit(payout);
            player.registerPush();
        }
    }
}
