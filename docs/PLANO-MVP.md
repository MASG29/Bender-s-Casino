# PLANO MVP — Bender's Casino (Blackjack)

> **Onde vive este documento:** este é o artefacto oficial da equipa, atualizado diretamente no repositório via PR normal sempre que uma decisão do plano muda.

---

## Contexto

Equipa de 4 alunos, 3 dias (entrega segunda-feira), SPA "Bender's Casino" com **apenas Blackjack** no MVP. O repositório está vazio (só `README.md`, 1 commit). Não há `.gitignore` — se 4 pessoas clonarem assim, em menos de uma hora há lixo do IDE commitado.

Objetivo deste plano: cada item do Dia N é um caminho de ficheiro concreto. Nada de prosa sem ação executável.

**Stack decidida:**
| | |
|---|---|
| Back-end | Spring Boot **4.1.1**, Java **21** (LTS, já instalado em `/usr/lib/jvm/java-21-openjdk-amd64`) |
| Build | Maven + **Maven Wrapper commitado** (`./mvnw`) — as 4 máquinas têm JDKs diferentes (23/25/26); o wrapper e o `<java.version>21</java.version>` garantem builds iguais |
| Front-end | HTML/CSS/JS vanilla em `src/main/resources/static/` — mesma origem, zero CORS, **um único `.jar`** |
| Cartas | Deck of Cards API (externa) |
| Estado | Em memória (`ConcurrentHashMap`) |
| Testes | Da responsabilidade da Pessoa A — ver secção 5 |

---

## 1. UML — Diagrama de Classes (back-end)

```
┌─────────────────────────┐         ┌──────────────────────────────┐
│        Player           │         │        GameSession           │
├─────────────────────────┤ 1     1 ├──────────────────────────────┤
│ - id: UUID              │◄────────│ - gameId: UUID               │
│ - name: String          │         │ - playerId: UUID             │
│ - balance: int          │         │ - deckId: String             │
│ - consecutiveWins: int  │         │ - playerHand: Hand           │
│ - consecutiveLosses: int│         │ - dealerHand: Hand           │
│ - consecutiveBJ: int    │         │ - bet: Bet                   │
│ - totalWins: int        │         │ - status: GameStatus         │
│ - totalLosses: int      │         │ - outcome: Outcome           │
│ - totalPushes: int      │         ├──────────────────────────────┤
│ - totalBlackjacks: int  │         │ + isFinished(): boolean      │
├─────────────────────────┤         └───────┬──────────┬───────────┘
│ + debit(int)            │                 │ 2        │ 1
│ + credit(int)           │                 │          │
│ + canAfford(int): bool  │                 │          │
│ + registerWin/Loss/     │                 │          │
│       Push/Blackjack()  │                 │          │
│ + reset()               │                 │          │
└─────────────────────────┘                 │          │
                                            ▼          ▼
                            ┌───────────────────┐  ┌─────────────┐
                            │       Hand        │  │     Bet     │
                            ├───────────────────┤  ├─────────────┤
                            │ - cards: List<Card>  │ - amount:int│
                            ├───────────────────┤  │ - payout:int│
                            │ + add(Card)       │  └─────────────┘
                            │ + value(): int    │
                            │ + isSoft(): bool  │
                            │ + isBlackjack()   │
                            │ + isBusted()      │
                            └─────────┬─────────┘
                                      │ 2..n
                                      ▼
                          ┌──────────────────────┐
                          │    Card  (record)    │
                          ├──────────────────────┤
                          │ code: String  "KH"   │
                          │ value: String "KING" │
                          │ suit: String "HEARTS"│
                          │ image: String (url)  │
                          ├──────────────────────┤
                          │ + points(): int      │  ← "2".."10"=n, J/Q/K=10, ACE=11
                          │ + isAce(): boolean   │
                          └──────────────────────┘

enum GameStatus  { PLAYER_TURN, DEALER_TURN, FINISHED }
enum Outcome     { PLAYER_BLACKJACK, PLAYER_WIN, DEALER_WIN, PLAYER_BUST,
                   DEALER_BUST, PUSH }
enum JokeTrigger { GAME_START, PLAYER_BLACKJACK, PLAYER_BUST, PLAYER_WIN,
                   DEALER_WIN, PUSH, LOSING_STREAK, WINNING_STREAK,
                   DOUBLE_BLACKJACK, BROKE }

┌───────────────────────────┐        ┌──────────────────────────────────────┐
│  «interface» DeckClient   │◄───────│ DeckOfCardsApiClient                 │
├───────────────────────────┤        │ @ConditionalOnProperty(name=         │
│ newShuffledDeck(int): Deck│        │  "deckofcards.mode", havingValue=    │
│ draw(String,int):         │        │  "api", matchIfMissing=true)         │
│            List<Card>     │        └──────────────────────────────────────┘
└───────────────────────────┘◄───────┌──────────────────────────────────────┐
                                     │ InMemoryDeckClient  (testes + demo)  │
                                     │ @ConditionalOnProperty(name=         │
                                     │  "deckofcards.mode", havingValue=    │
                                     │  "memory")                           │
                                     └──────────────────────────────────────┘

record Deck(String deckId, int remaining)
```

**Nota de arquitetura sobre `DeckClient`:** a interface com duas implementações **não é over-engineering** — é o que permite testar `BlackjackService` sem rede (requisito do TDD) e é literalmente a entidade `Deck` que o vosso enunciado antecipava (*"se não usarmos API externa"*).

**Bónus — o plano B da demo.** A escolha da implementação é uma *property*, não código. Se a API externa estiver em baixo na segunda-feira de manhã:
```bash
java -jar target/*.jar --deckofcards.mode=memory
```
O jogo continua a funcionar sem rede. **Não fazer isto com `@Primary`** — obrigaria a editar código e recompilar em pânico à frente da turma. São as mesmas 3 linhas de trabalho; esta versão é a que se consegue usar sob pressão. Testar este comando **uma vez no Dia 3**, para não ser a primeira vez na demo.

**Nota sobre os contadores do `Player` — são duas famílias diferentes, não misturar:**
- `consecutiveWins/Losses/BJ` → **sequências**, alimentam o `JokeService` (secção 9.1). Zeram quando a sequência quebra.
- `totalWins/Losses/Pushes/Blackjacks` → **acumulados da sessão**, alimentam o `stats` do `PlayerResponse` e a view Profile. Nunca zeram (exceto no `reset()`).

`registerWin()` incrementa os dois: `totalWins++` **e** `consecutiveWins++`, e põe `consecutiveLosses = 0`. Escrever um teste para isto — é o erro mais fácil de cometer.

**Regras de Blackjack fixadas (não negociar durante a implementação):**
- 6 baralhos (`deck_count=6`) — o próprio site da API diz que "Blackjack typically uses 6 decks".
- Dealer pede carta enquanto total < 17; **para em todos os 17** (incluindo soft 17 — mais simples, menos casos de teste).
- Blackjack natural paga **3:2** (aposta 100 → recebe 250 de volta). Vitória normal paga 1:1. Push devolve a aposta.
- **Sem split, double down ou insurance no MVP.** São extras.
- Ás: soma tudo a 11; enquanto `total > 21 && ases > 0`, faz `total -= 10`.

---

## 2. Estrutura de Pacotes e Ficheiros

```
Bender-s-Casino/
├── .gitignore                     ← TAREFA #1, antes de qualquer código
├── mvnw / mvnw.cmd / .mvn/        ← Maven Wrapper COMMITADO
├── pom.xml
├── docs/PLANO-MVP.md              ← este documento
└── src/
    ├── main/java/com/bendercasino/
    │   ├── BenderCasinoApplication.java
    │   ├── model/
    │   │   ├── Card.java              (record)
    │   │   ├── Hand.java
    │   │   ├── Player.java
    │   │   ├── Bet.java               (record)
    │   │   ├── GameSession.java
    │   │   ├── Deck.java              (record)
    │   │   ├── GameStatus.java  Outcome.java  JokeTrigger.java
    │   ├── service/
    │   │   ├── HandValueCalculator.java   ← função pura, ponto de entrada do TDD
    │   │   ├── BlackjackService.java
    │   │   ├── PlayerService.java
    │   │   └── JokeService.java
    │   ├── client/
    │   │   ├── DeckClient.java            (interface)
    │   │   ├── DeckOfCardsApiClient.java
    │   │   ├── InMemoryDeckClient.java
    │   │   └── dto/ DeckResponse.java  DrawResponse.java  ApiCard.java
    │   ├── repository/
    │   │   ├── InMemoryPlayerRepository.java
    │   │   └── InMemoryGameSessionRepository.java
    │   ├── controller/
    │   │   ├── PlayerController.java
    │   │   ├── BlackjackController.java
    │   │   └── SpaForwardController.java  ← ver secção 4, crítico
    │   ├── dto/
    │   │   ├── CreatePlayerRequest.java  PlayerResponse.java
    │   │   ├── StartGameRequest.java     PlayerActionRequest.java
    │   │   ├── GameStateResponse.java    HandDto.java  CardDto.java
    │   │   ├── JokeRequest.java          JokeResponse.java
    │   │   └── ErrorResponse.java
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java   (@RestControllerAdvice)
    │   │   ├── PlayerNotFoundException.java
    │   │   ├── GameNotFoundException.java
    │   │   ├── InsufficientBalanceException.java
    │   │   ├── InvalidBetException.java
    │   │   ├── InvalidGameStateException.java
    │   │   └── DeckApiException.java
    │   ├── config/ RestClientConfig.java
    │   └── util/  CardMapper.java
    ├── main/resources/
    │   ├── application.yml
    │   ├── bender-jokes.json          ← catálogo de piadas por trigger
    │   └── static/                    ← SPA (secção 4)
    └── test/java/com/bendercasino/
        ├── service/ HandValueCalculatorTest.java  BlackjackServiceTest.java
        │            JokeServiceTest.java
        ├── client/  DeckOfCardsApiClientTest.java
        └── controller/ BlackjackControllerTest.java  PlayerControllerTest.java
```

**`.gitignore` (primeiro commit, antes de tudo):**
```gitignore
# IDE
.idea/
*.iml
.vscode/
# Build
target/
*.jar
!.mvn/wrapper/maven-wrapper.jar
# SO
.DS_Store
# Local-only (não faz parte da aplicação)
.claude/
CLAUDE.md
```

---

## 3. Endpoints REST

**Base:** `http://localhost:8080`. Todos `Content-Type: application/json`.

### Jogadores

| # | Método | Caminho | Body | Resposta |
|---|---|---|---|---|
| 1 | `POST` | `/api/players` | `{"name":"Fry"}` | `201` `PlayerResponse` |
| 2 | `GET` | `/api/players/{id}` | — | `200` `PlayerResponse` |
| 3 | `GET` | `/api/players/{id}/balance` | — | `200` `{"balance":1000}` |
| 4 | `POST` | `/api/players/{id}/reset` | — | `200` `PlayerResponse` |

```jsonc
// PlayerResponse
{ "playerId": "3f1a...-b2", "name": "Fry", "balance": 1000,
  "stats": { "wins": 0, "losses": 0, "pushes": 0, "blackjacks": 0 } }
```

> `stats` mapeia diretamente os campos `totalWins/totalLosses/totalPushes/totalBlackjacks` do `Player` (secção 1) — não são as sequências.
>
> **`POST /reset`** (usado pelo botão da view Profile, tarefa C5): repõe `balance = 1000`, zera **todos** os contadores e **descarta a sessão de jogo ativa**. Sem descartar a sessão, o jogador fica com uma mão órfã e o `start` seguinte rebenta com `INVALID_GAME_STATE`. Vale 0.3h e salva a demo quando alguém ficar sem fichas ao vivo.

### Blackjack

| # | Método | Caminho | Body | Resposta |
|---|---|---|---|---|
| 5 | `POST` | `/api/blackjack/start` | `{"playerId":"...","bet":100}` | `200` `GameStateResponse` |
| 6 | `POST` | `/api/blackjack/hit` | `{"playerId":"..."}` | `200` `GameStateResponse` |
| 7 | `POST` | `/api/blackjack/stand` | `{"playerId":"..."}` | `200` `GameStateResponse` |
| 8 | `GET` | `/api/blackjack/state/{playerId}` | — | `200` `GameStateResponse` |
| 9 | `POST` | `/api/blackjack/piada` | `{"playerId":"...","trigger":"GAME_START"}` | `200` `{"joke":"..."}` |

> **Como o jogo é identificado:** o enunciado original não dizia. Fica fixado: **uma sessão ativa por jogador**, guardada num `ConcurrentHashMap<UUID, GameSession>`. O `hit`/`stand` levam apenas `playerId`. Não há `gameId` no request.

```jsonc
// GameStateResponse — a ÚNICA resposta que o front-end precisa de saber ler
{
  "gameId": "9c2e...-11",
  "playerId": "3f1a...-b2",
  "status": "PLAYER_TURN",              // PLAYER_TURN | FINISHED
  "playerHand": {
    "cards": [
      { "code":"AS", "value":"ACE", "suit":"SPADES",
        "image":"https://deckofcardsapi.com/static/img/AS.png" },
      { "code":"6H", "value":"6", "suit":"HEARTS",
        "image":"https://deckofcardsapi.com/static/img/6H.png" }
    ],
    "value": 17, "soft": true, "blackjack": false, "busted": false
  },
  "dealerHand": {
    "cards": [ { "code":"KD", "value":"KING", "suit":"DIAMONDS", "image":"..." } ],
    "value": 10, "hiddenCard": true,    // durante PLAYER_TURN a 2ª carta NÃO é enviada
    "blackjack": false, "busted": false
  },
  "bet": 100,
  "balance": 900,
  "outcome": null,                       // preenchido só quando status=FINISHED
  "payout": 0,
  "benderJoke": "Ainda estás vivo, Fry? Que desilusão.",
  "streaks": { "wins": 0, "losses": 2, "blackjacks": 0 }
}
```

> **Decisão importante:** a piada vem **inline** no `GameStateResponse`. Assim o front-end faz **1 chamada por ação**, não 2. O endpoint `POST /api/blackjack/piada` (#9) mantém-se para piadas *on-demand* (entrada no lobby, botão "provoca-me"). Ambos existem.

> **Segurança de jogo:** a carta tapada do dealer **nunca sai do servidor** enquanto `status == PLAYER_TURN`. Enviar as duas cartas e "esconder" no CSS é fazer batota visível no DevTools.

### Erros — formato único (`@RestControllerAdvice`)

```jsonc
{ "timestamp":"2026-08-24T10:31:02Z", "status":400,
  "error":"INSUFFICIENT_BALANCE",
  "message":"Fry tem 50 fichas, não pode apostar 100.",
  "path":"/api/blackjack/start" }
```

| Exceção | HTTP | `error` |
|---|---|---|
| `PlayerNotFoundException` | 404 | `PLAYER_NOT_FOUND` |
| `GameNotFoundException` | 404 | `GAME_NOT_FOUND` |
| `InvalidBetException` (aposta ≤ 0) | 400 | `INVALID_BET` |
| `InsufficientBalanceException` | 400 | `INSUFFICIENT_BALANCE` |
| `InvalidGameStateException` (hit após stand/bust, start com jogo ativo) | 409 | `INVALID_GAME_STATE` |
| `DeckApiException` | 503 | `DECK_API_UNAVAILABLE` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |

---

## 4. Front-end SPA

```
src/main/resources/static/
├── index.html            ← único HTML; <div id="app"></div> + <div id="bender-panel">
├── css/
│   ├── theme.css         ← variáveis: --neon-cyan, --neon-magenta, --metal, --felt-green
│   ├── layout.css
│   └── table.css         ← mesa, cartas, animação de distribuir
└── js/
    ├── app.js            ← bootstrap
    ├── router.js         ← History API
    ├── api.js             ← wrapper fetch + tratamento de erro
    ├── state.js          ← estado do cliente + sessionStorage
    ├── views/ home.js  lobby.js  blackjack.js  profile.js
    └── components/ card.js  bender.js  chips.js
```

**Router (History API):**
```js
const routes = { '/': home, '/lobby': lobby, '/blackjack': blackjackTable, '/profile': profile };
export function navigate(path) { history.pushState({}, '', path); render(path); }
window.addEventListener('popstate', () => render(location.pathname));
document.addEventListener('click', e => {                 // links internos
  const a = e.target.closest('a[data-link]');
  if (a) { e.preventDefault(); navigate(a.getAttribute('href')); }
});
```

**⚠️ Armadilha nº1 (custa 1h no Dia 3 se for esquecida):** com History API, um **F5 em `/lobby` dá 404** porque o Spring procura um ficheiro `lobby`. Solução — `SpaForwardController.java`:
```java
@Controller
class SpaForwardController {
    @RequestMapping({"/lobby", "/blackjack", "/profile"})
    String forward() { return "forward:/index.html"; }
}
```
(Evitar `/{path:[^\.]*}` genérico — apanharia `/api/**` também.)

**⚠️ Armadilha nº2:** o `playerId` tem de ir para `sessionStorage`. Sem isso, um F5 a meio da demo perde o jogador e o saldo.
```js
sessionStorage.setItem('playerId', id);   // ao criar
const id = sessionStorage.getItem('playerId');  // no arranque de app.js
```

**`api.js` — wrapper único:**
```js
const BASE = '/api';                                  // mesma origem → sem CORS
async function request(method, path, body) {
  const res = await fetch(BASE + path, {
    method, headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  });
  if (!res.ok) { const e = await res.json().catch(() => ({})); throw new ApiError(e); }
  return res.json();
}
export const api = {
  createPlayer: name => request('POST', '/players', { name }),
  start: (playerId, bet) => request('POST', '/blackjack/start', { playerId, bet }),
  hit:   playerId => request('POST', '/blackjack/hit',   { playerId }),
  stand: playerId => request('POST', '/blackjack/stand', { playerId }),
  state: playerId => request('GET',  `/blackjack/state/${playerId}`),
  joke:  (playerId, trigger) => request('POST', '/blackjack/piada', { playerId, trigger })
};
```

**Tema visual (cyberpunk/casino):**
```css
:root {
  --felt: #0a3d2a;  --metal: #8b95a5;  --metal-dark: #2c3440;
  --neon-cyan: #00fff2;  --neon-magenta: #ff00d4;  --gold: #ffcc33;
  --bg: #0b0e14;
}
.neon { text-shadow: 0 0 5px var(--neon-cyan), 0 0 20px var(--neon-cyan); }
.card-deal { animation: deal .35s cubic-bezier(.2,.8,.3,1); }
@keyframes deal { from { transform: translate(-40vw,-30vh) rotate(-25deg); opacity:0 } }
```
Fontes: `Orbitron` (títulos) + `Share Tech Mono` (números) via Google Fonts. Imagens das cartas vêm diretamente da URL que a API devolve — **não descarregar nada**.

---

## 5. Divisão de Tarefas (4 pessoas)

> Regra que evita 80% dos conflitos: **as interfaces e os DTOs são escritos no Dia 1, de manhã, em conjunto, e vão para o `dev` antes de qualquer implementação.** A partir daí ninguém espera por ninguém.
>
> **Todos os testes de back-end são da Pessoa A.** As restantes pessoas implementam por escrito/manualmente e não anexam testes aos seus PRs — ver a regra 2 da secção 6.

    ### 👤 Pessoa A — Domínio + TODOS os testes de back-end + **Git Master**
    | # | Tarefa | Ficheiros | Depende de |
    |---|---|---|---|
    | A1 | `.gitignore` + Maven Wrapper (as 4 máquinas têm JDKs diferentes) | raiz | — |
    | A2 | **TDD:** `HandValueCalculatorTest` → `HandValueCalculator` | `service/` | A1 |
    | A3 | `Card`, `Hand`, `Player`, `Bet`, `GameSession`, enums | `model/` | A2 |
    | A4 | Repositórios em memória (`ConcurrentHashMap`) | `repository/` | A3 |
    | A5 | **TDD:** `BlackjackServiceTest` → `BlackjackService` (start/hit/stand, dealer, payouts) | `service/` | A3, A4, **B1** |
    | A6 | **TDD:** `DeckOfCardsApiClientTest` (MockRestServiceServer) | `test/client/` | B3 |
    | A7 | **TDD:** `JokeServiceTest` (prioridade de triggers, interpolação, não repetir) | `test/service/` | B7 |
    | A8 | **TDD:** testes de controller (`@WebMvcTest` + `@MockitoBean`) | `test/controller/` | B8 |
    | A9 | Git Master: revê e faz merge de todos os PRs para `dev` | — | contínuo |

    ### 👤 Pessoa B — Back-end / API + Integração externa
    | # | Tarefa | Ficheiros | Depende de |
    |---|---|---|---|
    | B1 | **Interface `DeckClient` + DTOs da API externa** ⚠️ *primeira coisa a entrar no `dev`, desbloqueia A5* | `client/` | A1 |
    | B2 | `InMemoryDeckClient` (baralho determinístico para testes) | `client/` | B1 |
    | B3 | `RestClientConfig` + `DeckOfCardsApiClient` + `DeckApiException` | `client/`, `config/` | B1 |
    | B4 | Todos os DTOs de request/response | `dto/` | A3 |
    | B5 | `GlobalExceptionHandler` + as 6 exceções | `exception/` | B4 |
    | B6 | `PlayerService` (criar, saldo, streaks + acumulados, `reset()`). O `reset()` precisa dos **dois** repositórios: repõe o jogador **e apaga a sessão ativa**. | `service/` | A4 |
    | B7 | `JokeService` + `JokeTrigger` + carregar `bender-jokes.json` | `service/` | D1 |
    | B8 | `PlayerController` + `BlackjackController` (inclui `POST /players/{id}/reset`) | `controller/` | A5, B4, B5, B6 |

    ### 👤 Pessoa C — Front-end Lead / Estrutura + Tema
    | # | Tarefa | Ficheiros | Depende de |
    |---|---|---|---|
    | C1 | `index.html` + `theme.css` (paleta neon/metal) + fontes | `static/` | — |
    | C2 | `router.js` (History API, `popstate`, links `data-link`) | `static/js/` | C1 |
    | C3 | View **Home**: input de nome → `POST /api/players` → `/lobby` | `views/home.js` | C2, D2 |
    | C4 | View **Lobby**: mesas, saldo, botão "Jogar Blackjack" | `views/lobby.js` | C2 |
    | C5 | View **Profile**: nome, saldo, `stats`, botão reset → `POST /api/players/{id}/reset` | `views/profile.js` | C2, B7 |
    | C6 | `table.css` — mesa, slots de cartas, fichas, animação `deal` | `static/css/` | C1 |
    | C7 | `components/card.js` (`<img>` a partir da URL da API) + `chips.js` | `components/` | C6 |
    | C8 | Responsivo + estados de loading/erro | todos | C3-C7 |
    
    ### 👤 Pessoa D — Front-end / Integração + Bender
    | # | Tarefa | Ficheiros | Depende de |
    |---|---|---|---|
    | D1 | **`bender-jokes.json`** — ≥6 frases por `JokeTrigger` (conteúdo, sem código) ⚠️ *desbloqueia A8* | `resources/` | — |
    | D2 | `api.js` (wrapper fetch) + `ApiError` + `state.js` com `sessionStorage` | `static/js/` | B5 (formato dos DTOs) |
    | D3 | View **BlackjackTable**: apostar → start → hit/stand | `views/blackjack.js` | C6, D2 |
    | D4 | Render do `GameStateResponse` (mãos, valores, carta tapada) | `views/blackjack.js` | D3 |
    | D5 | `components/bender.js` — painel do Bender, efeito typewriter, fala em cada ação | `components/` | D2 |
    | D6 | Animações: distribuir, virar a carta do dealer, contador de fichas | `static/js/` | D4 |
    | D7 | Ecrãs de fim de mão (WIN/LOSE/PUSH/BLACKJACK) + "Nova mão" | `views/blackjack.js` | D4 |
    | D8 | Tratamento de erros na UI (saldo insuficiente, API de cartas em baixo) | `static/js/` | D2, B6 |
    
    **Caminho crítico:** `B1 → A5 → B8 → D3`. **B1 e D1 são as duas primeiras coisas a serem feitas no Dia 1**, porque desbloqueiam duas pessoas cada.

    **Porque é que o `JokeService` está no B e não no A:** o `JokeService` não desbloqueia ninguém — pode ser feito a qualquer hora, e não precisa de teste antes de existir (só depois). Fica no B para o A se concentrar só em `HandValueCalculator`/`BlackjackService` (o caminho crítico) e nos testes de tudo o resto. Se o A ficar sem tempo para o `JokeServiceTest` no Dia 2, escorrega para o Dia 3 sem custo — o `JokeService` já está a funcionar.
    
    ---

## 6. Estratégia de Branches

```
main            ← protegida. Só recebe merge do dev no Domingo à noite. Tag v1.0-mvp
 └─ dev         ← integração. TODOS os PRs vão para aqui.
     ├─ feat/a-dominio        (Pessoa A)
     ├─ feat/b-api            (Pessoa B)
     ├─ feat/c-ui             (Pessoa C)
     └─ feat/d-integracao     (Pessoa D)
```

**Regras (não negociáveis):**
1. **Ninguém faz push direto para `main` ou `dev`.** Sempre PR.
2. **PRs de back-end de B (`feat/b-api`) não precisam de teste anexado** — os testes de back-end são todos da Pessoa A e chegam num PR/commit seguinte, antes do merge desse trabalho para `dev`. PRs de C/D (CSS/HTML/JS) também não levam teste.
3. **PR pequeno.** Se passa de ~400 linhas, parte em dois.
4. `./mvnw test` tem de passar **localmente antes** de abrir o PR (ou, no caso de B, `./mvnw compile`). "Passa na minha máquina" não é evidência — mostra o output.
5. Todos fazem `git pull --rebase origin dev` **de manhã e antes de abrir PR**. Cinco minutos por dia poupam duas horas de merge hell no Domingo.
6. **Git Master = Pessoa A.** É a única pessoa que carrega no botão de merge. Se A estiver bloqueada, B substitui.
7. **Conventional commits:** `feat(blackjack): calcular valor de mão com ases`, `fix(deck): tratar remaining=0`, `test(joke): streak de 3 derrotas`, `chore: gitignore`.

*(Se usarem os nomes de branch que o Linear gera, definam o prefixo em Settings → e mantenham `feat/` — o Linear fecha a issue automaticamente com `Fixes BEN-12` no corpo do PR.)*

---

## 7. Plano de Sprints — 3 Dias

### 🔵 DIA 1 (Sexta) — Fundações + Núcleo de Regras
**Objetivo mensurável:** `./mvnw test` verde com as regras do Blackjack completas; SPA navega entre as 4 páginas.

| Hora | Quem | O quê |
|---|---|---|
| 0:00–0:30 | **Todos** | Reunião: ler `docs/PLANO-MVP.md`, fixar o `GameStateResponse` (secção 3). **É o contrato — a partir daqui não muda.** |
| 0:30–1:00 | A | A1 + A2 → **push direto para `dev`** (setup inicial). Todos clonam. |
| 1:00–1:30 | B, D | **B1** (interface `DeckClient`) e **D1** (`bender-jokes.json`) → PR imediato. Desbloqueiam A e B. |
| 1:00–1:30 | C | C1 (`index.html` + `theme.css`) |
| 1:30–4:00 | A | **A3 (TDD ases!)** → A4 → A5 |
| 1:30–4:00 | B | B2 → B3 → B4 (Deck API a funcionar) |
| 1:30–4:00 | C | C2 (router) → C4 (lobby) |
| 1:30–4:00 | D | D2 (`api.js`, `state.js`) |
| 4:00–7:00 | A | **A6 — `BlackjackService` completo (TDD)** ← *tarefa mais importante do dia* → **acabar com A7 (`PlayerService`)**, porque o B7 de amanhã de manhã depende dele |
| 4:00–7:00 | B | B5 (DTOs) → B6 (exceções) |
| 4:00–7:00 | C | C3 (home) → C4 (lobby, se faltou) |
| 4:00–7:00 | D | D5 (painel do Bender) + C6 a pares com C |
| Fim do dia | **Todos** | Merge para `dev`. `./mvnw test` verde. Demo interna de 5 min. |

### 🟢 DIA 2 (Sábado) — Integração
**Objetivo mensurável:** jogo completo end-to-end no browser — apostar, hit, stand, ver resultado, saldo atualiza.

| Hora | Quem | O quê |
|---|---|---|
| 0:00–0:15 | Todos | `git pull --rebase origin dev`. Bloqueios em 1 frase cada. |
| 0:15–3:00 | A | **A8 + A9 (`JokeService` + testes)**. Revisão de PRs. Se o B travar no B7, largar as piadas e ajudar. |
| 0:15–3:00 | B | **B7 (controllers)** → B8 — *desbloqueado pelo A7, entregue ontem* |
| 0:15–3:00 | C | C6 + C7 (mesa e cartas) |
| 0:15–3:00 | D | D3 + D4 (ligar a mesa à API) |
| **3:00** | **Todos** | 🔴 **PONTO DE INTEGRAÇÃO — parar tudo.** Front + back a falar. Jogar uma mão de verdade, os 4 à volta do mesmo ecrã. |
| 3:00–6:00 | B | Ligar o `JokeService` ao `GameStateResponse`; rever endpoints à mão |
| 3:00–6:00 | A | Testes de integração; casos-limite (saldo 0, deck esgotado) |
| 3:00–6:00 | C | C5 (profile + reset, já com o endpoint pronto) → C8 (responsivo, loading/erro) |
| 3:00–6:00 | D | D6 (animações) + D7 (ecrãs de fim de mão) |
| 6:00–7:00 | Todos | Piadas do Bender ligadas ao fluxo. **Merge. Freeze de features.** |

### 🟡 DIA 3 (Domingo) — Polimento e Entrega
**Objetivo:** demo à prova de bala. **Nenhuma funcionalidade nova depois das 15h.**

| Hora | Quem | O quê |
|---|---|---|
| Manhã | Todos | **Caça aos bugs.** Cada pessoa joga 10 mãos e escreve o que partiu. Corrigir por gravidade. |
| Manhã | D | D8 (erros na UI) + F5 em todas as rotas |
| Manhã | C | Polimento visual, contraste, ecrãs vazios |
| Meio-dia | A | `./mvnw clean verify`, README com instruções de correr |
| Meio-dia | B | Rever todos os endpoints à mão (`curl`) |
| **15:00** | **Todos** | 🔴 **FEATURE FREEZE.** Merge `dev` → `main`. Tag `v1.0-mvp`. |
| Tarde | Todos | Ensaiar a demo **2 vezes**, cronometrada. Guião escrito. |
| Tarde | — | *Se e só se sobrar tempo:* Dockerfile, deploy Koyeb, 1 teste Playwright, esboço de Roleta |

---

## 8. Board — Linear (e mapeamento para Trello)

| Coluna Trello | Estado Linear | Significado |
|---|---|---|
| Backlog | `Backlog` | Extras pós-MVP |
| To Do | `Todo` | Neste sprint, ninguém a trabalhar |
| Doing | `In Progress` | **Máx. 1 por pessoa** (limite WIP) |
| Testing | `In Review` | PR aberto / a testar |
| Done | `Done` | Merged em `dev` + testes verdes |

**Onde é que cada coisa começa:** todas as issues das três tabelas seguintes entram em **To Do** no arranque do Dia 1. A lista final ("Backlog pós-MVP") é a única que fica em **Backlog** — e não se toca nela antes de Domingo às 15h.

### Issues (estimativas em horas)

**Infra & Setup**
| Issue | Pessoa | Est. | Labels |
|---|---|---|---|
| `.gitignore` + Maven Wrapper | A | 0.5h | `infra` |
| Setup do workspace Linear | A | 0.5h | `infra` |
| Copiar plano para `docs/PLANO-MVP.md` | A | 0.2h | `docs` |

**Back-end**
| Issue | Pessoa | Est. | Labels |
|---|---|---|---|
| Interface `DeckClient` + DTOs ⚠️ *bloqueia A6* | B | 0.5h | `backend` `deck-api` |
| `HandValueCalculator` (TDD, ases) | A | 1.5h | `backend` |
| Modelos de domínio + enums | A | 1h | `backend` |
| Repositórios em memória | A | 0.5h | `backend` |
| `InMemoryDeckClient` | B | 1h | `backend` `deck-api` |
| `DeckOfCardsApiClient` + RestClient + timeouts | B | 1.5h | `backend` `deck-api` |
| Teste do cliente (MockRestServiceServer) | B | 1h | `backend` `deck-api` |
| **`BlackjackService` (TDD) — caminho crítico** | A | 3h | `backend` |
| `PlayerService` (criar, saldo, reset, contadores) | A | 1h | `backend` |
| `JokeService` + prioridade de triggers | A | 2h | `backend` `jokes` |
| `JokeServiceTest` | A | 1h | `backend` `jokes` |
| DTOs de request/response | B | 1h | `backend` |
| Exceções + `GlobalExceptionHandler` | B | 1h | `backend` |
| Controllers REST (inclui `POST /players/{id}/reset`) | B | 2h | `backend` |
| Testes de controller (`@WebMvcTest`) | B | 1.5h | `backend` |

**Front-end**
| Issue | Pessoa | Est. | Labels |
|---|---|---|---|
| `bender-jokes.json` (≥6 por trigger) ⚠️ *bloqueia A8* | D | 1h | `jokes` |
| `index.html` + tema neon/metal | C | 1.5h | `frontend` |
| Router History API | C | 1.5h | `frontend` |
| `SpaForwardController` (F5 nas rotas) | C | 0.3h | `backend` `frontend` |
| View Home (criar jogador) | C | 1h | `frontend` |
| View Lobby | C | 1h | `frontend` |
| View Profile | C | 1h | `frontend` |
| CSS da mesa + animação de distribuir | C | 2h | `frontend` |
| Componentes card + chips | C | 1.5h | `frontend` |
| Responsivo + loading/erro | C | 1.5h | `frontend` |
| `api.js` + `state.js` + `sessionStorage` | D | 1.5h | `frontend` |
| View BlackjackTable (aposta + ações) | D | 2h | `frontend` |
| Render do `GameStateResponse` | D | 2h | `frontend` |
| Painel do Bender + typewriter | D | 1.5h | `frontend` `jokes` |
| Animações (distribuir, virar, fichas) | D | 2h | `frontend` |
| Ecrãs de fim de mão | D | 1.5h | `frontend` |
| Tratamento de erros na UI | D | 1h | `frontend` |

**Backlog (pós-MVP):** Roleta · Slots · Dockerfile + deploy Koyeb · teste E2E Playwright · piadas por IA com moderação · persistência JPA/H2 · split/double/insurance · tabela de recordes.

**Total ≈ 46h ÷ 4 pessoas ≈ 11.5h cada em 3 dias.** Somando a tabela: **A ≈ 13h** (e ainda é Git Master, papel não estimado) · **B ≈ 9.5h** · **C ≈ 11.5h** · **D ≈ 12.5h**.

O B tem folga **de propósito** — carrega o caminho crítico e é quem mais provavelmente derrapa. **Válvula de escape:** se ao fim do Dia 1 o A já passou das 12h reais, o `JokeService` (A8/A9) volta para o B. Não desbloqueia ninguém — é o amortecedor do plano. É apertado — por isso o feature freeze às 15h de Domingo não é uma sugestão.

### Setup do Linear (10 minutos, faz-se uma vez)

1. **Workspace:** `Bender's Casino`. Plano Free chega e sobra (membros ilimitados, 2 equipas, 250 issues; temos ~40).
2. **1 equipa** — nome `Bender`, identificador **`BEN`** (as issues ficam `BEN-1`, `BEN-2`...). **Não criem 4 equipas** por pessoa — o trabalho é o mesmo produto; as pessoas separam-se por *assignee*, não por equipa.
3. **1 projeto:** `Blackjack MVP`, target date = segunda-feira.
4. **Cycles de 1 dia** (Settings → Cycles → duração 1 semana **não** serve aqui): criem 3 cycles manuais `Dia 1`, `Dia 2`, `Dia 3`. Se o Linear não deixar cycles de 1 dia no plano Free, usem **3 Milestones** dentro do projeto — dá o mesmo resultado.
5. **Labels (6, não mais):** `backend` `frontend` `deck-api` `jokes` `infra` `docs`. Resistam a criar 20 labels — ninguém os mantém.
6. **Estados do workflow:** renomear para bater certo com a tabela acima (`In Review` em vez de `In Review/QA`).
7. **Integração GitHub:** Settings → Integrations → GitHub. Ativa "Link pull requests". Escrever `Fixes BEN-12` no corpo do PR fecha a issue no merge. É a única automação de que precisam.
8. **Triage:** Pessoa A é responsável. Bug encontrado → issue no Triage → A prioriza.
9. **Ritual diário (5 min):** cada pessoa arrasta as suas issues e diz 1 frase: *feito ontem / hoje / bloqueado em*.

---

## 9. Dicas de Implementação

### 9.1 Piadas do Bender — onde e quando

O `JokeService` tem **dois métodos**, e a diferença entre eles é o teste do A9:

```java
// 1. Usado pelo BlackjackService a cada ação. O SERVIÇO resolve o trigger
//    a partir do estado do jogador (escada abaixo). outcome pode ser null (no start).
String jokeFor(Player player, Outcome outcome);

// 2. Usado pelo endpoint #9 (piada on-demand). Trigger explícito, SEM escada.
String jokeFor(Player player, JokeTrigger trigger);
```

Se o trigger fosse sempre um parâmetro de entrada, **a prioridade não era testável** — e é precisamente isso que o `JokeServiceTest` (A9) tem de verificar.

**Prioridade dos triggers** — resolvida dentro do método 1, o primeiro que der match ganha:

```
1. DOUBLE_BLACKJACK   consecutiveBlackjacks >= 2   → surpresa/inveja
2. LOSING_STREAK      consecutiveLosses    >= 3    → gozo
3. WINNING_STREAK     consecutiveWins      >= 3    → arrogância / acusação de batota
4. BROKE              balance == 0                 → despedida cruel
5. Outcome da mão     BLACKJACK/WIN/LOSE/BUST/PUSH
6. GAME_START                                      → provocação genérica
```

**Formato do `bender-jokes.json`** (`{player}` é interpolado com o nome):
```json
{
  "LOSING_STREAK": [
    "Três seguidas, {player}. Já vi robôs de lavar loiça com melhor sorte.",
    "{player}, o teu problema não é a sorte. És tu.",
    "Continua assim e compro-me uma perna nova com as tuas fichas."
  ],
  "DOUBLE_BLACKJACK": [
    "Dois blackjacks seguidos?! {player}, ou tens sorte ou tens uma prótese como a minha.",
    "Ok, {player}, agora estou desconfiado. E eu SEI como é fazer batota."
  ],
  "GAME_START": ["Aposta lá, {player}. Não tenho o dia todo — tenho, mas não te interessa."]
}
```

**Regras do serviço** (todas testáveis, todas sem dependências → é o melhor trabalho paralelo do projeto):
- Escolha aleatória dentro da categoria, **nunca repetir a última frase** desse jogador (guardar `lastJokeIndex` por trigger).
- Se `{player}` não existir na frase, usar na mesma — a interpolação é opcional.
- Se a categoria estiver vazia, cair para `GAME_START`. **Nunca lançar exceção** — uma piada em falta não pode partir o jogo.

**No front-end:** o `bender.js` lê `response.benderJoke` a cada ação e faz *typewriter*. Zero chamadas extra.

### 9.2 Comunicação com a Deck of Cards API

**Endpoints que usamos — só dois:**
```
GET https://deckofcardsapi.com/api/deck/new/shuffle/?deck_count=6
GET https://deckofcardsapi.com/api/deck/{deck_id}/draw/?count={n}
```

**Regras fixadas:**
1. **NUNCA usar `jokers_enabled=true`.** Devolve cartas com `value: "JOKER"` e envenena o cálculo do valor da mão.
2. **NÃO usar a API de piles.** A documentação diz explicitamente *"This will not work with multiple decks"* e nós usamos 6. As mãos vivem em memória no servidor. Menos chamadas, menos bugs.
3. **Distribuir com UMA chamada `count=4`** (2 jogador + 2 dealer), não quatro chamadas. Cada `hit` é `count=1`. Menos round-trips = demo que não engasga.
4. **Deck esgotado:** se `remaining < 15`, criar baralho novo antes da mão seguinte. Sem esta regra, a ronda N da demo rebenta.
5. `value` é **String**, não número: `"2".."10"`, `"JACK"`, `"QUEEN"`, `"KING"`, `"ACE"`. Um único método `Card.points()` faz o mapeamento. Tudo o resto usa esse método.
6. Baralhos morrem ao fim de 2 semanas sem uso — irrelevante para nós, mas não guardar `deck_id` em lado nenhum persistente.

**Configuração (`application.yml`):**
```yaml
spring:
  http:
    clients:
      connect-timeout: 2s
      read-timeout: 3s
deckofcards:
  mode: api                 # api | memory  ← escape hatch da demo (secção 1)
  base-url: https://deckofcardsapi.com/api/deck
  deck-count: 6
  reshuffle-threshold: 15
```

**Tratamento de erros:** `DeckOfCardsApiClient` apanha `RestClientException` / `success: false` e lança `DeckApiException` → `503 DECK_API_UNAVAILABLE`. O front mostra *"O Bender foi buscar baralhos novos. Tenta outra vez."* **Não deixar `HttpClientErrorException` chegar ao utilizador.**

### 9.3 Como testar (TDD)

**Ordem obrigatória — RED → GREEN → COMMIT:**

**Passo 1 — `HandValueCalculatorTest`** (função pura, sem Spring, sem rede — 30 segundos por ciclo). É aqui que os projetos de Blackjack falham; escrevam estes testes **primeiro**, todos:

| Teste | Esperado |
|---|---|
| mão vazia | 0 |
| `2♠ + 3♥` | 5 |
| `K♦ + Q♣` | 20 |
| `A♠ + K♥` | 21, `isBlackjack() == true` |
| `A♠ + A♥ + 9♣` | **21** (11+1+9) ← o clássico |
| `A♠ + A♥ + A♦ + 8♣` | 21 |
| `A♠ + 6♥` | 17, `isSoft() == true` |
| `A♠ + 6♥ + K♦` | 17, `isSoft() == false` |
| `A♠ + A♥` | 12, soft |
| `K♠ + Q♥ + 2♣` | 22, `isBusted() == true` |
| `5♠+5♥+5♦+5♣+A♠` | 21 |
| `10 + 9 + 2` (21 em 3 cartas) | 21, `isBlackjack() == false` ← **não é blackjack natural** |

**Passo 2 — `BlackjackServiceTest`** com `InMemoryDeckClient` (baralho fixo, resultado determinístico — **nunca chamar a API real num teste unitário**):
- start distribui 2+2; a 2ª do dealer não sai na resposta
- blackjack natural → payout `bet * 2.5`, saldo confere
- blackjack dos dois → `PUSH`, aposta devolvida
- hit até rebentar → `PLAYER_BUST`, aposta perdida
- stand → dealer pede até ≥17 e para
- dealer rebenta → `DEALER_BUST`, payout `bet * 2`
- totais iguais → `PUSH`
- aposta > saldo → `InsufficientBalanceException`
- aposta ≤ 0 → `InvalidBetException`
- hit com jogo terminado → `InvalidGameStateException`
- start com jogo já ativo → `InvalidGameStateException`

**Passo 3 — `DeckOfCardsApiClientTest`** com `MockRestServiceServer`: JSON gravado da API real → assert do mapeamento; e um caso `500` → `DeckApiException`.

**Passo 4 — Controllers:** `@WebMvcTest` + `@MockitoBean` (⚠️ **não `@MockBean`** — foi removido no Boot 4). Verificar códigos HTTP e o formato do `ErrorResponse`.

**Passo 5 — Front-end:** manual. Checklist no Dia 3: F5 em cada rota · saldo insuficiente · backend desligado · janela a 375px de largura.

---

## 10. ⚠️ Armadilhas do Spring Boot 4 (ler ANTES de escrever código)

O Boot 4 mudou coisas que **invalidam quase tudo o que vão encontrar no Google e no StackOverflow** (que é 3.x). Cinco minutos aqui poupam horas:

| O que o Google diz (3.x) | O que é preciso no Boot 4.1 |
|---|---|
| `spring-boot-starter-web` | **`spring-boot-starter-webmvc`** (o antigo funciona mas está deprecated) |
| `RestTemplate` sem starter | precisa de **`spring-boot-starter-restclient`** |
| Validação vem com o web starter | precisa de **`spring-boot-starter-validation`** explícito |
| `com.fasterxml.jackson.*` | **Jackson 3** → `tools.jackson.*` (exceto `jackson-annotations`) |
| `@JsonComponent` | `@JacksonComponent` |
| Bean `ObjectMapper` para configurar Jackson | já não chega — definir bean **`JsonMapper`** |
| `@MockBean` / `@SpyBean` | **`@MockitoBean` / `@MockitoSpyBean`** |
| `@SpringBootTest` dá MockMvc | **não dá** — juntar `@AutoConfigureMockMvc` |
| Undertow como servidor | removido (Servlet 6.1) — usar Tomcat (default) |

**`pom.xml` de referência:**
```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>4.1.1</version>
</parent>
<properties><java.version>21</java.version></properties>
<dependencies>
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-restclient</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
</dependencies>
```

**Regra de ouro:** perante qualquer dúvida de API do Spring, ir direto à documentação oficial (`docs.spring.io`, versão 4.x) em vez de Google — a maioria dos resultados que aparecem no Google é 3.x e está errada para este projeto.

---

## 11. Verificação — como sabemos que está feito

**Back-end:**
```bash
./mvnw clean test          # todos verdes, incl. os 12 casos de valor de mão
./mvnw spring-boot:run     # arranca na 8080
```

**Fluxo end-to-end por `curl`** (ponham isto num `scripts/smoke.sh`):
```bash
PID=$(curl -s -X POST localhost:8080/api/players \
      -H 'Content-Type: application/json' -d '{"name":"Fry"}' | jq -r .playerId)
curl -s localhost:8080/api/players/$PID/balance            # {"balance":1000}
curl -s -X POST localhost:8080/api/blackjack/start -H 'Content-Type: application/json' \
     -d "{\"playerId\":\"$PID\",\"bet\":100}" | jq
curl -s -X POST localhost:8080/api/blackjack/hit   -H 'Content-Type: application/json' -d "{\"playerId\":\"$PID\"}" | jq
curl -s -X POST localhost:8080/api/blackjack/stand -H 'Content-Type: application/json' -d "{\"playerId\":\"$PID\"}" | jq '.outcome, .payout, .benderJoke'
# caso de erro:
curl -s -X POST localhost:8080/api/blackjack/start -H 'Content-Type: application/json' \
     -d "{\"playerId\":\"$PID\",\"bet\":999999}" | jq   # 400 INSUFFICIENT_BALANCE
# reset (usado pelo botão da view Profile):
curl -s -X POST localhost:8080/api/players/$PID/reset | jq  # balance 1000, stats a zeros
```

**Plano B da demo — testar no Dia 3, não em cima da hora.** Depois do `./mvnw clean verify` já existe o `.jar`:
```bash
java -jar target/*.jar --deckofcards.mode=memory
```
Jogar uma mão completa assim. Se funcionar, a API externa deixa de ser um risco de entrega.
*(Durante o desenvolvimento, sem `.jar`: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--deckofcards.mode=memory`.)*

**Front-end — checklist manual (Dia 3):**
- [ ] `localhost:8080` → Home; escrever nome → Lobby com 1000 fichas
- [ ] Lobby → mesa de Blackjack; apostar 100 → 2 cartas visíveis + 1 do dealer tapada
- [ ] Hit → carta nova, valor atualiza; rebentar → mensagem correta
- [ ] Stand → dealer vira, pede até ≥17, resultado e saldo corretos
- [ ] Bender fala em **todas** as ações; 3 derrotas seguidas → frase de gozo
- [ ] **F5 em `/lobby`, `/blackjack`, `/profile`** → não dá 404 e o jogador mantém-se
- [ ] Apostar mais do que o saldo → erro tratado, sem crash
- [ ] Profile mostra `stats` corretos após 3 mãos; botão **reset** repõe 1000 fichas e deixa jogar logo a seguir
- [ ] `java -jar target/*.jar --deckofcards.mode=memory` arranca e joga uma mão (plano B da demo)
- [ ] Backend desligado → mensagem amigável, sem ecrã branco
- [ ] Janela a 375px → jogável

**Definition of Done por issue:** teste escrito e a passar · PR revisto por A · merged em `dev` · issue arrastada para Done no Linear.

---

## Riscos e mitigações

| Risco | Probabilidade | Mitigação |
|---|---|---|
| Lógica dos ases mal feita | **Alta** | 12 testes escritos **primeiro**, no Dia 1 de manhã |
| Deck of Cards API em baixo na demo | Baixa | `--deckofcards.mode=memory` na linha de comandos. **Sem editar código, sem recompilar.** Ensaiar no Dia 3. |
| Front e back desalinhados | **Alta** | `GameStateResponse` congelado na 1ª reunião; DTOs no `dev` antes de qualquer implementação |
| Merge hell no Domingo | Média | `pull --rebase` 2x/dia; PRs pequenos; Git Master único |
| Armadilhas do Boot 4 a queimar horas | **Alta** | Secção 10 + documentação oficial, nunca de memória |
| F5 dá 404 na demo | Média | `SpaForwardController` no Dia 1 (0.3h) |
| Âmbito a crescer (Roleta, Slots, deploy) | **Alta** | Backlog explícito + feature freeze às 15h de Domingo |
</content>
