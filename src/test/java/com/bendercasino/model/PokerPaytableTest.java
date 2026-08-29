package com.bendercasino.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PokerPaytableTest {

    @Test @DisplayName("nothing pays 0")
    void nothingPaysZero() {
        assertThat(PokerPaytable.payout(PokerHandCategory.NOTHING, 10)).isEqualTo(0);
    }

    @Test @DisplayName("jacks or better pays 1x bet")
    void jacksOrBetter() {
        assertThat(PokerPaytable.payout(PokerHandCategory.JACKS_OR_BETTER, 10)).isEqualTo(10);
    }

    @Test @DisplayName("flush pays 6x bet")
    void flush() {
        assertThat(PokerPaytable.payout(PokerHandCategory.FLUSH, 10)).isEqualTo(60);
    }

    @Test @DisplayName("full house pays 9x bet")
    void fullHouse() {
        assertThat(PokerPaytable.payout(PokerHandCategory.FULL_HOUSE, 10)).isEqualTo(90);
    }

    @Test @DisplayName("royal flush pays 250x bet")
    void royalFlush() {
        assertThat(PokerPaytable.payout(PokerHandCategory.ROYAL_FLUSH, 5)).isEqualTo(1250);
    }
}
