package com.bendercasino.service;

import com.bendercasino.exception.ForbiddenResetException;
import com.bendercasino.exception.InvalidCredentialsException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.Player;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
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

    public Player create(String name, String username, String firstName, String lastName, String email, String rawPassword) {
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
        if (playerRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already taken: " + email);
        }
        Player player = new Player(name, username, firstName, lastName, email, passwordEncoder.encode(rawPassword));
        return playerRepository.save(player);
    }

    private Optional<Player> findByIdentifier(String identifier) {
        return playerRepository.findByUsername(identifier)
            .or(() -> playerRepository.findByEmail(identifier));
    }

    public boolean verifyCredentials(String identifier, String rawPassword) {
        return findByIdentifier(identifier)
            .map(player -> passwordEncoder.matches(rawPassword, player.getPasswordHash()))
            .orElse(false);
    }

    public Player login(String identifier, String rawPassword) {
        if (!verifyCredentials(identifier, rawPassword)) {
            throw new InvalidCredentialsException();
        }
        return findByIdentifier(identifier).orElseThrow(InvalidCredentialsException::new);
    }

    public Player findById(UUID id) {
        return playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
    }

    /**
     * Reset do saldo e das estatísticas. Antes da persistência (L-B1) isto era inofensivo:
     * o estado morria com o processo. Agora que o saldo vive em H2, um POST anónimo a
     * /api/players/{id}/reset apagava a conta de outra pessoa — por isso o reset só é
     * permitido ao próprio jogador autenticado (o username da sessão tem de ser o dono
     * do id). Não há roles de admin; se um dia houver, acrescentar aqui a exceção.
     */
    public Player reset(UUID id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !findById(id).getUsername().equals(authentication.getName())) {
            throw new ForbiddenResetException();
        }
        Player player = findById(id);
        player.reset();
        sessionRepository.deleteByPlayerId(id);
        return playerRepository.save(player);
    }
}
