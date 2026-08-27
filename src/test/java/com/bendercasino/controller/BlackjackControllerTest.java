package com.bendercasino.controller;

import com.bendercasino.model.blackjack.BlackjackState;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.blackjack.GameStatus;
import com.bendercasino.model.blackjack.Hand;
import com.bendercasino.model.JokeTrigger;
import com.bendercasino.model.blackjack.Card;
import com.bendercasino.model.blackjack.Outcome;
import com.bendercasino.model.blackjack.Player;
import com.bendercasino.service.BlackjackService;
import com.bendercasino.service.JokeService;
import com.bendercasino.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlackjackController.class)
class BlackjackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlackjackService blackjackService;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private JokeService jokeService;

    private UUID playerId;
    private Player player;
    private GameSession session;
    private Hand playerHand;
    private Hand dealerHand;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        player = new Player("TestPlayer", "testplayer", "Test", "Player", "test@example.com", "hash");
        playerId = player.getId();

        playerHand = new Hand();
        playerHand.add(new Card("AH", "ACE", "HEARTS", ""));
        playerHand.add(new Card("KH", "KING", "HEARTS", ""));

        dealerHand = new Hand();
        dealerHand.add(new Card("9S", "9", "SPADES", ""));
        dealerHand.add(new Card("9D", "9", "DIAMONDS", ""));

        session = new GameSession(playerId, "deck-1", "blackjack", 100);
        BlackjackState state = new BlackjackState();
        state.getPlayerHand().add(playerHand.getCards().get(0));
        state.getPlayerHand().add(playerHand.getCards().get(1));
        state.getDealerHand().add(dealerHand.getCards().get(0));
        state.getDealerHand().add(dealerHand.getCards().get(1));
        session.setState(state);
        session.setStatus(GameStatus.PLAYER_TURN);
    }


    @Test
    @DisplayName("POST /start returns 200 with GameStateResponse on success")
    void start_success() throws Exception {
        when(blackjackService.start(eq(playerId), eq(100))).thenReturn(session);
        when(playerService.findById(eq(playerId))).thenReturn(player);
        when(jokeService.jokeFor(any(Player.class), nullable(Outcome.class))).thenReturn("Test joke");
        when(jokeService.jokeFor(any(Player.class), any(JokeTrigger.class))).thenReturn("Test joke");

        mockMvc.perform(post("/api/blackjack/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":100}
                                """.formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.status").value("PLAYER_TURN"))
                .andExpect(jsonPath("$.playerHand.cards", hasSize(2)))
                .andExpect(jsonPath("$.dealerHand.cards", hasSize(1)))
                .andExpect(jsonPath("$.bet").value(100))
                .andExpect(jsonPath("$.payout").value(0))
                .andExpect(jsonPath("$.benderJoke").value("Test joke"))
                .andExpect(jsonPath("$.streaks.wins").value(0))
                .andExpect(jsonPath("$.streaks.losses").value(0))
                .andExpect(jsonPath("$.streaks.blackjacks").value(0));
    }

    @Test
    @DisplayName("POST /start returns 400 when bet is not positive")
    void start_invalidBet_returns400() throws Exception {
        mockMvc.perform(post("/api/blackjack/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":0}
                                """.formatted(playerId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /start returns 400 when bet is missing")
    void start_missingBet_returns400() throws Exception {
        mockMvc.perform(post("/api/blackjack/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s"}
                                """.formatted(playerId)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("POST /hit returns 200 with GameStateResponse on success")
    void hit_success() throws Exception {
        when(blackjackService.hit(eq(playerId))).thenReturn(session);
        when(playerService.findById(eq(playerId))).thenReturn(player);
        when(jokeService.jokeFor(any(Player.class), nullable(Outcome.class))).thenReturn("Test joke");
        when(jokeService.jokeFor(any(Player.class), any(JokeTrigger.class))).thenReturn("Test joke");

        mockMvc.perform(post("/api/blackjack/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s"}
                                """.formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.status").value("PLAYER_TURN"))
                .andExpect(jsonPath("$.playerHand.cards", hasSize(2)))
                .andExpect(jsonPath("$.dealerHand.cards", hasSize(1)))
                .andExpect(jsonPath("$.bet").value(100));
    }

    @Test
    @DisplayName("POST /hit returns 400 when playerId is missing")
    void hit_missingPlayerId_returns400() throws Exception {
        mockMvc.perform(post("/api/blackjack/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("POST /stand returns 200 with GameStateResponse on success")
    void stand_success() throws Exception {
        session.setStatus(GameStatus.FINISHED);
        ((BlackjackState) session.getState()).setOutcome(Outcome.PLAYER_WIN);
        session.getBet().withPayout(200);

        when(blackjackService.stand(eq(playerId))).thenReturn(session);
        when(playerService.findById(eq(playerId))).thenReturn(player);
        when(jokeService.jokeFor(any(Player.class), nullable(Outcome.class))).thenReturn("Test joke");
        when(jokeService.jokeFor(any(Player.class), any(JokeTrigger.class))).thenReturn("Test joke");

        mockMvc.perform(post("/api/blackjack/stand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s"}
                                """.formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.outcome").value("PLAYER_WIN"))
                .andExpect(jsonPath("$.bet").value(100));
    }

    @Test
    @DisplayName("POST /stand returns 400 when playerId is missing")
    void stand_missingPlayerId_returns400() throws Exception {
        mockMvc.perform(post("/api/blackjack/stand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("GET /state/{playerId} returns 200 with GameStateResponse on success")
    void state_success() throws Exception {
        when(blackjackService.getState(eq(playerId))).thenReturn(session);
        when(playerService.findById(eq(playerId))).thenReturn(player);
        when(jokeService.jokeFor(any(Player.class), nullable(Outcome.class))).thenReturn("Test joke");
        when(jokeService.jokeFor(any(Player.class), any(JokeTrigger.class))).thenReturn("Test joke");

        mockMvc.perform(get("/api/blackjack/state/%s".formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.status").value("PLAYER_TURN"))
                .andExpect(jsonPath("$.playerHand.cards", hasSize(2)))
                .andExpect(jsonPath("$.dealerHand.cards", hasSize(1)))
                .andExpect(jsonPath("$.bet").value(100));
    }

    @Test
    @DisplayName("GET /state/{playerId} returns 404 when playerId format is invalid")
    void state_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/blackjack/state/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("POST /joke returns 200 with JokeResponse on success")
    void joke_success() throws Exception {
        when(playerService.findById(eq(playerId))).thenReturn(player);
        when(jokeService.jokeFor(eq(player), eq(JokeTrigger.GAME_START))).thenReturn("Bender joke here");

        mockMvc.perform(post("/api/blackjack/joke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","trigger":"GAME_START"}
                                """.formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joke").value("Bender joke here"));
    }

    @Test
    @DisplayName("POST /joke returns 400 when playerId is missing")
    void joke_missingPlayerId_returns400() throws Exception {
        mockMvc.perform(post("/api/blackjack/joke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"trigger":"GAME_START"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /joke returns 400 when trigger is missing")
    void joke_missingTrigger_returns400() throws Exception {
        mockMvc.perform(post("/api/blackjack/joke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s"}
                                """.formatted(playerId)))
                .andExpect(status().isBadRequest());
    }
}