package com.bendercasino.controller;

import com.bendercasino.dto.PlayerResponse;
import com.bendercasino.model.Player;
import com.bendercasino.service.PlayerService;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/{id}")
    public PlayerResponse getById(@PathVariable UUID id) {
        return toDto(playerService.findById(id));
    }

    @GetMapping("/{id}/balance")
    public Map<String, Integer> getBalance(@PathVariable UUID id) {
        Player player = playerService.findById(id);
        return Map.of("balance", player.getBalance());
    }

    @PostMapping("/{id}/reset")
    public PlayerResponse reset(@PathVariable UUID id, Authentication authentication) {
        return toDto(playerService.reset(id, authentication));
    }

    private PlayerResponse toDto(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getBalance(),
                new PlayerResponse.StatsDto(
                        player.getTotalWins(),
                        player.getTotalLosses(),
                        player.getTotalPushes(),
                        player.getTotalBlackjacks()
                )
        );
    }
}
