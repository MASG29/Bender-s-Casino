package com.bendercasino.controller;

import com.bendercasino.model.slots.EndResult;
import com.bendercasino.model.slots.SpinRequest;
import com.bendercasino.service.PlayerService;
import com.bendercasino.service.SlotsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/slots")
public class SlotsController {

    private SlotsService slotsService;
    private PlayerService playerService;

    @RequestMapping(method = RequestMethod.POST, path = "/roll")
    public ResponseEntity<EndResult> roll(@Valid @RequestBody SpinRequest req) {
        EndResult er = slotsService.bet(req.getPlayerId(), req.getBetAmount());
        return new ResponseEntity<>(er, HttpStatus.OK);
    }

    @Autowired
    public void setSlotsService(SlotsService slotsService) {
        this.slotsService = slotsService;
    }

    @Autowired
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }
}
