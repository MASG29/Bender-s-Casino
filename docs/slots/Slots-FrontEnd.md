# Slots — Front-end (S-F)

**Dupla: Eddie + Tiago Paulos.** Branch: `feat/slots`.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| S-F1 | Vista da máquina | Shell do E-F2 adaptada: três rolos, alavanca/botão, mostrador de saldo e aposta. | E-F2 |
| S-F2 | Animação dos rolos | Rolos param um a um. **Os símbolos finais são os que o back-end devolveu** — o cliente nunca decide o resultado. | S-B1 |
| S-F3 | Escolha da aposta | Valor por spin, validado contra o saldo antes de enviar. | S-B1 |
| S-F4 | Prémios e eventos | Destacar as linhas ganhas; tratamento especial para `JACKPOT` e `NEAR_MISS`. | S-B4 |
| S-F5 | Assets | Símbolos, corpo da máquina, som opcional. | — |

> **Arranca já:** os assets e o desenho da máquina não dependem de nada.
