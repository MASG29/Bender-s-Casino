package com.bendercasino.controller;

import com.bendercasino.dto.ActRequest;
import com.bendercasino.dto.StartGameRequest;
import com.bendercasino.exception.UnknownGameException;
import com.bendercasino.model.GameSession;
import com.bendercasino.service.GameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/games/{game}")
public class GamesController {

    private final Map<String, GameService> gameServices;

    public GamesController(Map<String, GameService> gameServices) {
        this.gameServices = gameServices;
    }

    @PostMapping("/start")
    public GameSession start(@PathVariable String game, @Valid @RequestBody StartGameRequest request) {
        return resolve(game).start(request.playerId(), request.bet());
    }

    @PostMapping("/{gameId}/act")
    public GameSession act(@PathVariable String game, @PathVariable UUID gameId, @Valid @RequestBody ActRequest request) {
        return resolve(game).act(gameId, request.action(), request.payload());
    }

    @GetMapping("/{gameId}/state")
    public GameSession state(@PathVariable String game, @PathVariable UUID gameId) {
        return resolve(game).state(gameId);
    }

    private GameService resolve(String game) {
        GameService service = gameServices.get(game);
        if (service == null) {
            throw new UnknownGameException(game);
        }
        return service;
    }
}
