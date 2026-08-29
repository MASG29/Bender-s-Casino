package com.bendercasino.controller;

import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.blackjack.Player;
import com.bendercasino.model.slots.EndResult;
import com.bendercasino.model.slots.SpinRequest;
import com.bendercasino.service.PlayerService;
import com.bendercasino.service.SlotsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/slots")
public class SlotsController {

    private SlotsService slotsService;
    private PlayerService playerService;

    @RequestMapping(method = RequestMethod.POST, path = "/roll")
    public ResponseEntity<EndResult> roll(@Valid @RequestBody SpinRequest req) {
        try {
            EndResult er = slotsService.bet(req.getPlayerId(), req.getBetAmount());
            return new ResponseEntity<>(er, HttpStatus.OK);
        } catch (PlayerNotFoundException | InsufficientBalanceException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
