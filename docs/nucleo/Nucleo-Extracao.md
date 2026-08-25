# Núcleo de jogo — extração (E)

**Dono: Marco.** Branch: `feat/nucleo`.

Todo o código atual tem forma de blackjack: o `GameSession` tem `playerHand`/`dealerHand`/`Bet`,
o `BlackjackController` está colado ao `/api/blackjack`, e o `SpaForwardController` tem a lista
de rotas escrita à mão. Se os quatro jogos forem escritos contra isto tal como está, ficamos com
quatro cópias incompatíveis do mesmo modelo e conflitos nos mesmos ficheiros.

Estas tarefas não são um jogo — são a base que os jogos usam. Entram no `dev` **um PR de cada
vez**, porque todas tocam em ficheiros partilhados.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| E0 | ✅ Testes de controller (A8) | `@WebMvcTest` + `@MockitoBean` para `BlackjackController` e `PlayerController`. É a rede de segurança: sem isto não há como provar que o refactor não partiu o blackjack. | — |
| E1 | `GameSession` genérico | Tirar `playerHand`/`dealerHand`/`outcome` do `GameSession` e deixar `gameId`, `playerId`, `game` (nome), `bet`, `status`, `state` (estado específico do jogo). O estado de blackjack passa a ser um tipo próprio. | E0 |
| E2 | Interface `GameService` | `start(playerId, bet)`, `act(gameId, action, payload)`, `state(gameId)`. `BlackjackService` passa a implementá-la sem mudar de comportamento. | E1 |
| E3 | Rotas `/api/games/{game}/...` | Um controller genérico que despacha para o `GameService` certo pelo nome do jogo. **Manter `/api/blackjack` como alias** para o front-end atual não partir. | E2 |
| E4 | `SpaForwardController` por padrão | Hoje: `@RequestMapping({"/lobby", "/blackjack", "/profile"})`. Cada jogo novo que se esqueça de acrescentar aqui dá 404 no F5. Trocar por um padrão que apanhe as rotas do SPA. | — |

## Regras

- **E0 primeiro.** Não se refactoriza o `BlackjackController` sem testes.
- **E4 é independente** — pode ir num PR à parte, a qualquer altura.
- Os serviços e controllers dos jogos novos (S-B3/S-B4, X-B4/X-B5, V-B4/V-B5, R-B3/R-B4)
  dependem do E2/E3. **O que não depende:** DTOs e funções puras testadas em TDD
  (`SlotPaytable`, `PokerHandEvaluator`, `RoulettePayout`, regras do Peixinho) — isso arranca já.
