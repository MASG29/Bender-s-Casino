# Núcleo de jogo — front-end (E-F)

**Dono: Marco.** Branch: `feat/nucleo`.

O mesmo problema do lado do cliente: o `routes.js` tem as rotas escritas à mão e todo o
`views/pages/blackjack.js` + `table.css` está feito à medida do blackjack. Quatro jogos escritos
por cima disto dão quatro cópias da mesa e conflitos nos mesmos ficheiros.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
<<<<<<< HEAD
<<<<<<< HEAD
| E-F1 | Rotas a partir de uma lista de jogos | O `routes.js` passa a gerar as rotas de jogo a partir de uma lista única, em vez de as ter escritas uma a uma. | — |
| E-F2 | Shell de mesa reutilizável | Extrair de `table.css` e `views/pages/blackjack.js` a parte comum (tapete, zona de saldo, zona de aposta, painel de resultado) para os outros jogos herdarem. O blackjack passa a usar a shell. | — |
| E-F3 | Lobby dinâmico | O lobby lista os jogos a partir da mesma lista do E-F1, com o Bar à parte (não é jogo). | E-F1 |
=======
| E-F1 | ✅ Rotas a partir de uma lista de jogos | O `routes.js` passa a gerar as rotas de jogo a partir de uma lista única, em vez de as ter escritas uma a uma. | — |
| E-F2 | ⏸ Shell de mesa reutilizável | Extrair de `table.css` e `views/pages/blackjack.js` a parte comum (tapete, zona de saldo, zona de aposta, painel de resultado) para os outros jogos herdarem. O blackjack passa a usar a shell. **Em espera:** o Tiago (AquaTPPT) tem uma reescrita grande de `blackjack.js` por mergear (PRs #22/#24, fechados) — coordenar antes de tocar neste ficheiro. | — |
| E-F3 | ✅ Lobby dinâmico | O lobby lista os jogos a partir da mesma lista do E-F1, com o Bar à parte (não é jogo). | E-F1 |
>>>>>>> aa580614f083b57544366ef99b839ec37b5dc086
=======
| E-F1 | Rotas a partir de uma lista de jogos | O `routes.js` passa a gerar as rotas de jogo a partir de uma lista única, em vez de as ter escritas uma a uma. | — |
| E-F2 | Shell de mesa reutilizável | Extrair de `table.css` e `views/pages/blackjack.js` a parte comum (tapete, zona de saldo, zona de aposta, painel de resultado) para os outros jogos herdarem. O blackjack passa a usar a shell. | — |
| E-F3 | Lobby dinâmico | O lobby lista os jogos a partir da mesma lista do E-F1, com o Bar à parte (não é jogo). | E-F1 |
>>>>>>> a0a7eb74a5a7007662e8df8de1f30aa906bc317b

> Lembrete: cada rota nova de SPA precisa de estar coberta pelo `SpaForwardController` (E4),
> senão dá 404 no F5 — é a mesma armadilha que já está documentada no blackjack.
