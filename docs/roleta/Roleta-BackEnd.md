# Roleta MVP — Back-end (R)

**Back-end: Marco.** Branch: `feat/roleta`.

**MVP é só preto ou vermelho.** Sem apostas em números, sem dúzias, sem colunas, sem split.
Uma aposta, dois resultados possíveis, paga 1:1.

Não usa o `DeckClient` (tal como os Slots) — é RNG puro.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| R-B1 | DTOs | `RouletteSpinRequest` (`playerId`, `bet`, `colour`) e `RouletteSpinResponse` (`number`, `colour`, `won`, `payout`, `balance`). Congelar com o front-end antes de implementar. | — |
| R-B2 | `RoulettePayout` (TDD) | Função pura: número 0–36 → cor, e (cor apostada, cor saída) → ganho. Tabela europeia: 1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36 são vermelhos, os outros de 1 a 36 são pretos. **O zero é verde e perde sempre — é essa a vantagem da casa.** Escrever o teste primeiro, como no `HandValueCalculator`. | — |
| R-B3 | `RouletteService` | Debita a aposta, sorteia 0–36, aplica o `RoulettePayout`, credita. **`Random` injetado por construtor** para o teste poder fixar a semente. | R-B2, E2 |
| R-B4 | `RouletteController` | Endpoint de spin sob o despacho genérico do E3. Validar que o `colour` é `RED` ou `BLACK` e que o jogador tem saldo. | R-B3, E3 |
