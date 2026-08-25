package com.bendercasino.service;

import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.Player;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerServiceTest {

    private PlayerRepository playerRepository;
    private InMemoryGameSessionRepository sessionRepository;
    private PlayerService service;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        playerRepository = mock(PlayerRepository.class);
        Map<UUID, Player> store = new HashMap<>();
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            store.put(p.getId(), p);
            return p;
        });
        when(playerRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return Optional.ofNullable(store.get(id));
        });
        when(playerRepository.findByUsername(any(String.class))).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            return store.values().stream()
                .filter(p -> username.equals(p.getUsername()))
                .findFirst();
        });
        sessionRepository = new InMemoryGameSessionRepository();
        service = new PlayerService(playerRepository, sessionRepository, encoder);
    }

    @Test
    @DisplayName("create builds and persists a player with hashed password and default balance 1000")
    void create_buildsAndPersistsPlayer() {
        Player created = service.create("Fry", "fry", "pass1234");

        assertThat(created.getName()).isEqualTo("Fry");
        assertThat(created.getUsername()).isEqualTo("fry");
        assertThat(created.getPasswordHash()).isNotEqualTo("pass1234");
        assertThat(encoder.matches("pass1234", created.getPasswordHash())).isTrue();
        assertThat(created.getBalance()).isEqualTo(1000);
        assertThat(created.getConsecutiveWins()).isZero();
        assertThat(created.getConsecutiveLosses()).isZero();
        assertThat(created.getConsecutiveBlackjacks()).isZero();
        assertThat(created.getTotalWins()).isZero();
        assertThat(created.getTotalLosses()).isZero();
        assertThat(created.getTotalPushes()).isZero();
        assertThat(created.getTotalBlackjacks()).isZero();

        var fetched = playerRepository.findById(created.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getId()).isEqualTo(created.getId());
        assertThat(fetched.get().getName()).isEqualTo("Fry");
        assertThat(fetched.get().getUsername()).isEqualTo("fry");
        assertThat(fetched.get().getPasswordHash()).isNotEqualTo("pass1234");
        assertThat(fetched.get().getBalance()).isEqualTo(1000);
    }

    @Test
    @DisplayName("create rejects a duplicate username")
    void create_duplicateUsername_throws() {
        service.create("Fry", "fry", "pass1234");

        assertThatThrownBy(() -> service.create("Other Fry", "fry", "other-pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("verifyCredentials accepts the right password and rejects wrong or unknown user")
    void verifyCredentials() {
        service.create("Leela", "leela", "turanga");

        assertThat(service.verifyCredentials("leela", "turanga")).isTrue();
        assertThat(service.verifyCredentials("leela", "wrong")).isFalse();
        assertThat(service.verifyCredentials("nobody", "whatever")).isFalse();
    }

    @Test
    @DisplayName("findById returns the stored player when it exists")
    void findById_existingPlayer_returnsPlayer() {
        Player saved = playerRepository.save(new Player("Bender", "bender", "hash"));

        Player found = service.findById(saved.getId());

        assertThat(found).isSameAs(saved);
        assertThat(found.getName()).isEqualTo("Bender");
        assertThat(found.getBalance()).isEqualTo(1000);
    }

    @Test
    @DisplayName("findById throws PlayerNotFoundException for unknown UUID")
    void findById_unknownId_throwsPlayerNotFoundException() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.findById(unknown))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    @DisplayName("reset restores balance to 1000 and all counters to 0")
    void reset_mutatedPlayer_restoresDefaults() {
        Player player = playerRepository.save(new Player("Amy", "amy", "hash"));
        UUID id = player.getId();

        player.debit(400);
        player.registerWin();
        player.registerBlackjack();

        Player resetPlayer = service.reset(id);

        assertThat(resetPlayer.getBalance()).isEqualTo(1000);
        assertThat(resetPlayer.getConsecutiveWins()).isZero();
        assertThat(resetPlayer.getConsecutiveBlackjacks()).isZero();

        var fetched = playerRepository.findById(id);
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getTotalWins()).isZero();
        assertThat(fetched.get().getTotalLosses()).isZero();
        assertThat(fetched.get().getTotalPushes()).isZero();
        assertThat(fetched.get().getTotalBlackjacks()).isZero();
    }

    @Test
    @DisplayName("reset discards active game session if one exists")
    void reset_withActiveSession_deletesSession() {
        Player player = playerRepository.save(new Player("Zoidberg", "zoidberg", "hash"));
        UUID id = player.getId();

        // create an active game session
        sessionRepository.save(new GameSession(id, "deck-1", "blackjack", 100));
        assertThat(sessionRepository.findByPlayerId(id)).isPresent();

        service.reset(id);

        assertThat(sessionRepository.findByPlayerId(id)).isEmpty();
    }

    @Test
    @DisplayName("reset completes normally when no active session exists")
    void reset_noActiveSession_completesNormally() {
        Player player = playerRepository.save(new Player("Hermes", "hermes", "hash"));
        UUID id = player.getId();

        // no session created

        Player resetPlayer = service.reset(id);

        assertThat(resetPlayer.getBalance()).isEqualTo(1000);
        assertThat(sessionRepository.findByPlayerId(id)).isEmpty();
    }

    @Test
    @DisplayName("reset throws PlayerNotFoundException for unknown UUID")
    void reset_unknownId_throwsPlayerNotFoundException() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.reset(unknown))
                .isInstanceOf(PlayerNotFoundException.class);
    }
}
