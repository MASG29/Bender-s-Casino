package com.bendercasino.service;

import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.blackjack.Player;
import com.bendercasino.model.slots.*;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SlotsService {

    private Slot slot;
    private Integer betAmount;
    private PlayerRepository pr;
    private InMemoryGameSessionRepository sessionRepository;
    private JokeService jokeService;


    public SlotsService() {
        this.slot = new Slot();
    }

    public EndResult bet(UUID id, int amount) {
        Player player = pr.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
        if (!player.canAfford(amount)) {
            throw new InsufficientBalanceException(player.getName(), player.getBalance(), amount);
        }
        GameSession session = new GameSession(id, null, "slots", amount);
        sessionRepository.save(session);
        player.debit(amount);
        pr.save(player);
        SlotResult roll = roll();
        int payout = (int) (amount * roll.getMultiplier());

        EndResult er = new EndResult(roll.getMultiplier(), payout, roll.getSymbols(), roll.getOutcome());
        player.credit(payout);
        pr.save(player);
        return er;
    }

    public SlotResult roll() {

        Symbol[] symbols = new Symbol[3];

        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = slot.getMultipliers().get((int) Math.floor(Math.random() * (slot.getMultipliers().size())));
        }

        return new SlotResult(symbols);
    }

    @Autowired
    public void setJokeService(JokeService jokeService) {
        this.jokeService = jokeService;
    }

    @Autowired
    public void setSessionRepository(InMemoryGameSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Autowired
    public void setPr(PlayerRepository pr) {
        this.pr = pr;
    }

}
