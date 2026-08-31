package com.bendercasino.model.videopoker;

import com.bendercasino.model.Card;

import java.util.Arrays;
import java.util.List;

public class PokerHandEvaluator {

    private PokerHandEvaluator() {}

    public static PokerHandCategory evaluate(List<Card> cards) {
        if (cards == null || cards.size() != 5) {
            throw new IllegalArgumentException("Video poker hands must have 5 cards");
        }

        int[] ranks = new int[5];
        for (int i = 0; i < 5; i++) {
            ranks[i] = rank(cards.get(i));
        }
        Arrays.sort(ranks);

        boolean flush = isFlush(cards);
        boolean straight = isStraight(ranks);
        int[] counts = rankCounts(ranks);

        int pairs = 0;
        int pairRank = 0;
        int trips = 0;
        int quads = 0;
        for (int r = 2; r <= 14; r++) {
            if (counts[r] == 2) {
                pairs++;
                pairRank = r;
            } else if (counts[r] == 3) {
                trips++;
            } else if (counts[r] == 4) {
                quads++;
            }
        }

        if (flush && straight && ranks[0] == 10) {
            return PokerHandCategory.ROYAL_FLUSH;
        }
        if (flush && straight) {
            return PokerHandCategory.STRAIGHT_FLUSH;
        }
        if (quads == 1) {
            return PokerHandCategory.FOUR_OF_A_KIND;
        }
        if (trips == 1 && pairs == 1) {
            return PokerHandCategory.FULL_HOUSE;
        }
        if (flush) {
            return PokerHandCategory.FLUSH;
        }
        if (straight) {
            return PokerHandCategory.STRAIGHT;
        }
        if (trips == 1) {
            return PokerHandCategory.THREE_OF_A_KIND;
        }
        if (pairs == 2) {
            return PokerHandCategory.TWO_PAIR;
        }
        if (pairs == 1 && pairRank >= 11) {
            return PokerHandCategory.JACKS_OR_BETTER;
        }
        return PokerHandCategory.NOTHING;
    }

    private static boolean isFlush(List<Card> cards) {
        String suit = cards.get(0).suit();
        for (Card card : cards) {
            if (!card.suit().equals(suit)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStraight(int[] sortedRanks) {
        if (sortedRanks[0] == 2
                && sortedRanks[1] == 3
                && sortedRanks[2] == 4
                && sortedRanks[3] == 5
                && sortedRanks[4] == 14) {
            return true;
        }
        for (int i = 0; i < 4; i++) {
            if (sortedRanks[i + 1] != sortedRanks[i] + 1) {
                return false;
            }
        }
        return true;
    }

    private static int[] rankCounts(int[] ranks) {
        int[] counts = new int[15];
        for (int rank : ranks) {
            counts[rank]++;
        }
        return counts;
    }

    private static int rank(Card card) {
        return switch (card.value()) {
            case "ACE" -> 14;
            case "KING" -> 13;
            case "QUEEN" -> 12;
            case "JACK" -> 11;
            default -> Integer.parseInt(card.value());
        };
    }
}
