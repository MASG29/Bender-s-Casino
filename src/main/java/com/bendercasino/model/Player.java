package com.bendercasino.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "players")
public class Player {

    @Id
    private UUID id;
    private String name;
    private int balance;

    private int consecutiveWins;
    private int consecutiveLosses;
    private int consecutiveBlackjacks;


    private int totalWins;
    private int totalLosses;
    private int totalPushes;
    private int totalBlackjacks;

    public Player(String name) {
        this.id      = UUID.randomUUID();
        this.name    = name;
        this.balance = 1000;
    }

    protected Player() {
    }

    public void debit(int amount) {
        this.balance -= amount;
    }

    public void credit(int amount) {
        this.balance += amount;
    }

    public boolean canAfford(int amount) {
        return balance >= amount;
    }



    public void registerWin() {
        totalWins++;
        consecutiveWins++;
        consecutiveLosses    = 0;
        consecutiveBlackjacks = 0;
    }

    public void registerLoss() {
        totalLosses++;
        consecutiveLosses++;
        consecutiveWins       = 0;
        consecutiveBlackjacks = 0;
    }

    public void registerPush() {
        totalPushes++;
        consecutiveWins       = 0;
        consecutiveLosses     = 0;
        consecutiveBlackjacks = 0;
    }

    public void registerBlackjack() {
        totalBlackjacks++;
        totalWins++;
        consecutiveBlackjacks++;
        consecutiveWins++;
        consecutiveLosses = 0;
    }

    public void reset() {
        balance               = 1000;
        consecutiveWins       = 0;
        consecutiveLosses     = 0;
        consecutiveBlackjacks = 0;
        totalWins             = 0;
        totalLosses           = 0;
        totalPushes           = 0;
        totalBlackjacks       = 0;
    }

    // --- getters ---

    public UUID getId()                   { return id; }
    public String getName()               { return name; }
    public int getBalance()               { return balance; }
    public int getConsecutiveWins()       { return consecutiveWins; }
    public int getConsecutiveLosses()     { return consecutiveLosses; }
    public int getConsecutiveBlackjacks() { return consecutiveBlackjacks; }
    public int getTotalWins()             { return totalWins; }
    public int getTotalLosses()           { return totalLosses; }
    public int getTotalPushes()           { return totalPushes; }
    public int getTotalBlackjacks()       { return totalBlackjacks; }
}
