package com.bendercasino.controller;

import com.bendercasino.model.Player;
import com.bendercasino.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
        player = new Player("TestPlayer", "testplayer", "Test", "Player", "test@example.com", "hash");
        playerId = player.getId();
    }


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


    @Test
    @DisplayName("GET /{id}/balance returns 200 with balance map on success")
    void getBalance_success() throws Exception {
        player.credit(250);
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


    @Test
    @DisplayName("POST /{id}/reset returns 200 with PlayerResponse when the authenticated player resets themselves")
    void reset_success() throws Exception {
        // the Authentication param resolves to null in a @WebMvcTest slice, so the mock must accept that
        Player resetPlayer = new Player("TestPlayer", "testplayer", "Test", "Player", "test@example.com", "hash");
        resetPlayer.credit(500);
        when(playerService.reset(eq(playerId), nullable(Authentication.class))).thenReturn(resetPlayer);

        mockMvc.perform(post("/api/players/%s/reset".formatted(playerId))
                        .with(user("testplayer").roles("PLAYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(resetPlayer.getId().toString()))
                .andExpect(jsonPath("$.name").value("TestPlayer"))
                .andExpect(jsonPath("$.balance").value(1500))
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