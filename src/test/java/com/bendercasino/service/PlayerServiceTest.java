package com.bendercasino.service;

import com.bendercasino.exception.ForbiddenResetException;
import com.bendercasino.exception.InvalidCredentialsException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.blackjack.Player;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

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
        when(playerRepository.findByEmail(any(String.class))).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return store.values().stream()
                .filter(p -> email.equals(p.getEmail()))
                .findFirst();
        });
        sessionRepository = new InMemoryGameSessionRepository();
        service = new PlayerService(playerRepository, sessionRepository, encoder);
    }

    @Test
    @DisplayName("create builds and persists a player with hashed password and default balance 1000")
    void create_buildsAndPersistsPlayer() {
        Player created = service.create("Fry", "fry", "Philip", "Fry", "fry@example.com", "pass1234");

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
        service.create("Fry", "fry", "Philip", "Fry", "fry@example.com", "pass1234");

        assertThatThrownBy(() -> service.create("Other Fry", "fry", "Philip", "Fry", "other-fry@example.com", "other-pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("create rejects a duplicate email")
    void create_duplicateEmail_throws() {
        service.create("Fry", "fry", "Philip", "Fry", "fry@example.com", "pass1234");

        assertThatThrownBy(() -> service.create("Other Fry", "otherfry", "Philip", "Fry", "fry@example.com", "other-pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("verifyCredentials accepts the right password and rejects wrong or unknown user")
    void verifyCredentials() {
        service.create("Leela", "leela", "Turanga", "Leela", "leela@example.com", "turanga");

        assertThat(service.verifyCredentials("leela", "turanga")).isTrue();
        assertThat(service.verifyCredentials("leela", "wrong")).isFalse();
        assertThat(service.verifyCredentials("nobody", "whatever")).isFalse();
    }

    @Test
    @DisplayName("login returns the player for valid credentials")
    void login_validCredentials_returnsPlayer() {
        Player created = service.create("Leela", "leela", "Turanga", "Leela", "leela@example.com", "turanga");

        Player logged = service.login("leela", "turanga");

        assertThat(logged.getId()).isEqualTo(created.getId());
        assertThat(logged.getUsername()).isEqualTo("leela");
    }

    @Test
    @DisplayName("login accepts the player's email as identifier")
    void login_byEmail_returnsPlayer() {
        Player created = service.create("Leela", "leela", "Turanga", "Leela", "leela@example.com", "turanga");

        Player logged = service.login("leela@example.com", "turanga");

        assertThat(logged.getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("login throws InvalidCredentialsException for wrong password or unknown user")
    void login_badCredentials_throws() {
        service.create("Leela", "leela", "Turanga", "Leela", "leela@example.com", "turanga");

        assertThatThrownBy(() -> service.login("leela", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> service.login("nobody", "whatever"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("findById returns the stored player when it exists")
    void findById_existingPlayer_returnsPlayer() {
        Player saved = playerRepository.save(new Player("Bender", "bender", "Bender", "Rodriguez", "bender@example.com", "hash"));

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
    @DisplayName("reset restores balance to 1000 and all counters to 0 when done by the owner")
    void reset_mutatedPlayer_restoresDefaults() {
        Player player = playerRepository.save(new Player("Amy", "amy", "Amy", "Wong", "amy@example.com", "hash"));
        UUID id = player.getId();

        player.debit(400);
        player.registerWin();
        player.registerBlackjack();

        Player resetPlayer = service.reset(id, authFor("amy"));

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
        Player player = playerRepository.save(new Player("Zoidberg", "zoidberg", "John", "Zoidberg", "zoidberg@example.com", "hash"));
        UUID id = player.getId();

        sessionRepository.save(new GameSession(id, "deck-1", "blackjack", 100));
        assertThat(sessionRepository.findByPlayerId(id)).isPresent();

        service.reset(id, authFor("zoidberg"));

        assertThat(sessionRepository.findByPlayerId(id)).isEmpty();
    }

    @Test
    @DisplayName("reset completes normally when no active session exists")
    void reset_noActiveSession_completesNormally() {
        Player player = playerRepository.save(new Player("Hermes", "hermes", "Hermes", "Conrad", "hermes@example.com", "hash"));
        UUID id = player.getId();

        Player resetPlayer = service.reset(id, authFor("hermes"));

        assertThat(resetPlayer.getBalance()).isEqualTo(1000);
        assertThat(sessionRepository.findByPlayerId(id)).isEmpty();
    }

    @Test
    @DisplayName("reset throws PlayerNotFoundException for unknown UUID")
    void reset_unknownId_throwsPlayerNotFoundException() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.reset(unknown, authFor("amy")))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    @DisplayName("reset throws ForbiddenResetException when the authenticated player is not the owner")
    void reset_byAnotherPlayer_throwsForbiddenResetException() {
        Player player = playerRepository.save(new Player("Amy", "amy", "Amy", "Wong", "amy@example.com", "hash"));

        player.debit(400);
        player.registerWin();

        assertThatThrownBy(() -> service.reset(player.getId(), authFor("not-amy")))
                .isInstanceOf(ForbiddenResetException.class);

        var fetched = playerRepository.findById(player.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getBalance()).isEqualTo(600);
        assertThat(fetched.get().getTotalWins()).isEqualTo(1);
    }

    @Test
    @DisplayName("reset throws ForbiddenResetException when there is no authentication at all")
    void reset_withoutAuthentication_throwsForbiddenResetException() {
        Player player = playerRepository.save(new Player("Amy", "amy", "Amy", "Wong", "amy@example.com", "hash"));

        assertThatThrownBy(() -> service.reset(player.getId(), null))
                .isInstanceOf(ForbiddenResetException.class);
    }

    private Authentication authFor(String username) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }
}
