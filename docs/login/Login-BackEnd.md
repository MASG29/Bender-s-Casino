# Login — Back-end (L)

**Dupla: Marco + Diogo. Back-end: Marco. Front-end: Diogo.** É o primeiro trabalho da dupla,
antes da Roleta e do Peixinho. Branch: `feat/login`.

O custo real do login não é o formulário nem o hash — é a **persistência**. Hoje o `pom.xml` não
tem `data-jpa` nem `security`, e o `InMemoryPlayerRepository` perde tudo a cada restart. Uma
conta que desaparece quando o servidor reinicia não é uma conta.

## Decisões tomadas

| # | Decisão | Resolvido |
|---|---------|-----------|
| L0 | Identidade do jogador | **Continua a ser o `playerId` (UUID).** A conta liga-se ao jogador que já existe; o resto do domínio não muda. |
| L0b | Persistência | **Base de dados.** O `InMemoryPlayerRepository` deixa de servir — as contas têm de sobreviver a um restart. |
| L0c | Motor da base de dados | Por confirmar. **H2 em ficheiro** é o arranque natural: não obriga ninguém da equipa a instalar nada e passa a Postgres só mudando o driver e o `application.properties`. |

## Tarefas

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| L-B1 | Persistência | Acrescentar `spring-boot-starter-data-jpa` + driver ao `pom.xml`. `Player` passa a `@Entity` com o `id` (UUID) como chave primária. Migrar `InMemoryPlayerRepository` para repositório JPA **mantendo a mesma interface**, para os serviços que já existem não terem de mudar. | L0c |
| L-B2 | Credenciais | `spring-boot-starter-security`, campos `username` + `passwordHash` no `Player`, `BCryptPasswordEncoder`. **Nunca guardar a password em claro, nem a devolver em nenhum DTO.** | L-B1 |
| L-B3 | Endpoints de auth | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`. Sessão HTTP do Spring Security chega — não é preciso JWT. | L-B2 |
| L-B4 | Proteger a API | Fechar `/api/games/**` e `/api/players/**` a quem não está autenticado; deixar aberto `/api/auth/**` e os estáticos. Redefinir o que `PlayerService.reset()` faz agora que o saldo persiste. | L-B3, **E3** |

> **Porque é que o L-B4 depende do E3:** o padrão `/api/games/**` só existe depois do E3. Se o
> login entrar antes, o matcher de segurança fica escrito contra `/api/blackjack` e tem de ser
> mexido logo a seguir.

## Nota de coordenação

**L-B1, L-B2 e L-B3 não dependem do núcleo (`E`)** — arrancam já. Só o L-B4 espera pelo E3.

O Marco é dono do `E` **e** de parte do login, e as duas branches tocam nos mesmos ficheiros
(`pom.xml`, controllers, config): podem andar as duas, mas **mergeia-se uma de cada vez**.

Divisão dentro da dupla: **Marco no back-end (L-B1 a L-B4), Diogo no front-end (L-F1 a L-F4).**
O ponto de encontro é o L-B3 — assim que os endpoints de `/api/auth/**` estiverem congelados, os
dois lados avançam ao mesmo tempo.
