package com.bendercasino.controller;

import com.bendercasino.dto.*;
import com.bendercasino.model.blackjack.BlackjackState;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.blackjack.GameStatus;
import com.bendercasino.model.JokeTrigger;
import com.bendercasino.model.blackjack.Player;
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
        Player player = playerService.findById(request.playerId());
        return toDTO(session, player);
    }

    @PostMapping("/hit")
    public GameStateResponse hit(@Valid @RequestBody PlayerActionRequest request) {
        GameSession session = blackjackService.hit(request.playerId());
        Player player = playerService.findById(request.playerId());
        return toDTO(session, player);
    }

    @PostMapping("/stand")
    public GameStateResponse stand(@Valid @RequestBody PlayerActionRequest request) {
        GameSession session = blackjackService.stand(request.playerId());
        Player player = playerService.findById(request.playerId());
        return toDTO(session, player);
    }

    @GetMapping("/state/{playerId}")
    public GameStateResponse state(@PathVariable UUID playerId) {
        GameSession session = blackjackService.getState(playerId);
        Player player = playerService.findById(playerId);
        return toDTO(session, player);
    }

    @PostMapping("/joke")
    public JokeResponse joke(@Valid @RequestBody JokeRequest request) {
        Player player = playerService.findById(request.playerId());
        JokeTrigger trigger = JokeTrigger.valueOf(request.trigger());
        String joke = jokeService.jokeFor(player, trigger);
        return new JokeResponse(joke);
    }

    private GameStateResponse toDTO(GameSession session, Player player) {
        BlackjackState state = (BlackjackState) session.getState();
        boolean hidden = session.getStatus() == GameStatus.PLAYER_TURN;

        HandDto dealerDto = hidden
                ? CardMapper.toDtoDealerHidden(state.getDealerHand())
                : CardMapper.toDto(state.getDealerHand());

        String joke = jokeService.jokeFor(player, state.getOutcome());

        GameStateResponse.StreaksDto streaks = new GameStateResponse.StreaksDto(
                player.getConsecutiveWins(),
                player.getConsecutiveLosses(),
                player.getConsecutiveBlackjacks()
        );

        return new GameStateResponse(
                session.getGameId(),
                session.getPlayerId(),
                session.getStatus().name(),
                CardMapper.toDto(state.getPlayerHand()),
                dealerDto,
                session.getBet().amount(),
                state.getOutcome() != null ? state.getOutcome().name() : null,
                session.getBet().payout(),
                joke,
                streaks
        );
    }
}
