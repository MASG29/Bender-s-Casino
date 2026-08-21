package com.bendercasino.repository;

import com.bendercasino.model.Player;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPlayerRepository {

    private final Map<UUID, Player> store = new ConcurrentHashMap<>();

    public Player save(Player player) {
        store.put(player.getId(), player);
        return player;
    }

    public Optional<Player> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
