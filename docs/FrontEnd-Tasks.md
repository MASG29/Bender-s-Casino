# Bender's Casino — Tarefas Front-end

> Contrato REST combinado em equipa no Dia 1 — `GameStateResponse` e `PlayerResponse` (ver `dto/` no back-end) são a fonte de verdade para o que a UI recebe.

SPA vanilla HTML/CSS/JS em `src/main/resources/static/`, mesma origem que o back-end — sem CORS, um único `.jar` no fim.

## Eddie — Estrutura + Tema

| # | Tarefa | Ficheiros | Depende de |
|---|---|---|---|
| C1 | `index.html` + `theme.css` (paleta neon/metal) + fontes | `static/` | — |
| C2 | `router.js` (History API, `popstate`, links `data-link`) | `static/js/` | C1 |
| C3 | View Home: input de nome → `POST /api/players` → `/lobby` | `views/home.js` | C2, D2 |
| C4 | View Lobby: mesas, saldo, botão "Jogar Blackjack" | `views/lobby.js` | C2 |
| C5 | View Profile: nome, saldo, `stats`, botão reset → `POST /api/players/{id}/reset` | `views/profile.js` | C2, B8 |
| C6 | `table.css` — mesa, slots de cartas, fichas, animação `deal` | `static/css/` | C1 |
| C7 | `components/card.js` (`<img>` a partir da URL da API) + `chips.js` | `components/` | C6 |
| C8 | Responsivo + estados de loading/erro | todos | C3–C7 |

## Tiago Paulos — Integração + Bender

| # | Tarefa | Ficheiros | Depende de |
|---|---|---|---|
| D1 | `bender-jokes.json` — ≥6 frases por `JokeTrigger` (conteúdo, sem código) ⚠️ *desbloqueia o back-end* | `resources/` | — |
| D2 | `api.js` (wrapper fetch) + `ApiError` + `state.js` com `sessionStorage` | `static/js/` | formato dos DTOs |
| D3 | View BlackjackTable: apostar → start → hit/stand | `views/blackjack.js` | C6, D2 |
| D4 | Render do `GameStateResponse` (mãos, valores, carta tapada) | `views/blackjack.js` | D3 |
| D5 | `components/bender.js` — painel do Bender, efeito typewriter, fala em cada ação | `components/` | D2 |
| D6 | Animações: distribuir, virar a carta do dealer, contador de fichas | `static/js/` | D4 |
| D7 | Ecrãs de fim de mão (WIN/LOSE/PUSH/BLACKJACK) + "Nova mão" | `views/blackjack.js` | D4 |
| D8 | Tratamento de erros na UI (saldo insuficiente, API de cartas em baixo) | `static/js/` | D2 |

## Armadilhas a não esquecer (secção 4 do plano)

- **F5 em `/lobby`, `/blackjack`, `/profile` dá 404** com a History API se o `SpaForwardController` (já existe no back-end) não estiver a apanhar essas rotas — testar isto cedo.
- **`playerId` tem de ir para `sessionStorage`** — sem isso, um F5 a meio do jogo perde o jogador e o saldo.

## Caminho crítico

`D1` é uma das duas primeiras coisas a fazer no Dia 1 — desbloqueia o `JokeService` do back-end. `D3` fecha o caminho crítico do MVP.

## Branch

```
main
 └─ dev
     ├─ feat/c-ui            (Eddie)
     └─ feat/d-integracao    (Tiago Paulos)
```
