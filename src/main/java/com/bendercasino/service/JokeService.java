package com.bendercasino.service;

import com.bendercasino.model.JokeTrigger;
import com.bendercasino.model.Outcome;
import com.bendercasino.model.Player;
import org.springframework.stereotype.Service;

@Service
public class JokeService {

    public JokeService(/* injectar o catálogo de piadas do bender-jokes.json */) {
        // TODO
    }

    public String jokeFor(Player player, Outcome outcome) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    public String jokeFor(Player player, JokeTrigger trigger) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }
}
