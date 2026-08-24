package com.bendercasino.service;

import com.bendercasino.exception.PlayerNotFoundException;
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
        Player player = new Player(name);
        return playerRepository.save(player);
    }

    public Player findById(UUID id) {
        // TODO
        return playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
    }

    public Player reset(UUID id) {
        // TODO
        Player player = findById(id);
        player.reset();
        sessionRepository.deleteByPlayerId(id);
        return playerRepository.save(player);
    }
}
