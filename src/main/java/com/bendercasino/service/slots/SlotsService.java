package com.bendercasino.service.slots;

import com.bendercasino.exception.GameNotFoundException;
import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.model.slots.Slot;
import com.bendercasino.model.slots.SlotResult;
import com.bendercasino.model.slots.SlotsState;
import com.bendercasino.model.slots.Symbol;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import com.bendercasino.service.GameService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service("slots")
public class SlotsService implements GameService {

    private final PlayerRepository playerRepository;
    private final InMemoryGameSessionRepository sessionRepository;
    private final Slot slot;
    private final Random random;

    public SlotsService(PlayerRepository playerRepository,
                         InMemoryGameSessionRepository sessionRepository,
                         Random random) {
        this.playerRepository = playerRepository;
        this.sessionRepository = sessionRepository;
        this.slot = new Slot();
        this.random = random;
    }

    public GameSession spin(UUID playerId, int bet) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        if (bet <= 0) {
            throw new InvalidBetException(bet);
        }

        if (!player.canAfford(bet)) {
            throw new InsufficientBalanceException(player.getName(), player.getBalance(), bet);
        }

        player.debit(bet);

        SlotResult result = roll();
        double multiplier = result.getMultiplier();
        int payout = (int) Math.round(bet * multiplier);

        player.credit(payout);
            if (payout > 0) {
                player.registerWin();
            } else {
                player.registerLoss();
            }
            playerRepository.save(player);

        GameSession session = new GameSession(playerId, null, "slots", bet);
        session.setStatus(GameStatus.FINISHED);
        session.setState(new SlotsState(result.getSymbols(), result.getOutcome(), multiplier, payout));
        sessionRepository.save(session);

        return session;
    }

    private SlotResult roll() {
        List<Symbol> reel = slot.getMultipliers();
        Symbol[] symbols = new Symbol[3];
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = reel.get(random.nextInt(reel.size()));
        }
        return new SlotResult(symbols);
    }

    @Override
    public GameSession start(UUID playerId, int bet) {
        throw new UnsupportedOperationException(
                "Slots does not support start(playerId, bet); use spin(playerId, bet) instead");
    }

    @Override
    public GameSession act(UUID gameId, String action, Object payload) {
        throw new UnsupportedOperationException("Slots resolves in a single spin, no further actions");
    }

    @Override
    public GameSession state(UUID gameId) {
        return sessionRepository.findByGameId(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
