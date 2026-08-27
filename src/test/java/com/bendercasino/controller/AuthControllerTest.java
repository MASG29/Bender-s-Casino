package com.bendercasino.controller;

import com.bendercasino.exception.InvalidCredentialsException;
import com.bendercasino.model.blackjack.Player;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean PlayerService playerService;

    private UUID playerId;
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer", "testplayer", "Test", "Player", "test@example.com", "hash");
        playerId = player.getId();
    }


    @Test
    @DisplayName("POST /register returns 201 with PlayerResponse on success")
    void register_success_returns201() throws Exception {
        when(playerService.create("TestPlayer", "testplayer", "Test", "Player", "test@example.com", "secret123"))
                .thenReturn(player);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"TestPlayer","username":"testplayer","firstName":"Test","lastName":"Player","email":"test@example.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.name").value("TestPlayer"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    @DisplayName("POST /register returns 400 when fields are blank")
    void register_blankFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","username":"","firstName":"","lastName":"","email":"","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register returns 400 when email is malformed")
    void register_malformedEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"TestPlayer","username":"testplayer","firstName":"Test","lastName":"Player","email":"not-an-email","password":"secret123"}
                                """))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("POST /login returns 200 with PlayerResponse on valid credentials")
    void login_success() throws Exception {
        when(playerService.login("testplayer", "secret123")).thenReturn(player);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"testplayer","password":"secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(playerId.toString()))
                .andExpect(jsonPath("$.name").value("TestPlayer"));
    }

    @Test
    @DisplayName("POST /login returns 401 on invalid credentials")
    void login_badCredentials_returns401() throws Exception {
        when(playerService.login(anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"testplayer","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }


    @Test
    @DisplayName("POST /logout returns 200 with status logged_out")
    void logout_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("logged_out"));
    }
}
