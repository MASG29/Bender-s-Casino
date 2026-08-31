# Peixinho (À Pesca) — Front-end (X-F)

**Front-end: Marco** (o Diogo fica com o back-end do Peixinho). Branch: `feat/peixinho-frontend` (back-end do Diogo está em `feat/peixinho-backend`).

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| X-F1 | Vista da mesa | Shell do E-F2. Mão do jogador visível, **mão do adversário só como número de cartas viradas para baixo**, monte ao centro. | E-F2 |
| X-F2 | Fazer um pedido | Só deixar pedir valores que o jogador tem na mão — a regra é do back-end, mas a UI não deve sequer oferecer o que é inválido. | X-B1 |
| X-F3 | Conjuntos baixados | Zona própria para os conjuntos de 4 de cada lado, com contagem. | X-B1 |
| X-F4 | Feedback de "vai à pesca" | Distinguir claramente as três coisas: recebeu cartas e joga outra vez / foi à pesca / passou a vez. | X-B1 |
| X-F5 | Vez do bot | Mostrar o que o bot pediu e o que aconteceu, com pausa suficiente para se conseguir ler. | X-B3 |
| X-F6 | Fim de partida | Contagem final de conjuntos e resultado da aposta. | X-B0 |
