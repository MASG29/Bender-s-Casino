package com.bendercasino.service;

import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.Player;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final InMemoryGameSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public PlayerService(PlayerRepository playerRepository,
                         InMemoryGameSessionRepository sessionRepository,
                         PasswordEncoder passwordEncoder) {
        this.playerRepository  = playerRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    public Player create(String name, String username, String rawPassword) {
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
        Player player = new Player(name, username, passwordEncoder.encode(rawPassword));
        return playerRepository.save(player);
    }

    public boolean verifyCredentials(String username, String rawPassword) {
        return playerRepository.findByUsername(username)
            .map(player -> passwordEncoder.matches(rawPassword, player.getPasswordHash()))
            .orElse(false);
    }

    public Player findById(UUID id) {
        return playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
    }

    public Player reset(UUID id) {
        Player player = findById(id);
        player.reset();
        sessionRepository.deleteByPlayerId(id);
        return playerRepository.save(player);
    }
}
