package com.bendercasino.controller;

import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.model.Colour;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.model.RouletteState;
import com.bendercasino.repository.PlayerRepository;
import com.bendercasino.service.RouletteService;
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
        session.setState(new RouletteState(1, Colour.RED, Colour.RED, true, 200));

        when(rouletteService.spin(eq(playerId), eq(100), eq(Colour.RED))).thenReturn(session);

        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":100,"colour":"RED"}
                                """.formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.colour").value("RED"))
                .andExpect(jsonPath("$.won").value(true))
                .andExpect(jsonPath("$.payout").value(200))
                .andExpect(jsonPath("$.balance").value(player.getBalance()));
    }

    @Test
    @DisplayName("POST /api/roulette/spin without a playerId returns 400")
    void spin_missingPlayerId_returns400() throws Exception {
        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bet":100,"colour":"RED"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/roulette/spin with a non-positive bet returns 400")
    void spin_invalidBet_returns400() throws Exception {
        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":0,"colour":"RED"}
                                """.formatted(playerId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/roulette/spin returns 400 INSUFFICIENT_BALANCE when the service rejects the bet")
    void spin_insufficientBalance_returns400() throws Exception {
        when(rouletteService.spin(any(UUID.class), anyInt(), any(Colour.class)))
                .thenThrow(new InsufficientBalanceException(player.getName(), player.getBalance(), 10_000));

        mockMvc.perform(post("/api/roulette/spin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerId":"%s","bet":10000,"colour":"RED"}
                                """.formatted(playerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_BALANCE"));
    }
}
