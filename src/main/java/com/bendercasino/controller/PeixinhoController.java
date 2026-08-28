package com.bendercasino.controller;

import com.bendercasino.dto.AskCardRequest;
import com.bendercasino.dto.AskResultResponse;
import com.bendercasino.dto.PeixinhoStateResponse;
import com.bendercasino.dto.StartPeixinhoRequest;
import com.bendercasino.service.PeixinhoService;
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