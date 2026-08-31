package com.bendercasino.controller.slots;

import com.bendercasino.dto.slots.SlotsSpinRequest;
import com.bendercasino.dto.slots.SlotsSpinResponse;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.Player;
import com.bendercasino.model.slots.SlotsState;
import com.bendercasino.repository.PlayerRepository;
import com.bendercasino.service.slots.SlotsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slots")
public class SlotsController {

    private final SlotsService slotsService;
    private final PlayerRepository playerRepository;

    public SlotsController(SlotsService slotsService, PlayerRepository playerRepository) {
        this.slotsService = slotsService;
        this.playerRepository = playerRepository;
    }

    @PostMapping("/roll")
    public SlotsSpinResponse roll(@Valid @RequestBody SlotsSpinRequest request) {
        GameSession session = slotsService.spin(request.playerId(), request.betAmount());
        SlotsState state = (SlotsState) session.getState();
        Player player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new PlayerNotFoundException(request.playerId()));

        return new SlotsSpinResponse(
                state.symbols(),
                state.outcome(),
                state.payout(),
                player.getBalance()
        );
    }
}
