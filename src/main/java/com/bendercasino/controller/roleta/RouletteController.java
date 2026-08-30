package com.bendercasino.controller.roleta;

import com.bendercasino.dto.roleta.RouletteSpinRequest;
import com.bendercasino.dto.roleta.RouletteSpinResponse;
import com.bendercasino.model.roleta.RouletteState;
import com.bendercasino.service.roleta.RouletteService;

import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.Player;
import com.bendercasino.repository.PlayerRepository;
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
        GameSession session = rouletteService.spin(request.playerId(), request.bet(), request.betType());
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
