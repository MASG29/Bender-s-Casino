package com.bendercasino.controller;

import com.bendercasino.dto.*;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.JokeTrigger;
import com.bendercasino.model.Player;
import com.bendercasino.service.BlackjackService;
import com.bendercasino.service.JokeService;
import com.bendercasino.service.PlayerService;
import com.bendercasino.util.CardMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/blackjack")
public class BlackjackController {

    private final BlackjackService blackjackService;
    private final PlayerService playerService;
    private final JokeService jokeService;

    public BlackjackController(BlackjackService blackjackService, PlayerService playerService, JokeService jokeService) {
        this.blackjackService = blackjackService;
        this.playerService = playerService;
        this.jokeService = jokeService;
    }

    @PostMapping("/start")
    public GameStateResponse start(@Valid @RequestBody StartGameRequest request) {
        GameSession session = blackjackService.start(request.playerId(), request.bet());
        return toDTO(session);
    }

    @PostMapping("/hit")
    public GameStateResponse hit(@Valid @RequestBody PlayerActionRequest request) {
        GameSession session = blackjackService.hit(request.playerId());
        return toDTO(session);
    }

    @PostMapping("/stand")
    public GameStateResponse stand(@Valid @RequestBody PlayerActionRequest request) {
        GameSession session = blackjackService.stand(request.playerId());
        return toDTO(session);
    }

    @GetMapping("/state/{playerId}")
    public GameStateResponse state(@PathVariable UUID playerId) {
        GameSession session = blackjackService.getState(playerId);
        return toDTO(session);
    }

    @PostMapping("/joke")
    public JokeResponse joke(@Valid @RequestBody JokeRequest request) {
        Player player = playerService.findById(request.playerId());
        JokeTrigger trigger = JokeTrigger.valueOf(request.trigger());
        String joke = jokeService.jokeFor(player, trigger);
        return new JokeResponse(joke);
    }

    private GameStateResponse toDTO(GameSession session) {
        boolean hidden = session.getStatus() == GameStatus.PLAYER_TURN;
        HandDto dealerDto = hidden
                ? CardMapper.toDtoDealerHidden(session.getDealerHand())
                : CardMapper.toDto(session.getDealerHand());

        return new GameStateResponse(
                session.getGameId(),
                session.getPlayerId(),
                session.getStatus().name(),
                CardMapper.toDto(session.getPlayerHand()),
                dealerDto,
                session.getBet().amount(),
                session.getOutcome() != null ? session.getOutcome().name() : null,
                session.getBet().payout(),
                null,
                null
        );
    }
}
