package com.bendercasino.service;

import com.bendercasino.dto.CreatePlayerRequest;
import com.bendercasino.exception.InvalidCredentialsException;
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

    public Player create(CreatePlayerRequest request) {
    if (playerRepository.findByUsername(request.email()).isPresent()) {
        throw new IllegalArgumentException("Email already taken: " + request.email());
    }
    Player player = new Player(
        request.name(),
        request.firstName(),
        request.lastName(),
        request.email(),
        passwordEncoder.encode(request.password())
    );
    return playerRepository.save(player);
}

    public boolean verifyCredentials(String username, String rawPassword) {
        return playerRepository.findByUsername(username)
            .map(player -> passwordEncoder.matches(rawPassword, player.getPasswordHash()))
            .orElse(false);
    }

    public Player login(String username, String rawPassword) {
        if (!verifyCredentials(username, rawPassword)) {
            throw new InvalidCredentialsException();
        }
        return playerRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new);
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
