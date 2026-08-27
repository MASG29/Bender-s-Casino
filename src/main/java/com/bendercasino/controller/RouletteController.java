package com.bendercasino.controller;

import com.bendercasino.dto.RouletteSpinRequest;
import com.bendercasino.dto.RouletteSpinResponse;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.Player;
import com.bendercasino.model.RouletteState;
import com.bendercasino.repository.PlayerRepository;
import com.bendercasino.service.RouletteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roulette")
public class RouletteController {

    private final RouletteService rouletteService;
    private final PlayerRepository playerRepository;

    public RouletteController(RouletteService rouletteService, PlayerRepository playerRepository) {
        this.rouletteService = rouletteService;
        this.playerRepository = playerRepository;
    }

    @PostMapping("/spin")
    public RouletteSpinResponse spin(@Valid @RequestBody RouletteSpinRequest request) {
        GameSession session = rouletteService.spin(request.playerId(), request.bet(), request.colour());
        RouletteState state = (RouletteState) session.getState();
        Player player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new PlayerNotFoundException(request.playerId()));

        return new RouletteSpinResponse(
                state.number(),
                state.colour(),
                state.won(),
                state.payout(),
                player.getBalance()
        );
    }
}
