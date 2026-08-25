package com.bendercasino.service;

import com.bendercasino.client.DeckClient;
import com.bendercasino.exception.GameNotFoundException;
import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.InvalidGameStateException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.Bet;
import com.bendercasino.model.BlackjackState;
import com.bendercasino.model.Card;
import com.bendercasino.model.Deck;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Hand;
import com.bendercasino.model.Outcome;
import com.bendercasino.model.Player;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.InMemoryPlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BlackjackServiceTest {

    private InMemoryPlayerRepository playerRepository;
    private InMemoryGameSessionRepository sessionRepository;
    private DeckClient deckClient;
    private JokeService jokeService;
    private BlackjackService service;
    private Player player;
    private UUID playerId;

    private static BlackjackState state(GameSession session) {
        return (BlackjackState) session.getState();
    }

    private static Card card(String value, String suit) {
        return new Card(value.substring(0, 1) + suit.substring(0, 1), value, suit, "");
    }

    @BeforeEach
    void setUp() {
        playerRepository = new InMemoryPlayerRepository();
        sessionRepository = new InMemoryGameSessionRepository();
        deckClient = mock(DeckClient.class);
        jokeService = mock(JokeService.class);
        service = new BlackjackService(deckClient, playerRepository, sessionRepository, jokeService);

        player = new Player("TestPlayer");
        playerRepository.save(player);
        playerId = player.getId();
    }

    // --- 1. start deals 2 cards each, neither is blackjack ---
    @Test
    @DisplayName("start deals 2 cards each, neither blackjack -> PLAYER_TURN")
    void start_dealsFourCards_noBlackjack_playerTurn() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("2", "HEARTS"),    // player card 1
                card("3", "HEARTS"),    // player card 2 = 5
                card("9", "SPADES"),    // dealer card 1
                card("9", "DIAMONDS")   // dealer card 2 = 18
        ));

        GameSession session = service.start(playerId, 100);

        assertThat(session.getStatus()).isEqualTo(GameStatus.PLAYER_TURN);
        assertThat(state(session).getOutcome()).isNull();
        assertThat(state(session).getPlayerHand().getCards()).hasSize(2);
        assertThat(state(session).getDealerHand().getCards()).hasSize(2);
        assertThat(state(session).getPlayerHand().value()).isEqualTo(5);
        assertThat(state(session).getDealerHand().value()).isEqualTo(18);
    }

    // --- 2. Player blackjack, dealer not ---
    @Test
    @DisplayName("player natural blackjack, dealer not -> PLAYER_BLACKJACK, balance 1150")
    void start_playerBlackjack_only_playerBlackjack() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("ACE", "SPADES"),   // player card 1
                card("KING", "HEARTS"),  // player card 2 -> blackjack
                card("9", "SPADES"),     // dealer card 1
                card("9", "DIAMONDS")    // dealer card 2 = 18
        ));

        GameSession session = service.start(playerId, 100);

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(state(session).getOutcome()).isEqualTo(Outcome.PLAYER_BLACKJACK);
        assertThat(player.getBalance()).isEqualTo(1150); // 1000 - 100 + 250
        assertThat(player.getTotalBlackjacks()).isEqualTo(1);
        assertThat(player.getConsecutiveBlackjacks()).isEqualTo(1);
        assertThat(session.getBet().payout()).isEqualTo(250); // 100 * 5 / 2
    }

    // --- 3. Both blackjack ---
    @Test
    @DisplayName("both blackjack -> PUSH, balance 1000")
    void start_bothBlackjack_push() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("ACE", "SPADES"),   // player card 1
                card("KING", "HEARTS"),  // player card 2 -> blackjack
                card("ACE", "DIAMONDS"), // dealer card 1
                card("QUEEN", "CLUBS")   // dealer card 2 -> blackjack
        ));

        GameSession session = service.start(playerId, 100);

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(state(session).getOutcome()).isEqualTo(Outcome.PUSH);
        assertThat(player.getBalance()).isEqualTo(1000); // 1000 - 100 + 100
        assertThat(player.getTotalPushes()).isEqualTo(1);
        assertThat(session.getBet().payout()).isEqualTo(100);
    }

    // --- 4. Player hits and busts ---
    @Test
    @DisplayName("hit busts -> PLAYER_BUST, balance 900")
    void hit_busts_playerBust() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("10", "HEARTS"),   // player card 1
                card("9", "CLUBS"),     // player card 2 = 19
                card("6", "SPADES"),    // dealer card 1
                card("5", "DIAMONDS")   // dealer card 2 = 11
        ));
        // Player hits: draws KING -> 29 (bust)
        when(deckClient.draw("deck-1", 1)).thenReturn(List.of(card("KING", "DIAMONDS")));

        service.start(playerId, 100);
        GameSession session = service.hit(playerId);

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(state(session).getOutcome()).isEqualTo(Outcome.PLAYER_BUST);
        assertThat(player.getBalance()).isEqualTo(900); // 1000 - 100 + 0
        assertThat(player.getTotalLosses()).isEqualTo(1);
        assertThat(state(session).getPlayerHand().isBusted()).isTrue();
    }

    // --- 5. stand: dealer draws exactly one card to reach 17 ---
    @Test
    @DisplayName("stand dealer draws one to 17 -> PLAYER_WIN, balance 1100")
    void stand_dealerDrawsOneTo17_playerWin() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("10", "SPADES"),  // player card 1
                card("9", "HEARTS"),   // player card 2 = 19
                card("6", "HEARTS"),   // dealer card 1
                card("5", "DIAMONDS")  // dealer card 2 = 11
        ));
        // Dealer draws one card: 6 -> 17
        when(deckClient.draw("deck-1", 1)).thenReturn(List.of(card("6", "CLUBS")));

        service.start(playerId, 100);
        GameSession session = service.stand(playerId);

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(state(session).getOutcome()).isEqualTo(Outcome.PLAYER_WIN);
        assertThat(player.getBalance()).isEqualTo(1100); // 1000 - 100 + 200
        assertThat(player.getTotalWins()).isEqualTo(1);
        assertThat(session.getBet().payout()).isEqualTo(200);
        verify(deckClient, times(1)).draw("deck-1", 1);
    }

    // --- 6. stand: dealer busts ---
    @Test
    @DisplayName("stand dealer busts -> DEALER_BUST, balance 1100")
    void stand_dealerBusts_dealerBust() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("10", "SPADES"),  // player card 1
                card("7", "HEARTS"),   // player card 2 = 17
                card("6", "CLUBS"),    // dealer card 1
                card("6", "DIAMONDS")  // dealer card 2 = 12
        ));
        // Dealer draws KING -> 22 (bust)
        when(deckClient.draw("deck-1", 1)).thenReturn(List.of(card("KING", "SPADES")));

        service.start(playerId, 100);
        GameSession session = service.stand(playerId);

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(state(session).getOutcome()).isEqualTo(Outcome.DEALER_BUST);
        assertThat(player.getBalance()).isEqualTo(1100); // 1000 - 100 + 200
        assertThat(player.getTotalWins()).isEqualTo(1);
        assertThat(session.getBet().payout()).isEqualTo(200);
    }

    // --- 7. stand: equal totals (push) ---
    @Test
    @DisplayName("stand equal totals -> PUSH, balance 1000")
    void stand_equalTotals_push() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("10", "SPADES"),  // player card 1
                card("9", "HEARTS"),   // player card 2 = 19
                card("10", "DIAMONDS"),// dealer card 1
                card("9", "CLUBS")     // dealer card 2 = 19 (already >= 17, no draw)
        ));

        service.start(playerId, 100);
        GameSession session = service.stand(playerId);

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(state(session).getOutcome()).isEqualTo(Outcome.PUSH);
        assertThat(player.getBalance()).isEqualTo(1000); // 1000 - 100 + 100
        assertThat(player.getTotalPushes()).isEqualTo(1);
        assertThat(session.getBet().payout()).isEqualTo(100);
    }

    // --- 8. stand: dealer ends higher without busting ---
    @Test
    @DisplayName("stand dealer higher -> DEALER_WIN, balance 900")
    void stand_dealerHigher_dealerWin() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("10", "SPADES"),  // player card 1
                card("7", "HEARTS"),   // player card 2 = 17
                card("10", "DIAMONDS"),// dealer card 1
                card("9", "CLUBS")     // dealer card 2 = 19 (already >= 17, no draw)
        ));

        service.start(playerId, 100);
        GameSession session = service.stand(playerId);

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(state(session).getOutcome()).isEqualTo(Outcome.DEALER_WIN);
        assertThat(player.getBalance()).isEqualTo(900); // 1000 - 100 + 0
        assertThat(player.getTotalLosses()).isEqualTo(1);
        assertThat(session.getBet().payout()).isEqualTo(0);
    }

    // --- 9. start with bet = 0 ---
    @Test
    @DisplayName("start bet=0 -> InvalidBetException, no deck interaction")
    void start_betZero_invalidBetException() {
        assertThatThrownBy(() -> service.start(playerId, 0))
                .isInstanceOf(InvalidBetException.class);

        verifyNoInteractions(deckClient);
    }

    // --- 10. start with bet > balance ---
    @Test
    @DisplayName("start bet > balance -> InsufficientBalanceException, no deck interaction")
    void start_betExceedsBalance_insufficientBalanceException() {
        assertThatThrownBy(() -> service.start(playerId, 1500))
                .isInstanceOf(InsufficientBalanceException.class);

        verifyNoInteractions(deckClient);
    }

    // --- 11. start for non-existent player ---
    @Test
    @DisplayName("start unknown player -> PlayerNotFoundException")
    void start_unknownPlayer_playerNotFoundException() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> service.start(unknownId, 100))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    // --- 12. start twice for same player with unfinished game ---
    @Test
    @DisplayName("start twice unfinished -> InvalidGameStateException on second, no deck interaction")
    void start_twiceUnfinished_invalidGameStateException() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("2", "HEARTS"),
                card("3", "HEARTS"),
                card("9", "SPADES"),
                card("9", "DIAMONDS")
        ));

        service.start(playerId, 100);

        assertThatThrownBy(() -> service.start(playerId, 100))
                .isInstanceOf(InvalidGameStateException.class);

        // Only one call to newShuffledDeck and draw (for the first start)
    }

    // --- 13. hit after game finished ---
    @Test
    @DisplayName("hit after finished -> InvalidGameStateException")
    void hit_afterFinished_invalidGameStateException() {
        var deck = new Deck("deck-1", 308);
        when(deckClient.newShuffledDeck(6)).thenReturn(deck);
        when(deckClient.draw("deck-1", 4)).thenReturn(List.of(
                card("10", "HEARTS"),
                card("9", "CLUBS"),
                card("6", "SPADES"),
                card("5", "DIAMONDS")
        ));
        when(deckClient.draw("deck-1", 1)).thenReturn(List.of(card("KING", "DIAMONDS")));

        service.start(playerId, 100);
        service.hit(playerId); // busts -> game finished

        assertThatThrownBy(() -> service.hit(playerId))
                .isInstanceOf(InvalidGameStateException.class);
    }

    // --- 14. getState for player with no session ---
    @Test
    @DisplayName("getState no session -> GameNotFoundException")
    void getState_noSession_gameNotFoundException() {
        assertThatThrownBy(() -> service.getState(playerId))
                .isInstanceOf(GameNotFoundException.class);
    }
}