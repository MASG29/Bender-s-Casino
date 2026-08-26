package com.bendercasino.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * L-B4: /api/games/** e /api/players/** exigem sessão autenticada; /api/auth/** e os
 * estáticos da SPA ficam abertos. Contexto completo (filtros de security incluídos),
 * com o H2 em memória do application.yml de testes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ApiSecurityTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new JsonMapper();

    private String uniqueSuffix;

    @BeforeEach
    void setUp() {
        // o login do teste anterior deixa contexto no ThreadLocal; cada teste começa limpo
        SecurityContextHolder.clearContext();
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
    }

    // --- pedido sem sessão ---

    @Test
    @DisplayName("GET /api/players/{id} sem sessão devolve 401")
    void playersWithoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/players/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/games/blackjack/start sem sessão devolve 401")
    void gamesWithoutSession_returns401() throws Exception {
        mockMvc.perform(post("/api/games/blackjack/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\",\"bet\":10}".formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/blackjack/start sem sessão devolve 401 — alias documentado de /api/games/blackjack")
    void blackjackAliasWithoutSession_returns401() throws Exception {
        mockMvc.perform(post("/api/blackjack/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\",\"bet\":10}".formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/players/{id} inválido sem sessão devolve 401, não 400 — o filtro corre antes do controller")
    void playersInvalidIdWithoutSession_returns401Not400() throws Exception {
        mockMvc.perform(get("/api/players/not-a-uuid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/players/{id}/reset sem sessão devolve 401")
    void resetWithoutSession_returns401() throws Exception {
        mockMvc.perform(post("/api/players/" + UUID.randomUUID() + "/reset"))
                .andExpect(status().isUnauthorized());
    }

    // --- pedido com sessão válida (login primeiro) ---

    @Test
    @DisplayName("Depois de POST /api/auth/login, GET /api/players/{id} com a mesma sessão devolve 200")
    void loginThenGetPlayer_returns200() throws Exception {
        String username = "sec_" + uniqueSuffix;
        UUID id = register(username);
        MockHttpSession session = login(username);

        mockMvc.perform(get("/api/players/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(id.toString()));
    }

    @Test
    @DisplayName("Depois do login, POST /api/players/{id}/reset ao próprio devolve 200")
    void loginThenResetSelf_returns200() throws Exception {
        String username = "sec_" + uniqueSuffix;
        UUID id = register(username);
        MockHttpSession session = login(username);

        mockMvc.perform(post("/api/players/" + id + "/reset").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    @DisplayName("Sessão do jogador A não consegue fazer reset da conta do jogador B — 403")
    void resetAnotherPlayer_returns403() throws Exception {
        String usernameA = "sec_" + uniqueSuffix;
        register(usernameA);
        MockHttpSession sessionA = login(usernameA);
        UUID playerIdB = register("other_" + uniqueSuffix);

        mockMvc.perform(post("/api/players/" + playerIdB + "/reset").session(sessionA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN_RESET"));
    }

    @Test
    @DisplayName("Login guarda a autenticação na sessão — o pedido seguinte já não é anónimo")
    void loginStoresAuthenticatedSession() throws Exception {
        String username = "sec_" + uniqueSuffix;
        UUID id = register(username);
        MockHttpSession session = login(username);

        // um endpoint autenticado qualquer: se a sessão não tivesse o contexto, devolvia 401
        mockMvc.perform(get("/api/players/" + id + "/balance").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));
    }

    // --- rotas que continuam abertas ---

    @Test
    @DisplayName("POST /api/auth/register continua aberto sem sessão (201)")
    void registerStaysOpen() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("open_" + uniqueSuffix)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/auth/login com credenciais erradas devolve o 401 do handler, não o do entry point")
    void loginBadCredentials_stillReturnsHandler401() throws Exception {
        String username = "sec_" + uniqueSuffix;
        register(username);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"wrong-password"}
                                """.formatted(username)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("index.html e assets da SPA continuam acessíveis sem sessão")
    void staticResourcesStayOpen() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/router.js")).andExpect(status().isOk());
        mockMvc.perform(get("/styles/style.css")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rotas do SPA (/lobby) fazem forward para index.html sem sessão")
    void spaRoutesStayOpen() throws Exception {
        MvcResult result = mockMvc.perform(get("/lobby"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getForwardedUrl()).isEqualTo("/index.html");
    }

    // --- helpers ---

    /** Regista um jogador novo (username/email únicos por teste) e devolve o playerId. */
    private UUID register(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(body.get("playerId").asText());
    }

    /** Faz login e devolve a sessão criada, para usar nos pedidos seguintes. */
    private MockHttpSession login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"secret123"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }

    private String registerBody(String username) {
        return """
                {"name":"Sec Player","username":"%s","firstName":"Sec","lastName":"Player","email":"%s@test.com","password":"secret123"}
                """.formatted(username, username);
    }
}
