package com.bendercasino.service;

import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.Player;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.InMemoryPlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerServiceTest {

    private InMemoryPlayerRepository playerRepository;
    private InMemoryGameSessionRepository sessionRepository;
    private PlayerService service;

    @BeforeEach
    void setUp() {
        playerRepository = new InMemoryPlayerRepository();
        sessionRepository = new InMemoryGameSessionRepository();
        service = new PlayerService(playerRepository, sessionRepository);
    }

    @Test
    @DisplayName("create builds and persists a player with given name and default balance 1000")
    void create_buildsAndPersistsPlayer() {
        Player created = service.create("Fry");

        assertThat(created.getName()).isEqualTo("Fry");
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
        assertThat(fetched.get().getBalance()).isEqualTo(1000);
    }

    @Test
    @DisplayName("findById returns the stored player when it exists")
    void findById_existingPlayer_returnsPlayer() {
        Player saved = playerRepository.save(new Player("Bender"));

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
        Player player = playerRepository.save(new Player("Leela"));
        UUID id = player.getId();

        // mutate the player into a "dirty" state
        player.debit(400);              // balance -> 600
        player.registerWin();           // consecutiveWins=1, totalWins=1
        player.registerLoss();          // consecutiveLosses=1, totalLosses=1

        Player resetPlayer = service.reset(id);

        // verify returned player
        assertThat(resetPlayer.getBalance()).isEqualTo(1000);
        assertThat(resetPlayer.getConsecutiveWins()).isZero();
        assertThat(resetPlayer.getConsecutiveLosses()).isZero();
        assertThat(resetPlayer.getConsecutiveBlackjacks()).isZero();
        assertThat(resetPlayer.getTotalWins()).isZero();
        assertThat(resetPlayer.getTotalLosses()).isZero();
        assertThat(resetPlayer.getTotalPushes()).isZero();
        assertThat(resetPlayer.getTotalBlackjacks()).isZero();

        // verify persisted state by re-fetching
        var fetched = playerRepository.findById(id);
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getBalance()).isEqualTo(1000);
        assertThat(fetched.get().getConsecutiveWins()).isZero();
        assertThat(fetched.get().getConsecutiveLosses()).isZero();
        assertThat(fetched.get().getConsecutiveBlackjacks()).isZero();
        assertThat(fetched.get().getTotalWins()).isZero();
        assertThat(fetched.get().getTotalLosses()).isZero();
        assertThat(fetched.get().getTotalPushes()).isZero();
        assertThat(fetched.get().getTotalBlackjacks()).isZero();
    }

    @Test
    @DisplayName("reset discards active game session if one exists")
    void reset_withActiveSession_deletesSession() {
        Player player = playerRepository.save(new Player("Zoidberg"));
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
        Player player = playerRepository.save(new Player("Hermes"));
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