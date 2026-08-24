package com.bendercasino.service;

import com.bendercasino.client.DeckClient;
import com.bendercasino.model.GameSession;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.InMemoryPlayerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BlackjackService {

    private final DeckClient deckClient;
    private final InMemoryPlayerRepository playerRepository;
    private final InMemoryGameSessionRepository sessionRepository;
    private final JokeService jokeService;

    public BlackjackService(DeckClient deckClient,
                            InMemoryPlayerRepository playerRepository,
                            InMemoryGameSessionRepository sessionRepository,
                            JokeService jokeService) {
        this.deckClient        = deckClient;
        this.playerRepository  = playerRepository;
        this.sessionRepository = sessionRepository;
        this.jokeService       = jokeService;
    }

    public GameSession start(UUID playerId, int bet) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    public GameSession hit(UUID playerId) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    public GameSession stand(UUID playerId) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    public GameSession getState(UUID playerId) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }
}
