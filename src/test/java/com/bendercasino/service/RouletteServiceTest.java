package com.bendercasino.service;

import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.Colour;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.model.RouletteState;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouletteServiceTest {

    private PlayerRepository playerRepository;
    private InMemoryGameSessionRepository sessionRepository;
    private Random random;
    private RouletteService service;
    private Player player;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerRepository = mock(PlayerRepository.class);
        Map<UUID, Player> store = new HashMap<>();
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            store.put(p.getId(), p);
            return p;
        });
        when(playerRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return Optional.ofNullable(store.get(id));
        });

        sessionRepository = new InMemoryGameSessionRepository();
        random = mock(Random.class);
        service = new RouletteService(playerRepository, sessionRepository, random);

        player = new Player("Fry", "fry", "Philip", "Fry", "fry@example.com", "hash");
        playerId = player.getId();
        playerRepository.save(player);
    }

    @Test
    @DisplayName("aposta vermelho, sai numero vermelho, ganha 1:1")
    void winningRedBet_paysDouble() {
        when(random.nextInt(37)).thenReturn(1); // 1 e vermelho

        GameSession session = service.spin(playerId, 100, Colour.RED);

        assertThat(player.getBalance()).isEqualTo(1000 - 100 + 200);
        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        RouletteState state = (RouletteState) session.getState();
        assertThat(state.won()).isTrue();
        assertThat(state.number()).isEqualTo(1);
        assertThat(state.colour()).isEqualTo(Colour.RED);
        assertThat(state.betColour()).isEqualTo(Colour.RED);
        assertThat(state.payout()).isEqualTo(200);
    }

    @Test
    @DisplayName("aposta vermelho, sai numero preto, perde")
    void losingRedBet_paysNothing() {
        when(random.nextInt(37)).thenReturn(2); // 2 e preto

        GameSession session = service.spin(playerId, 100, Colour.RED);

        assertThat(player.getBalance()).isEqualTo(1000 - 100);
        RouletteState state = (RouletteState) session.getState();
        assertThat(state.won()).isFalse();
        assertThat(state.payout()).isZero();
    }

    @Test
    @DisplayName("sai o zero, aposta vermelho ou preto perde sempre")
    void zeroAlwaysLoses() {
        when(random.nextInt(37)).thenReturn(0);

        GameSession session = service.spin(playerId, 100, Colour.RED);

        assertThat(player.getBalance()).isEqualTo(1000 - 100);
        RouletteState state = (RouletteState) session.getState();
        assertThat(state.won()).isFalse();
        assertThat(state.colour()).isEqualTo(Colour.GREEN);
    }

    @Test
    @DisplayName("saldo insuficiente lanca InsufficientBalanceException e nao debita")
    void insufficientBalance_throwsAndDoesNotDebit() {
        assertThatThrownBy(() -> service.spin(playerId, 10_000, Colour.RED))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(player.getBalance()).isEqualTo(1000);
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    @DisplayName("aposta invalida (<= 0) lanca InvalidBetException")
    void invalidBet_throws() {
        assertThatThrownBy(() -> service.spin(playerId, 0, Colour.RED))
                .isInstanceOf(InvalidBetException.class);
    }

    @Test
    @DisplayName("jogador desconhecido lanca PlayerNotFoundException")
    void unknownPlayer_throws() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.spin(unknown, 100, Colour.RED))
                .isInstanceOf(PlayerNotFoundException.class);
    }
}
