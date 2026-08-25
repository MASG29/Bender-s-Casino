package com.bendercasino.controller;

import com.bendercasino.dto.CreatePlayerRequest;
import com.bendercasino.dto.PlayerResponse;
import com.bendercasino.model.Player;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    private UUID playerId;
    private Player player;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        player = new Player("TestPlayer", "testplayer", "hash");
        playerId = player.getId();
    }

    // --- POST /api/players ---

    @Test
    @DisplayName("POST / returns 201 with PlayerResponse on success")
    void create_success() throws Exception {
        when(playerService.create(eq("TestPlayer"), eq("testplayer"), eq("secret123"))).thenReturn(player);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"TestPlayer","username":"testplayer","password":"secret123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.name").value("TestPlayer"))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.stats.wins").value(0))
                .andExpect(jsonPath("$.stats.losses").value(0))
                .andExpect(jsonPath("$.stats.pushes").value(0))
                .andExpect(jsonPath("$.stats.blackjacks").value(0));
    }

    @Test
    @DisplayName("POST / returns 400 when name is blank")
    void create_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","username":"","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST / returns 400 when name is missing")
    void create_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/players/{id} ---

    @Test
    @DisplayName("GET /{id} returns 200 with PlayerResponse on success")
    void getById_success() throws Exception {
        when(playerService.findById(eq(playerId))).thenReturn(player);

        mockMvc.perform(get("/api/players/%s".formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.name").value("TestPlayer"))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.stats.wins").value(0))
                .andExpect(jsonPath("$.stats.losses").value(0))
                .andExpect(jsonPath("$.stats.pushes").value(0))
                .andExpect(jsonPath("$.stats.blackjacks").value(0));
    }

    @Test
    @DisplayName("GET /{id} returns 400 when id format is invalid")
    void getById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/players/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/players/{id}/balance ---

    @Test
    @DisplayName("GET /{id}/balance returns 200 with balance map on success")
    void getBalance_success() throws Exception {
        player.credit(250); // balance = 1250
        when(playerService.findById(eq(playerId))).thenReturn(player);

        mockMvc.perform(get("/api/players/%s/balance".formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1250));
    }

    @Test
    @DisplayName("GET /{id}/balance returns 400 when id format is invalid")
    void getBalance_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/players/not-a-uuid/balance"))
                .andExpect(status().isBadRequest());
    }

    // --- POST /api/players/{id}/reset ---

    @Test
    @DisplayName("POST /{id}/reset returns 200 with PlayerResponse on success")
    void reset_success() throws Exception {
        Player resetPlayer = new Player("TestPlayer", "testplayer", "hash");
        when(playerService.reset(eq(playerId))).thenReturn(resetPlayer);

        mockMvc.perform(post("/api/players/%s/reset".formatted(playerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(resetPlayer.getId().toString()))
                .andExpect(jsonPath("$.name").value("TestPlayer"))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.stats.wins").value(0))
                .andExpect(jsonPath("$.stats.losses").value(0))
                .andExpect(jsonPath("$.stats.pushes").value(0))
                .andExpect(jsonPath("$.stats.blackjacks").value(0));
    }

    @Test
    @DisplayName("POST /{id}/reset returns 400 when id format is invalid")
    void reset_invalidUuid_returns400() throws Exception {
        mockMvc.perform(post("/api/players/not-a-uuid/reset"))
                .andExpect(status().isBadRequest());
    }
}