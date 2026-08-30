# Núcleo de jogo — front-end (E-F)

**Dono: Marco.** Branch: `feat/nucleo`.

O mesmo problema do lado do cliente: o `routes.js` tem as rotas escritas à mão e todo o
`views/pages/blackjack.js` + `table.css` está feito à medida do blackjack. Quatro jogos escritos
por cima disto dão quatro cópias da mesa e conflitos nos mesmos ficheiros.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| E-F1 | ✅ Rotas a partir de uma lista de jogos | O `routes.js` passa a gerar as rotas de jogo a partir de uma lista única, em vez de as ter escritas uma a uma. | — |
| E-F2 | ❌ Descartado | Ideia original: extrair de `table.css`/`blackjack.js` uma shell de mesa reutilizável. Descartado depois da roleta mostrar, na prática, que os jogos são visualmente distintos demais para partilhar layout (a roleta construiu `roulette.js`/`roulette.css` totalmente à parte, sem herdar nada do blackjack, e funciona bem). Cada jogo constrói a sua página/CSS própria; só se reaproveita manualmente o que fizer sentido (tapete, chip rail, linha de saldo/aposta) copiando, não herdando de um componente partilhado. | — |
| E-F3 | ✅ Lobby dinâmico | O lobby lista os jogos a partir da mesma lista do E-F1, com o Bar à parte (não é jogo). | E-F1 |

> Lembrete: cada rota nova de SPA precisa de estar coberta pelo `SpaForwardController` (E4),
> senão dá 404 no F5 — é a mesma armadilha que já está documentada no blackjack.
