package com.bendercasino.exception;

import com.bendercasino.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlayerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePlayerNotFound(PlayerNotFoundException ex, HttpServletRequest req) {
        return build(404, "PLAYER_NOT_FOUND", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(GameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGameNotFound(GameNotFoundException ex, HttpServletRequest req) {
        return build(404, "GAME_NOT_FOUND", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(UnknownGameException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUnknownGame(UnknownGameException ex, HttpServletRequest req) {
        return build(404, "UNKNOWN_GAME", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(InvalidBetException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidBet(InvalidBetException ex, HttpServletRequest req) {
        return build(400, "INVALID_BET", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest req) {
        return build(400, "INSUFFICIENT_BALANCE", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(InvalidGameStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidGameState(InvalidGameStateException ex, HttpServletRequest req) {
        return build(409, "INVALID_GAME_STATE", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(DeckApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleDeckApi(DeckApiException ex, HttpServletRequest req) {
        return build(503, "DECK_API_UNAVAILABLE", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Validation error");
        return build(400, "VALIDATION_ERROR", msg, req.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return build(401, "INVALID_CREDENTIALS", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest req) {
        return build(400, "INVALID_ARGUMENT", ex.getMessage(), req.getRequestURI());
    }

    private ErrorResponse build(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now().toString(), status, error, message, path);
    }
}
