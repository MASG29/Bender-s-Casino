# Fase 2 — índice

O blackjack (MVP) está feito. A Fase 2 são quatro jogos novos, um sistema de login e, mais para
o fim, o Bar.

**Para saber o que fazer a seguir: [AGORA.md](AGORA.md).**

## Quem faz o quê

Cada dupla leva um jogo de ponta a ponta — back-end e front-end. As duas duplas trabalham ao
mesmo tempo, sem ordem de prioridade entre jogos.

| Área | Prefixo | Dupla | Back-end | Front-end | Documentos |
|------|---------|-------|----------|-----------|------------|
| Núcleo de jogo | `E` | Marco | Marco | Marco | [nucleo/](nucleo/Nucleo-Extracao.md) · [front](nucleo/Nucleo-FrontEnd.md) |
| Login | `L` | Marco + Diogo | Marco | Diogo | [login/](login/Login-BackEnd.md) · [front](login/Login-FrontEnd.md) |
| Roleta MVP | `R` | Marco + Diogo | Marco | Diogo | [roleta/](roleta/Roleta-BackEnd.md) · [front](roleta/Roleta-FrontEnd.md) |
| Peixinho | `X` | Marco + Diogo | Diogo | Marco | [peixinho/](peixinho/Peixinho-BackEnd.md) · [front](peixinho/Peixinho-FrontEnd.md) |
| Slots | `S` | Eddie + Tiago Paulos | por decidir | por decidir | [slots/](slots/Slots-BackEnd.md) · [front](slots/Slots-FrontEnd.md) |
| Video Poker | `V` | Eddie + Tiago Paulos | por decidir | por decidir | [videopoker/](videopoker/VideoPoker-BackEnd.md) · [front](videopoker/VideoPoker-FrontEnd.md) |
| Bar | — | — | — | — | [bar/](bar/Bar.md) — adiado |

O blackjack tem os seus próprios documentos em [blackjack/](blackjack/BlackJack-BackEnd.md) ·
[front](blackjack/BlackJack-FrontEnd.md). São os antigos `BackEnd-Tasks.md` e
`FrontEnd-Tasks.md`, e é lá que continua a estar a infraestrutura partilhada que nasceu com o
MVP (wrapper Maven, repositórios em memória, `DeckClient`, exceções, `PlayerService`,
`JokeService`) — está arrumada sob o blackjack porque o MVP *era* o blackjack.

## O Bar não é um jogo

É uma zona de convívio para beber e conviver. Não tem aposta, não tem vencedor, não entra na
lista de jogos do lobby como os outros. Ver [bar/Bar.md](bar/Bar.md).

## Núcleo primeiro

Todo o código atual tem forma de blackjack. As tarefas `E` ([nucleo/](nucleo/Nucleo-Extracao.md))
generalizam o `GameSession`, as rotas e a shell de mesa. Não bloqueiam tudo — DTOs, funções puras
e assets arrancam já — mas bloqueiam os serviços, os controllers e as vistas de jogo.

## Ficheiros partilhados

Não é uma questão de carga, é de conflitos: as branches que tocam em `pom.xml`, `routes.js`,
`table.css`, `SpaForwardController` ou no `GameSession` entram no `dev` **uma de cada vez**.
Tudo o resto pode andar em paralelo à vontade.

## Contrato primeiro

Em cada jogo, o par back/front **congela os DTOs antes de qualquer um implementar**. Foi assim
que o MVP correu bem e é o que permite os dois lados avançarem ao mesmo tempo.

## Branches

`main` ← `dev` ← `feat/*`. Sempre PR, nunca push direto para `main` ou `dev`.

- `feat/nucleo` — extração do núcleo (Marco)
- `feat/login` — login (Marco + Diogo)
- `feat/roleta` — Roleta (Marco + Diogo)
- `feat/peixinho` — Peixinho (Marco + Diogo)
- `feat/slots` — Slots (Eddie + Tiago Paulos)
- `feat/videopoker` — Video Poker (Eddie + Tiago Paulos)
