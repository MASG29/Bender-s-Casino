package com.bendercasino.controller.videopoker;

import com.bendercasino.dto.CardDto;
import com.bendercasino.dto.videopoker.DealResponse;
import com.bendercasino.dto.videopoker.DrawRequest;
import com.bendercasino.dto.videopoker.DrawResponse;
import com.bendercasino.dto.StartGameRequest;
import com.bendercasino.model.Card;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.Player;
import com.bendercasino.model.videopoker.VideoPokerState;
import com.bendercasino.service.PlayerService;
import com.bendercasino.service.videopoker.VideoPokerService;
import com.bendercasino.util.CardMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videopoker")
public class VideoPokerController {

    private final VideoPokerService videoPokerService;
    private final PlayerService playerService;

    public VideoPokerController(VideoPokerService videoPokerService, PlayerService playerService) {
        this.videoPokerService = videoPokerService;
        this.playerService = playerService;
    }

    @PostMapping("/deal")
    public DealResponse deal(@Valid @RequestBody StartGameRequest request) {
        GameSession session = videoPokerService.start(request.playerId(), request.bet());
        return new DealResponse(session.getGameId(), cardsFrom(session));
    }

    @PostMapping("/{handId}/draw")
    public DrawResponse draw(@PathVariable UUID handId, @RequestBody DrawRequest request) {
        GameSession session = videoPokerService.act(handId, "draw", request);
        Player player = playerService.findById(session.getPlayerId());
        VideoPokerState poker = (VideoPokerState) session.getState();
        return new DrawResponse(
                cardsFrom(session),
                poker.getCategory(),
                session.getBet().payout(),
                player.getBalance()
        );
    }

    private List<CardDto> cardsFrom(GameSession session) {
        List<Card> cards = ((VideoPokerState) session.getState()).getCards();
        return cards.stream().map(CardMapper::toDto).toList();
    }
}
