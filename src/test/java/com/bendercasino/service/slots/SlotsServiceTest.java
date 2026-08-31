package com.bendercasino.service.slots;

import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.model.slots.SlotsState;
import com.bendercasino.model.slots.Symbol;
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

class SlotsServiceTest {

    private PlayerRepository playerRepository;
    private InMemoryGameSessionRepository sessionRepository;
    private Random random;
    private SlotsService service;
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
        service = new SlotsService(playerRepository, sessionRepository, random);

        player = new Player("Fry", "fry", "Philip", "Fry", "fry@example.com", "hash");
        playerId = player.getId();
        playerRepository.save(player);
    }

    /**
     * Slot() builds its reel from Symbol.values() minus NONE, in enum declaration order,
     * so nextInt(reelSize) at draw N must return that symbol's index in that reel.
     */
    private void fixReel(Symbol first, Symbol second, Symbol third) {
        Symbol[] reel = { Symbol.CHERRY, Symbol.BAR, Symbol.DOUBLE_BAR, Symbol.TRIPLE_BAR,
                Symbol.BELL, Symbol.LEELA, Symbol.FRY, Symbol.BENDER };
        when(random.nextInt(anyInt())).thenReturn(
                indexOf(reel, first), indexOf(reel, second), indexOf(reel, third));
    }

    private int indexOf(Symbol[] reel, Symbol symbol) {
        for (int i = 0; i < reel.length; i++) {
            if (reel[i] == symbol) {
                return i;
            }
        }
        throw new IllegalArgumentException("Symbol not on reel: " + symbol);
    }

    @Test
    @DisplayName("tres simbolos iguais paga o valor do simbolo")
    void threeOfAKind_paysSymbolValue() {
        fixReel(Symbol.BELL, Symbol.BELL, Symbol.BELL);

        GameSession session = service.spin(playerId, 100);

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        SlotsState state = (SlotsState) session.getState();
        assertThat(state.symbols()).containsExactly(Symbol.BELL, Symbol.BELL, Symbol.BELL);
        assertThat(state.payout()).isEqualTo(400); // 100 * 4.0
        assertThat(player.getBalance()).isEqualTo(1000 - 100 + 400);
    }

    @Test
    @DisplayName("nenhum simbolo repetido nao paga nada")
    void noMatch_paysNothing() {
        fixReel(Symbol.CHERRY, Symbol.BAR, Symbol.BELL);

        GameSession session = service.spin(playerId, 100);

        SlotsState state = (SlotsState) session.getState();
        assertThat(state.payout()).isZero();
        assertThat(player.getBalance()).isEqualTo(1000 - 100);
    }

    @Test
    @DisplayName("saldo insuficiente lanca InsufficientBalanceException e nao debita")
    void insufficientBalance_throwsAndDoesNotDebit() {
        assertThatThrownBy(() -> service.spin(playerId, 10_000))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(player.getBalance()).isEqualTo(1000);
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    @DisplayName("aposta invalida (<= 0) lanca InvalidBetException")
    void invalidBet_throws() {
        assertThatThrownBy(() -> service.spin(playerId, 0))
                .isInstanceOf(InvalidBetException.class);
    }

    @Test
    @DisplayName("jogador desconhecido lanca PlayerNotFoundException")
    void unknownPlayer_throws() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.spin(unknown, 100))
                .isInstanceOf(PlayerNotFoundException.class);
    }
}
