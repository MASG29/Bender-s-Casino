package com.bendercasino.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contexto completo (todos os controllers carregados) para provar que o catch-all do
 * SpaForwardController nunca disputa rotas com os controllers de /api/**.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SpaForwardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String FORWARD_URL = "/index.html";

    @Test
    @DisplayName("GET /lobby faz forward para index.html")
    void forwardLobby() throws Exception {
        mockMvc.perform(get("/lobby"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(FORWARD_URL));
    }

    @Test
    @DisplayName("GET /blackjack faz forward para index.html")
    void forwardBlackjack() throws Exception {
        mockMvc.perform(get("/blackjack"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(FORWARD_URL));
    }

    @Test
    @DisplayName("GET /profile faz forward para index.html")
    void forwardProfile() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(FORWARD_URL));
    }

    @Test
    @DisplayName("Rota SPA nova sem estar na lista à mão também faz forward")
    void forwardNewRouteWithoutManualEntry() throws Exception {
        mockMvc.perform(get("/um-jogo-novo-qualquer"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(FORWARD_URL));
    }

    @Test
    @DisplayName("Rota aninhada do SPA de 2 segmentos faz forward")
    void forwardNestedRoute() throws Exception {
        mockMvc.perform(get("/lobby/qualquer-coisa"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(FORWARD_URL));
    }

    @Test
    @DisplayName("Asset estático (.js) não é intercetado pelo forward")
    void staticAssetNotForwarded() throws Exception {
        mockMvc.perform(get("/router.js"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String forwarded = result.getResponse().getForwardedUrl();
                    if (FORWARD_URL.equals(forwarded)) {
                        throw new AssertionError("/router.js foi intercetado pelo SpaForwardController");
                    }
                });
    }

    @Test
    @DisplayName("GET /api/players/{id} inexistente vai ao PlayerController e devolve 404 JSON, não o index.html")
    void apiPlayerRouteNotForwarded() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/players/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PLAYER_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/games/blackjack/{id}/state com jogo inexistente devolve 404 JSON, não o index.html")
    void apiGameStateRouteNotForwarded() throws Exception {
        UUID randomGameId = UUID.randomUUID();
        mockMvc.perform(get("/api/games/blackjack/" + randomGameId + "/state"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("GAME_NOT_FOUND"));
    }
}
