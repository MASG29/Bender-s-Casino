# Roleta MVP — Front-end (R-F)

**Front-end: Diogo** (o Marco fica com o back-end da Roleta). Branch: `feat/roleta-frontend` (back-end do Marco está em `feat/roleta-backend`).

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| R-F1 | Vista da roleta | Reaproveitar a shell de mesa do E-F2. Roda + tapete reduzido a duas zonas: preto e vermelho. | E-F2 |
| R-F2 | Escolha de cor e aposta | Selecionar cor, escolher o valor da ficha, validar contra o saldo antes de enviar. | R-B1 |
| R-F3 | Animação da roda | **A animação tem de parar no número que o back-end devolveu.** O resultado nunca é decidido no cliente — a animação é só apresentação de um resultado já fechado. | R-B1 |
| R-F4 | Resultado | Mostrar número, cor, ganho/perda e saldo novo. | R-F3 |
| R-F5 | Assets | Roda, fichas, zonas do tapete. | — |
