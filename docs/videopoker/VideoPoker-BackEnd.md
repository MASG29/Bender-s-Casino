# Video Poker (Poker Machine) — Back-end (V)

**Dupla: Eddie + Tiago Paulos.** A divisão back/front dentro da dupla ainda está por decidir.
Branch: `feat/videopoker`.

Jacks or Better, 5 cartas, uma troca. Usa o `DeckClient` que já existe.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| V-B1 | DTOs | `DealResponse` (5 cartas, `handId`), `DrawRequest` (quais as cartas a manter), `DrawResponse` (mão final, categoria, prémio, saldo). | — |
| V-B2 | `PokerHandEvaluator` (TDD) | Função pura: 5 cartas → categoria (par de valetes ou melhor, dois pares, trio, straight, flush, full house, poker, straight flush, royal flush). É a peça mais delicada do jogo — testes primeiro, incluindo o ás nas duas pontas do straight. | — |
| V-B3 | Tabela de prémios | Jacks or Better, prémio em função da aposta. Separado do avaliador. | V-B2 |
| V-B4 | `VideoPokerService` | Dá 5 cartas via `DeckClient`, guarda o estado até à troca, substitui as descartadas, avalia, paga. | V-B3, E2 |
| V-B5 | Controller | Endpoints de deal e draw sob o despacho genérico do E3. Validar que o índice das cartas a manter é válido e que não se troca duas vezes. | V-B4, E3 |

> **Arranca já, sem esperar pelo E:** V-B1 e V-B2 não tocam em Spring nem no `GameSession`.
