package com.bendercasino.service;

import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.Colour;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.model.RouletteState;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service("roulette")
public class RouletteService implements GameService {

    private final PlayerRepository playerRepository;
    private final InMemoryGameSessionRepository sessionRepository;
    private final Random random;

    public RouletteService(PlayerRepository playerRepository,
                            InMemoryGameSessionRepository sessionRepository,
                            Random random) {
        this.playerRepository = playerRepository;
        this.sessionRepository = sessionRepository;
        this.random = random;
    }

    public GameSession spin(UUID playerId, int bet, Colour betType) {
    Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));

    if (bet <= 0) {
        throw new InvalidBetException(bet);
    }

    if (!player.canAfford(bet)) {
        throw new InsufficientBalanceException(player.getName(), player.getBalance(), bet);
    }

    player.debit(bet);

    int spunNumber = random.nextInt(37);
    Colour spunColour = RoulettePayout.colourOf(spunNumber);
    int payout = RoulettePayout.payout(bet, betType, spunNumber);
    boolean won = payout > 0;

    player.credit(payout);
    playerRepository.save(player);

    GameSession session = new GameSession(playerId, null, "roleta", bet);
    session.setStatus(GameStatus.FINISHED);
    session.setState(new RouletteState(spunNumber, spunColour, betType, won, payout));
    sessionRepository.save(session);

    return session;
}

    @Override
    public GameSession start(UUID playerId, int bet) {
        throw new UnsupportedOperationException(
                "Roulette does not support start(playerId, bet); use spin(playerId, bet, betColour) instead");
    }

    @Override
    public GameSession act(UUID gameId, String action, Object payload) {
        throw new UnsupportedOperationException("Roulette resolves in a single spin, no further actions");
    }

    @Override
    public GameSession state(UUID gameId) {
        return sessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new com.bendercasino.exception.GameNotFoundException(gameId));
    }
}
