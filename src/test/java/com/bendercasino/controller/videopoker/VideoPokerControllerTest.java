package com.bendercasino.controller.videopoker;

import com.bendercasino.dto.videopoker.DrawRequest;
import com.bendercasino.model.Card;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.model.videopoker.PokerHandCategory;
import com.bendercasino.model.videopoker.VideoPokerState;
import com.bendercasino.service.PlayerService;
import com.bendercasino.service.videopoker.VideoPokerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VideoPokerController.class)
class VideoPokerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoPokerService videoPokerService;

    @MockitoBean
    private PlayerService playerService;

    private UUID playerId;
    private Player player;
    private GameSession session;

    private static Card card(String value, String suit) {
        return new Card(value.substring(0, 1) + suit.substring(0, 1), value, suit, "");
    }

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer", "testplayer", "Test", "Player", "test@example.com", "hash");
        playerId = player.getId();
        session = new GameSession(playerId, "deck-1", "videopoker", 100);
        session.setState(new VideoPokerState(List.of(
                card("2", "HEARTS"), card("6", "HEARTS"), card("9", "HEARTS"),
                card("JACK", "HEARTS"), card("KING", "HEARTS"))));
    }

    @Test
    @DisplayName("POST /deal returns 5 cards and handId")
    void deal_success() throws Exception {
        when(videoPokerService.start(eq(playerId), eq(100))).thenReturn(session);

        mockMvc.perform(post("/api/videopoker/deal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":100}
                                """.formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handId").value(session.getGameId().toString()))
                .andExpect(jsonPath("$.cards", hasSize(5)));
    }

    @Test
    @DisplayName("POST /deal returns 400 when bet is not positive")
    void deal_invalidBet_returns400() throws Exception {
        mockMvc.perform(post("/api/videopoker/deal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":0}
                                """.formatted(playerId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /{handId}/draw returns category, payout and balance")
    void draw_success() throws Exception {
        VideoPokerState poker = (VideoPokerState) session.getState();
        poker.setCategory(PokerHandCategory.FLUSH);
        session.setBet(session.getBet().withPayout(600));
        session.setStatus(GameStatus.FINISHED);

        when(videoPokerService.act(eq(session.getGameId()), eq("draw"), eq(new DrawRequest(List.of(0, 1, 2, 3, 4)))))
                .thenReturn(session);
        when(playerService.findById(eq(playerId))).thenReturn(player);

        mockMvc.perform(post("/api/videopoker/%s/draw".formatted(session.getGameId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"held\":[0,1,2,3,4]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards", hasSize(5)))
                .andExpect(jsonPath("$.category").value("FLUSH"))
                .andExpect(jsonPath("$.payout").value(600))
                .andExpect(jsonPath("$.balance").value(player.getBalance()));
    }
}
