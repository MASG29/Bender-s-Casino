package com.bendercasino.repository;

import com.bendercasino.model.GameSession;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryGameSessionRepositoryTest {

    private final InMemoryGameSessionRepository repository = new InMemoryGameSessionRepository();

    @Test
    void sessionsForDifferentGamesOfTheSamePlayerDoNotOverwriteEachOther() {
        UUID playerId = UUID.randomUUID();
        GameSession slotsSession = new GameSession(playerId, null, "slots", 10);
        GameSession blackjackSession = new GameSession(playerId, "deck-1", "blackjack", 20);

        repository.save(slotsSession);
        repository.save(blackjackSession);

        assertThat(repository.findByPlayerIdAndGame(playerId, "slots")).contains(slotsSession);
        assertThat(repository.findByPlayerIdAndGame(playerId, "blackjack")).contains(blackjackSession);
    }

    @Test
    void findByPlayerIdAndGame_missing_returnsEmpty() {
        UUID playerId = UUID.randomUUID();

        assertThat(repository.findByPlayerIdAndGame(playerId, "blackjack")).isEmpty();
    }

    @Test
    void deleteByPlayerId_removesSessionsAcrossAllGames() {
        UUID playerId = UUID.randomUUID();
        repository.save(new GameSession(playerId, null, "slots", 10));
        repository.save(new GameSession(playerId, "deck-1", "blackjack", 20));

        repository.deleteByPlayerId(playerId);

        assertThat(repository.findByPlayerIdAndGame(playerId, "slots")).isEmpty();
        assertThat(repository.findByPlayerIdAndGame(playerId, "blackjack")).isEmpty();
    }

    @Test
    void deleteByPlayerIdAndGame_onlyRemovesThatGamesSession() {
        UUID playerId = UUID.randomUUID();
        repository.save(new GameSession(playerId, null, "slots", 10));
        repository.save(new GameSession(playerId, "deck-1", "blackjack", 20));

        repository.deleteByPlayerIdAndGame(playerId, "slots");

        assertThat(repository.findByPlayerIdAndGame(playerId, "slots")).isEmpty();
        assertThat(repository.findByPlayerIdAndGame(playerId, "blackjack")).isPresent();
    }
}
