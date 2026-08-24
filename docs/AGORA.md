# Agora — o que está em cima da mesa

> Resumo curto. As tarefas detalhadas estão nos ficheiros por jogo — ver [Fase2-Jogos.md](Fase2-Jogos.md).

## Marco

1. **Back-end do login** (`docs/login/Login-BackEnd.md`). Já está decidido: as contas vão para
   **base de dados** e a identidade **continua a ser o `playerId` (UUID)**. **L-B1
   (persistência), L-B2 (credenciais) e L-B3 (endpoints) não dependem do núcleo**, arrancam já.
   Falta só confirmar o motor da base de dados (L0c) — H2 em ficheiro é o arranque natural.
2. **E0 — testes de controller (`@WebMvcTest`)**, o antigo A8, e **E4** (`SpaForwardController`
   por padrão). São independentes de tudo o resto e podem entrar entre PRs do login.
3. **E1 → E2 → E3 — extração do núcleo** (`docs/nucleo/`). Têm de estar no `dev` antes do
   **L-B4**, que fecha `/api/games/**` — esse padrão de rota só existe depois do E3.
4. Depois: back-end da Roleta (`docs/roleta/`).

> Login e núcleo tocam nos mesmos ficheiros (`pom.xml`, controllers, config). Podem andar os
> dois, mas **não se mergeiam duas branches ao mesmo tempo** — um PR de cada vez.

## Diogo

1. **Front-end do login** (`docs/login/Login-FrontEnd.md`) — L-F1 a L-F4. Arranca assim que o
   Marco congelar os endpoints do L-B3.
2. Depois: back-end do Peixinho (`docs/peixinho/`) — começar pelo **X-B0**, a adaptação a casino,
   que é uma decisão, não código.

## Eddie + Tiago Paulos

Falta decidir entre vocês quem fica com o back-end e quem fica com o front-end dos Slots e do
Video Poker.

**Podem começar hoje, sem esperar por nada:**
- `SlotPaytable` e `PokerHandEvaluator` — funções puras, TDD, sem Spring.
- Congelar os DTOs (S-B1, V-B1).
- Assets: símbolos dos slots, corpo das máquinas.

**Só depois do núcleo (E) estar no `dev`:** os serviços com estado de sessão, os controllers e
as vistas de jogo.

## Já decidido

- Login com **base de dados** (as contas persistem entre restarts).
- A identidade do jogador **continua a ser o `playerId` (UUID)**.
- Login: **Marco no back-end, Diogo no front-end.**

## Decisões que faltam

| # | Decisão | Quem |
|---|---------|------|
| L0c | Motor da base de dados (H2 em ficheiro vs Postgres) | Marco |
| X-B0 | Adaptação do Peixinho a casino: aposta, adversário, duração | Diogo |
| — | Quem faz back-end e quem faz front-end nos Slots / Video Poker | Eddie + Tiago Paulos |
| — | Quem escreve os testes na Fase 2 (na Fase 1 era sempre o Marco) | equipa |

## Adiado

O **Bar** (`docs/bar/`) fica para depois dos quatro jogos e do login.
