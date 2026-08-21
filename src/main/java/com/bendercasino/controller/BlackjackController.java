package com.bendercasino.controller;

import com.bendercasino.dto.*;
import com.bendercasino.service.BlackjackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/blackjack")
public class BlackjackController {

    private final BlackjackService blackjackService;

    public BlackjackController(BlackjackService blackjackService) {
        this.blackjackService = blackjackService;
    }

    @PostMapping("/start")
    public GameStateResponse start(@Valid @RequestBody StartGameRequest request) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    @PostMapping("/hit")
    public GameStateResponse hit(@Valid @RequestBody PlayerActionRequest request) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    @PostMapping("/stand")
    public GameStateResponse stand(@Valid @RequestBody PlayerActionRequest request) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    @GetMapping("/state/{playerId}")
    public GameStateResponse state(@PathVariable UUID playerId) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    @PostMapping("/joke")
    public JokeResponse joke(@Valid @RequestBody JokeRequest request) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }
}
