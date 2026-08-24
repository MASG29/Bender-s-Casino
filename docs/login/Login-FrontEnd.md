# Login — Front-end (L-F)

**Front-end: Diogo** (o Marco fica com o back-end do login). Branch: `feat/login`.

| # | Tarefa | Detalhe | Depende de |
|---|--------|---------|------------|
| L-F1 | Vista de registo | Formulário username + password + confirmação, validação no cliente antes de chamar `POST /api/auth/register`. | L-B3 |
| L-F2 | Vista de login | Formulário + erro visível quando as credenciais falham (não dizer *qual* dos dois campos falhou). | L-B3 |
| L-F3 | Sessão no `state.js` | Tirar o `playerId` do `sessionStorage` e passar a guardar a sessão no `state.js`, alimentada pelo que o back-end devolve no login. | L-B3 |
| L-F4 | Guarda no router | Rotas de jogo e de perfil redirecionam para `/login` se não houver sessão. A home passa a ser login/registo em vez de entrada direta no lobby. | L-F3 |
