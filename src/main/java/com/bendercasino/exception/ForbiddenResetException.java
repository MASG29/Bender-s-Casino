package com.bendercasino.exception;

/**
 * Levantada quando um jogador autenticado tenta fazer reset da conta de outro jogador.
 * O endpoint está autenticado (401 se não houver sessão); isto cobre a sessão errada (403).
 */
public class ForbiddenResetException extends RuntimeException {

    public ForbiddenResetException() {
        super("You can only reset your own account");
    }
}
