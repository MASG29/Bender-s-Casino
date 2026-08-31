package com.bendercasino.model;

import com.bendercasino.dto.BookDto;
import com.bendercasino.service.PeixinhoBot;

import java.util.*;

public class PeixinhoSession {

    private final UUID gameId;
    private final Map<UUID, List<Card>> hands;
    private final List<Card> deck;
    private final List<BookDto> books;
    private final List<UUID> playerOrder;
    private final List<PeixinhoBot.AskHistoryEntry> history;
    private final Map<UUID, Integer> bets;
    private int currentPlayerIndex;
    private String status;
    private String lastAction;

    public PeixinhoSession(List<UUID> playerOrder, Map<UUID, List<Card>> hands,
                           List<Card> deck, Map<UUID, Integer> bets) {
        this.gameId       = UUID.randomUUID();
        this.playerOrder  = new ArrayList<>(playerOrder);
        this.hands        = new HashMap<>(hands);
        this.deck         = new ArrayList<>(deck);
        this.books        = new ArrayList<>();
        this.history      = new ArrayList<>();
        this.bets         = new HashMap<>(bets);
        this.currentPlayerIndex = 0;
        this.status       = "PLAYING";
        this.lastAction   = "";
    }

    public UUID currentPlayerId() {
        return playerOrder.get(currentPlayerIndex);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
    }

    public void addHistory(PeixinhoBot.AskHistoryEntry entry) {
        history.add(entry);
    }

    public void addBook(BookDto book) {
        books.add(book);
    }

    public UUID getGameId()                              { return gameId; }
    public Map<UUID, List<Card>> getHands()              { return hands; }
    public List<Card> getDeck()                          { return deck; }
    public List<BookDto> getBooks()                      { return books; }
    public List<UUID> getPlayerOrder()                   { return playerOrder; }
    public List<PeixinhoBot.AskHistoryEntry> getHistory(){ return history; }
    public Map<UUID, Integer> getBets()                  { return bets; }
    public String getStatus()                            { return status; }
    public String getLastAction()                        { return lastAction; }
    public void setStatus(String status)                 { this.status = status; }
    public void setLastAction(String action)             { this.lastAction = action; }
}
