# Peixinho (À Pesca / Go Fish) — Back-end (X)

**Back-end: Diogo.** Branch: `feat/peixinho`.

## Regras base

Baralho de 52 cartas. Cada jogador recebe uma mão inicial. Na sua vez, um jogador pede a outro
todas as cartas de um determinado valor (ex.: "tens noves?") — **só pode pedir um valor que já
tenha na mão**. Se o adversário tiver, entrega todas e o pedinte joga outra vez. Se não tiver,
diz "vai à pesca" e o pedinte tira uma carta do monte. Quem juntar as 4 cartas do mesmo valor
baixa o conjunto. O jogo acaba quando os 13 conjuntos estiverem baixados; ganha quem tiver mais.

## X-B0 — Adaptação a casino (decidir primeiro)

Este é o único jogo da lista que não é naturalmente de casino: não tem casa, não tem aposta
natural e uma partida completa pode demorar 5–15 minutos. Antes de escrever código, fechar:

1. **Modelo de aposta** — aposta única no início e prémio ao vencedor? Ou valor por conjunto baixado?
2. **Adversário** — bot, ou o "dealer" a jogar com as mesmas regras?
3. **Duração** — partida completa (13 conjuntos) ou versão curta (primeiro a N conjuntos)?

Sem estas três respostas, X-B1 a X-B5 não se conseguem especificar.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| X-B1 | DTOs | Mão do jogador, conjuntos baixados de cada lado, cartas restantes no monte, de quem é a vez, resultado do último pedido. **Restrição forte: a mão do adversário nunca pode aparecer na resposta** — só o número de cartas. Um jogo de informação escondida perde-se todo se o estado sair no JSON. | X-B0 |
| X-B2 | Regras puras (TDD) | Pedido válido (o valor tem de estar na mão de quem pede), transferência de cartas, deteção de conjunto de 4, condição de fim, contagem final. Testes primeiro. | X-B0 |
| X-B3 | Bot | Decide o pedido **usando só o que um jogador veria**: a própria mão, os conjuntos baixados e o histórico de pedidos. Não pode espreitar a mão do jogador. | X-B2 |
| X-B4 | `PeixinhoService` | Estado da partida entre pedidos, o baralho vem do `DeckClient`, integra o bot, resolve aposta no fim. | X-B3, E2 |
| X-B5 | Controller | Endpoints de começar partida e de fazer um pedido, sob o despacho genérico do E3. | X-B4, E3 |

> Nota: com 13 valores repartidos por 2 jogadores não há empate possível no total de conjuntos
> — não é preciso um `PUSH` como no blackjack.
