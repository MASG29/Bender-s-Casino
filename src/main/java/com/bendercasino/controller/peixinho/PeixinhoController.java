package com.bendercasino.controller.peixinho;

import com.bendercasino.dto.peixinho.AskCardRequest;
import com.bendercasino.dto.peixinho.AskResultResponse;
import com.bendercasino.dto.peixinho.PeixinhoStateResponse;
import com.bendercasino.dto.peixinho.StartPeixinhoRequest;
import com.bendercasino.service.peixinho.PeixinhoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/peixinho")
public class PeixinhoController {

    private final PeixinhoService peixinhoService;

    public PeixinhoController(PeixinhoService peixinhoService) {
        this.peixinhoService = peixinhoService;
    }

    @PostMapping("/start")
    public PeixinhoStateResponse start(@Valid @RequestBody StartPeixinhoRequest request) {
        return peixinhoService.start(request.playerId(), request.bet());
    }


    @PostMapping("/ask")
    public AskResultResponse ask(@Valid @RequestBody AskCardRequest request) {
        return peixinhoService.ask(
                request.playerId(),
                request.targetId(),
                request.cardValue()
        );
    }

    @GetMapping("/state/{playerId}")
    public PeixinhoStateResponse state(@PathVariable UUID playerId) {
        return peixinhoService.getState(playerId);
    }
}