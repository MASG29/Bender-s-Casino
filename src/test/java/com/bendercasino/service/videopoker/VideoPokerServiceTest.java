package com.bendercasino.service.videopoker;

import com.bendercasino.client.DeckClient;
import com.bendercasino.dto.videopoker.DrawRequest;
import com.bendercasino.exception.InsufficientBalanceException;
import com.bendercasino.exception.InvalidBetException;
import com.bendercasino.exception.InvalidGameStateException;
import com.bendercasino.exception.PlayerNotFoundException;
import com.bendercasino.model.Card;
import com.bendercasino.model.Deck;
import com.bendercasino.model.GameSession;
import com.bendercasino.model.GameStatus;
import com.bendercasino.model.Player;
import com.bendercasino.model.videopoker.PokerHandCategory;
import com.bendercasino.model.videopoker.VideoPokerState;
import com.bendercasino.repository.InMemoryGameSessionRepository;
import com.bendercasino.repository.PlayerRepository;
import com.bendercasino.service.JokeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

class VideoPokerServiceTest {

    private PlayerRepository playerRepository;
    private InMemoryGameSessionRepository sessionRepository;
    private DeckClient deckClient;
    private VideoPokerService service;
    private Player player;
    private UUID playerId;

    private static Card card(String value, String suit) {
        return new Card(value.substring(0, 1) + suit.substring(0, 1), value, suit, "");
    }

    private static VideoPokerState poker(GameSession session) {
        return (VideoPokerState) session.getState();
    }

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
        deckClient = mock(DeckClient.class);
        service = new VideoPokerService(deckClient, playerRepository, sessionRepository, mock(JokeService.class));

        player = new Player("TestPlayer", "testplayer", "Test", "Player", "test@example.com", "hash");
        playerRepository.save(player);
        playerId = player.getId();
    }

    @Test
    @DisplayName("start deals 5 cards and waits for draw")
    void start_dealsFiveCards() {
        when(deckClient.newShuffledDeck(1)).thenReturn(new Deck("deck-1", 52));
        when(deckClient.draw("deck-1", 5)).thenReturn(List.of(
                card("2", "HEARTS"), card("6", "SPADES"), card("9", "CLUBS"),
                card("JACK", "DIAMONDS"), card("KING", "HEARTS")));

        GameSession session = service.start(playerId, 100);

        assertThat(session.getStatus()).isEqualTo(GameStatus.PLAYER_TURN);
        assertThat(poker(session).getCards()).hasSize(5);
        assertThat(poker(session).getCategory()).isNull();
        assertThat(player.getBalance()).isEqualTo(900);
    }

    @Test
    @DisplayName("draw holding a flush pays 6x")
    void draw_holdFlush_pays() {
        when(deckClient.newShuffledDeck(1)).thenReturn(new Deck("deck-1", 52));
        when(deckClient.draw("deck-1", 5)).thenReturn(List.of(
                card("2", "HEARTS"), card("6", "HEARTS"), card("9", "HEARTS"),
                card("JACK", "HEARTS"), card("KING", "HEARTS")));

        GameSession session = service.start(playerId, 100);
        session = service.act(session.getGameId(), "draw", new DrawRequest(List.of(0, 1, 2, 3, 4)));

        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(poker(session).getCategory()).isEqualTo(PokerHandCategory.FLUSH);
        assertThat(session.getBet().payout()).isEqualTo(600);
        assertThat(player.getBalance()).isEqualTo(1500);
    }

    @Test
    @DisplayName("draw twice is not allowed")
    void draw_twice_invalid() {
        when(deckClient.newShuffledDeck(1)).thenReturn(new Deck("deck-1", 52));
        when(deckClient.draw("deck-1", 5)).thenReturn(List.of(
                card("2", "HEARTS"), card("6", "SPADES"), card("9", "CLUBS"),
                card("JACK", "DIAMONDS"), card("KING", "HEARTS")));

        GameSession session = service.start(playerId, 100);
        service.act(session.getGameId(), "draw", new DrawRequest(List.of()));

        assertThatThrownBy(() -> service.act(session.getGameId(), "draw", new DrawRequest(List.of())))
                .isInstanceOf(InvalidGameStateException.class);
    }

    @Test
    @DisplayName("held index outside 0-4 is rejected")
    void draw_badIndex_invalid() {
        when(deckClient.newShuffledDeck(1)).thenReturn(new Deck("deck-1", 52));
        when(deckClient.draw("deck-1", 5)).thenReturn(List.of(
                card("2", "HEARTS"), card("6", "SPADES"), card("9", "CLUBS"),
                card("JACK", "DIAMONDS"), card("KING", "HEARTS")));

        GameSession session = service.start(playerId, 100);

        assertThatThrownBy(() -> service.act(session.getGameId(), "draw", new DrawRequest(List.of(5))))
                .isInstanceOf(InvalidGameStateException.class);
    }

    @Test
    @DisplayName("bet 0 -> InvalidBetException")
    void start_betZero() {
        assertThatThrownBy(() -> service.start(playerId, 0))
                .isInstanceOf(InvalidBetException.class);
    }

    @Test
    @DisplayName("bet more than balance -> InsufficientBalanceException")
    void start_tooRich() {
        assertThatThrownBy(() -> service.start(playerId, 1500))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("unknown player -> PlayerNotFoundException")
    void start_unknownPlayer() {
        assertThatThrownBy(() -> service.start(UUID.randomUUID(), 100))
                .isInstanceOf(PlayerNotFoundException.class);
    }
}
