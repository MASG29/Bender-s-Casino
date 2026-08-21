package com.bendercasino.service;

import com.bendercasino.model.Player;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.InMemoryPlayerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlayerService {

    private final InMemoryPlayerRepository playerRepository;
    private final InMemoryGameSessionRepository sessionRepository;

    public PlayerService(InMemoryPlayerRepository playerRepository,
                         InMemoryGameSessionRepository sessionRepository) {
        this.playerRepository  = playerRepository;
        this.sessionRepository = sessionRepository;
    }

    public Player create(String name) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    public Player findById(UUID id) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    public Player reset(UUID id) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }
}
