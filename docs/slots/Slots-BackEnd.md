# Slots — Back-end (S)

**Dupla: Eddie + Tiago Paulos.** A divisão back/front dentro da dupla ainda está por decidir.
Branch: `feat/slots`.

Não usa o `DeckClient` (tal como a Roleta) — é RNG puro.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| S-B1 | DTOs | `SpinRequest` (`playerId`, `bet`) e `SpinResponse` (símbolos dos rolos, linhas ganhas, prémio, saldo, eventos). Congelar com o front-end antes de implementar. | — |
| S-B2 | `SlotPaytable` (TDD) | Função pura: combinação de símbolos → prémio. Testes primeiro, como no `HandValueCalculator`. Define aqui os símbolos e a raridade de cada um. | — |
| S-B3 | `SlotMachineService` | Sorteia os rolos, aplica a `SlotPaytable`, debita/credita. **`Random` injetado por construtor** para os testes serem determinísticos. | S-B2, E2 |
| S-B4 | Controller + eventos | Endpoint de spin sob o despacho genérico do E3. Devolver eventos tipo `JACKPOT` / `NEAR_MISS` para o front-end animar. | S-B3, E3 |

> **Arranca já, sem esperar pelo E:** S-B1 e S-B2 não tocam em Spring nem no `GameSession`.
