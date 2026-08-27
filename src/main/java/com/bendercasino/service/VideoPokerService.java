package com.bendercasino.service;

import com.bendercasino.client.DeckClient;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.springframework.stereotype.Service;

@Service("Poker")
public class VideoPokerService {

    private final DeckClient deckClient;
    private final PlayerRepository playerRepository;
    private final InMemoryGameSessionRepository sessionRepository;
    private final JokeService jokeService;

    public VideoPokerService(DeckClient deckClient,
                            PlayerRepository playerRepository,
                            InMemoryGameSessionRepository sessionRepository,
                            JokeService jokeService) {
        this.deckClient = deckClient;
        this.playerRepository = playerRepository;
        this.sessionRepository = sessionRepository;
        this.jokeService = jokeService;
    }


}
