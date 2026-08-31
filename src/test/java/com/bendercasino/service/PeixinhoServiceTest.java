package com.bendercasino.service;

import com.bendercasino.client.DeckClient;
import com.bendercasino.dto.BookDto;
import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidGameStateException;
import com.bendercasino.model.Card;
import com.bendercasino.model.Deck;
import com.bendercasino.model.PeixinhoSession;
import com.bendercasino.model.Player;
import com.bendercasino.repository.InMemoryPeixinhoRepository;
import com.bendercasino.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PeixinhoServiceTest {

    private PlayerRepository playerRepository;
    private InMemoryPeixinhoRepository sessionRepository;
    private DeckClient deckClient;
    private PeixinhoBot bot;
    private PeixinhoService service;
    private Player player;

    private static Card card(String value, String suit) {
        return new Card(value + suit.substring(0, 1), value, suit, "");
    }

    @BeforeEach
    void setUp() {
        playerRepository = mock(PlayerRepository.class);
        sessionRepository = new InMemoryPeixinhoRepository();
        deckClient = mock(DeckClient.class);
        bot = mock(PeixinhoBot.class);
        service = new PeixinhoService(deckClient, sessionRepository, playerRepository, bot);
        player = new Player("TestPlayer", "testplayer", "Test", "Player", "test@example.com", "hash");

        when(playerRepository.findById(player.getId())).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deckClient.newShuffledDeck(1)).thenReturn(new Deck("deck-1", 52));
        when(deckClient.draw("deck-1", 14)).thenReturn(cards(14));
        when(deckClient.draw("deck-1", 38)).thenReturn(cards(38));
    }

    private static List<Card> cards(int count) {
        var cards = new ArrayList<Card>();
        for (int i = 0; i < count; i++) cards.add(card("2", "HEARTS"));
        return cards;
    }

    @Test
    void start_debitsBet() {
        service.start(player.getId(), 100);

        assertThat(player.getBalance()).isEqualTo(900);
    }

    @Test
    void start_rejectsInsufficientBalance() {
        assertThatThrownBy(() -> service.start(player.getId(), 1100))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(player.getBalance()).isEqualTo(1000);
    }

    @Test
    void start_rejectsExistingGame() {
        sessionRepository.save(player.getId(), session(player.getId(), UUID.randomUUID(), List.of(), List.of()));

        assertThatThrownBy(() -> service.start(player.getId(), 100))
                .isInstanceOf(InvalidGameStateException.class);
    }

    @Test
    void ask_transfersRequestedCards() {
        UUID botId = UUID.randomUUID();
        var playerHand = new ArrayList<>(List.of(card("7", "HEARTS")));
        var botHand = new ArrayList<>(List.of(card("7", "DIAMONDS"), card("KING", "SPADES")));
        var game = session(player.getId(), botId, playerHand, botHand);
        sessionRepository.save(player.getId(), game);

        service.ask(player.getId(), botId, "7");

        assertThat(playerHand).hasSize(2);
        assertThat(botHand).containsExactly(card("KING", "SPADES"));
    }

    @Test
    void ask_rejectsValueMissingFromPlayerHand() {
        UUID botId = UUID.randomUUID();
        sessionRepository.save(player.getId(), session(player.getId(), botId,
                new ArrayList<>(List.of(card("7", "HEARTS"))), new ArrayList<>()));

        assertThatThrownBy(() -> service.ask(player.getId(), botId, "KING"))
                .isInstanceOf(InvalidGameStateException.class);
    }

    @Test
    void ask_paysBetPerPlayerBookWhenPlayerWins() {
        UUID botId = UUID.randomUUID();
        var game = session(player.getId(), botId,
                new ArrayList<>(List.of(card("7", "HEARTS"))),
                new ArrayList<>(List.of(card("7", "DIAMONDS"))));
        addBooks(game, player.getId(), 8);
        addBooks(game, botId, 5);
        sessionRepository.save(player.getId(), game);
        player.debit(100);

        service.ask(player.getId(), botId, "7");

        assertThat(player.getBalance()).isEqualTo(1700); // 1000 - 100 + (100 * 8)
    }

    @Test
    void ask_doesNotCreditWhenBotWins() {
        UUID botId = UUID.randomUUID();
        var game = session(player.getId(), botId,
                new ArrayList<>(List.of(card("7", "HEARTS"))),
                new ArrayList<>(List.of(card("7", "DIAMONDS"))));
        addBooks(game, player.getId(), 5);
        addBooks(game, botId, 8);
        // The bot has the majority, so the human must not receive a payout.
        game.getBooks().removeIf(book -> book.playerId().equals(player.getId()));
        addBooks(game, player.getId(), 5);
        sessionRepository.save(player.getId(), game);
        player.debit(100);

        service.ask(player.getId(), botId, "7");

        assertThat(player.getBalance()).isEqualTo(900);
    }

    private static PeixinhoSession session(UUID playerId, UUID botId,
                                           List<Card> playerHand, List<Card> botHand) {
        Map<UUID, List<Card>> hands = new HashMap<>();
        hands.put(playerId, playerHand);
        hands.put(botId, botHand);
        return new PeixinhoSession(List.of(playerId, botId), hands, new ArrayList<>(), Map.of(playerId, 100, botId, 0));
    }

    private static void addBooks(PeixinhoSession session, UUID playerId, int count) {
        for (int i = 0; i < count; i++) session.addBook(new BookDto(playerId, "value-" + UUID.randomUUID()));
    }
}
