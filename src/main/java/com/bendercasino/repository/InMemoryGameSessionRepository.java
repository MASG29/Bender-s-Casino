package com.bendercasino.repository;

import com.bendercasino.model.GameSession;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameSessionRepository {

    private final Map<String, GameSession> store = new ConcurrentHashMap<>();

    public GameSession save(GameSession session) {
        store.put(key(session.getPlayerId(), session.getGame()), session);
        return session;
    }

    public Optional<GameSession> findByPlayerIdAndGame(UUID playerId, String game) {
        return Optional.ofNullable(store.get(key(playerId, game)));
    }

    public Optional<GameSession> findByGameId(UUID gameId) {
        return store.values().stream()
                .filter(s -> s.getGameId().equals(gameId))
                .findFirst();
    }

    public void deleteByPlayerIdAndGame(UUID playerId, String game) {
        store.remove(key(playerId, game));
    }

    public void deleteByPlayerId(UUID playerId) {
        store.keySet().removeIf(k -> k.startsWith(playerId + ":"));
    }

    private String key(UUID playerId, String game) {
        return playerId + ":" + game;
    }
}
