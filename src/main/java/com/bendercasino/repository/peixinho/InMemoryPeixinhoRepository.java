package com.bendercasino.repository.peixinho;

import com.bendercasino.model.peixinho.PeixinhoSession;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPeixinhoRepository {

    private final Map<UUID, PeixinhoSession> sessions = new ConcurrentHashMap<>();

    public PeixinhoSession save(UUID playerId, PeixinhoSession session) {
        sessions.put(playerId, session);
        return session;
    }

    public Optional<PeixinhoSession> findByPlayerId(UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public void deleteByPlayerId(UUID playerId) {
        sessions.remove(playerId);
    }
}