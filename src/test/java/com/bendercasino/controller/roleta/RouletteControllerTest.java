package com.bendercasino.controller.roleta;

import com.bendercasino.model.roleta.BetType;
import com.bendercasino.model.roleta.Colour;
import com.bendercasino.model.roleta.RouletteState;
import com.bendercasino.service.roleta.RouletteService;

import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouletteController.class)
class RouletteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RouletteService rouletteService;

    @MockitoBean
    private PlayerRepository playerRepository;

    private UUID playerId;
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Fry", "fry", "Philip", "Fry", "fry@example.com", "hash");
        playerId = player.getId();
        when(playerRepository.findById(eq(playerId))).thenReturn(Optional.of(player));
    }

    @Test
    @DisplayName("POST /api/roulette/spin returns 200 with the spin result on success")
    void spin_success() throws Exception {
        GameSession session = new GameSession(playerId, null, "roleta", 100);
        session.setStatus(GameStatus.FINISHED);
        session.setState(new RouletteState(1, Colour.RED, BetType.RED, true, 200));

        when(rouletteService.spin(eq(playerId), eq(100), eq(BetType.RED))).thenReturn(session);

        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":100,"betType":"RED"}
                                """.formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.colour").value("RED"))
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.payout").value(200))
                .andExpect(jsonPath("$.balance").value(player.getBalance()));
    }

    @Test
    @DisplayName("POST /api/roulette/spin accepts even/odd/low/high bet types")
    void spin_success_evenBetType() throws Exception {
        GameSession session = new GameSession(playerId, null, "roleta", 100);
        session.setStatus(GameStatus.FINISHED);
        session.setState(new RouletteState(4, Colour.BLACK, BetType.EVEN, true, 200));

        when(rouletteService.spin(eq(playerId), eq(100), eq(BetType.EVEN))).thenReturn(session);

        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":100,"betType":"EVEN"}
                                """.formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(4))
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.payout").value(200));
    }

    @Test
    @DisplayName("POST /api/roulette/spin without a playerId returns 400")
    void spin_missingPlayerId_returns400() throws Exception {
        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bet":100,"betType":"RED"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/roulette/spin with a non-positive bet returns 400")
    void spin_invalidBet_returns400() throws Exception {
        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":0,"betType":"RED"}
                                """.formatted(playerId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/roulette/spin returns 400 INSUFFICIENT_BALANCE when the service rejects the bet")
    void spin_insufficientBalance_returns400() throws Exception {
        when(rouletteService.spin(any(UUID.class), anyInt(), any(BetType.class)))
                .thenThrow(new InsufficientBalanceException(player.getName(), player.getBalance(), 10_000));

        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":10000,"betType":"RED"}
                                """.formatted(playerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_BALANCE"));
    }
}
