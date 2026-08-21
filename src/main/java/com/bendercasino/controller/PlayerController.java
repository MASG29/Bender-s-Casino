package com.bendercasino.controller;

import com.bendercasino.dto.CreatePlayerRequest;
import com.bendercasino.dto.PlayerResponse;
import com.bendercasino.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse create(@Valid @RequestBody CreatePlayerRequest request) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    @GetMapping("/{id}")
    public PlayerResponse getById(@PathVariable UUID id) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    @GetMapping("/{id}/balance")
    public Map<String, Integer> getBalance(@PathVariable UUID id) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }

    @PostMapping("/{id}/reset")
    public PlayerResponse reset(@PathVariable UUID id) {
        // TODO
        throw new UnsupportedOperationException("implement");
    }
}
