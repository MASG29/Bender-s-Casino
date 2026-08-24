# Bender's Casino — Tarefas Back-end

> Contrato REST, modelos e regras de negócio combinados em equipa no Dia 1 — ver `GameStateResponse` e `PlayerResponse` no código (`dto/`) como fonte de verdade.

**Regra da divisão:** o Marco escreve todos os testes de back-end (é também o Git Master); o Diogo implementa as suas tarefas sem anexar testes — ver regra 2 da secção 6 do plano.

## Estado atual

O commit inicial do esqueleto já entregou (a preencher só o que falta, marcado abaixo):

- ✅ Feito: `model/` (Card, Hand, Player, Bet, GameSession, enums), `dto/` (DTOs de request/response), `exception/` (GlobalExceptionHandler + 6 exceções), `repository/` (repositórios em memória), `client/InMemoryDeckClient`, `client/dto/`, `controller/SpaForwardController`, `util/CardMapper`.
- 🔲 Por fazer: tudo o que está marcado `TODO` no código — ver tabelas abaixo.

## Marco — Domínio + Testes + Git Master

| # | Tarefa | Ficheiros | Depende de |
|---|---|---|---|
| A1 | `.gitignore` + Maven Wrapper | raiz | — |
| A2 | **TDD:** `HandValueCalculatorTest` (já existe) → implementar `HandValueCalculator` | `service/` | A1 |
| A3 | Modelos de domínio + enums | `model/` | ✅ feito |
| A4 | Repositórios em memória | `repository/` | ✅ feito |
| A5 | **TDD:** `BlackjackServiceTest` → implementar `BlackjackService` (start/hit/stand, dealer, payouts) — caminho crítico | `service/` | A3, A4, B1 |
| A6 | **TDD:** `DeckOfCardsApiClientTest` (MockRestServiceServer) | `test/client/` | B3 |
| A7 | **TDD:** `JokeServiceTest` (prioridade de triggers, interpolação, não repetir) | `test/service/` | B7 |
| A8 | **TDD:** testes de controller (`@WebMvcTest` + `@MockitoBean`) | `test/controller/` | B8 |
| A9 | Git Master: revê e faz merge de todos os PRs para `dev` | — | contínuo |

## Diogo — API + Integração externa

| # | Tarefa | Ficheiros | Depende de |
|---|---|---|---|
| B1 | Interface `DeckClient` + DTOs da API externa ⚠️ *desbloqueia A5* | `client/` | ✅ feito |
| B2 | `InMemoryDeckClient` | `client/` | ✅ feito |
| B3 | `RestClientConfig` + implementar `DeckOfCardsApiClient` (chamadas reais à Deck of Cards API) | `client/`, `config/` | B1 |
| B4 | DTOs de request/response | `dto/` | ✅ feito |
| B5 | `GlobalExceptionHandler` + 6 exceções | `exception/` | ✅ feito |
| B6 | Implementar `PlayerService` (criar, saldo, streaks, `reset()`). O `reset()` usa os dois repositórios: repõe o jogador e apaga a sessão ativa | `service/` | A4 |
| B7 | Implementar `JokeService` + `JokeTrigger` + carregar `bender-jokes.json` | `service/` | D1 |
| B8 | Implementar `PlayerController` + `BlackjackController` (inclui `POST /players/{id}/reset`) | `controller/` | A5, B4, B5, B6 |

## Caminho crítico

`B1 → A5 → B8 → D3` (D3 é do front-end, ver `BlackJack-FrontEnd.md`).

## Branches

```
main
 └─ dev
     ├─ feat/a-dominio   (Marco)
     └─ feat/b-api       (Diogo)
```

PRs de `feat/b-api` não precisam de teste anexado — os testes chegam num PR seguinte do Marco antes do merge para `dev`. `./mvnw test` (Marco) ou `./mvnw compile` (Diogo) tem de passar localmente antes de abrir PR.


---

> Isto é o MVP do blackjack (Fase 1). Para a Fase 2 — jogos novos, login e núcleo de jogo —
> ver [../Fase2-Jogos.md](../Fase2-Jogos.md) e [../AGORA.md](../AGORA.md).
