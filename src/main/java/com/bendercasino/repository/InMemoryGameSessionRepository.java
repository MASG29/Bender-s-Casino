package com.bendercasino.repository;

import com.bendercasino.model.GameSession;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameSessionRepository {

    private final Map<UUID, GameSession> store = new ConcurrentHashMap<>();

    public GameSession save(GameSession session) {
        store.put(session.getPlayerId(), session);
        return session;
    }

    public Optional<GameSession> findByPlayerId(UUID playerId) {
        return Optional.ofNullable(store.get(playerId));
    }

    public Optional<GameSession> findByGameId(UUID gameId) {
        return store.values().stream()
                .filter(s -> s.getGameId().equals(gameId))
                .findFirst();
    }

    public void deleteByPlayerId(UUID playerId) {
        store.remove(playerId);
    }
}
