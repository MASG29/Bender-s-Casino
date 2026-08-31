package com.bendercasino.model.slots;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlotResultTest {

    @Test
    void threeOfAKind_winsAtSymbolValue() {
        SlotResult result = new SlotResult(new Symbol[]{Symbol.BELL, Symbol.BELL, Symbol.BELL});

        assertThat(result.getMultiplier()).isEqualTo(4.0);
        assertThat(result.getOutcome()).isEqualTo(Outcome.WIN);
    }

    @Test
    void twoOfAKindOnFirstReels_paysHalfSymbolValue() {
        SlotResult result = new SlotResult(new Symbol[]{Symbol.BAR, Symbol.BAR, Symbol.CHERRY});

        assertThat(result.getMultiplier()).isEqualTo(0.75);
        assertThat(result.getOutcome()).isEqualTo(Outcome.CONSOLATION);
    }

    @Test
    void twoOfAKindOnLastReels_paysHalfSymbolValue() {
        SlotResult result = new SlotResult(new Symbol[]{Symbol.CHERRY, Symbol.BAR, Symbol.BAR});

        assertThat(result.getMultiplier()).isEqualTo(0.75);
        assertThat(result.getOutcome()).isEqualTo(Outcome.CONSOLATION);
    }

    @Test
    void allDifferent_paysNothing() {
        SlotResult result = new SlotResult(new Symbol[]{Symbol.CHERRY, Symbol.BAR, Symbol.BELL});

        assertThat(result.getMultiplier()).isEqualTo(0.0);
        assertThat(result.getOutcome()).isEqualTo(Outcome.LOSS);
    }
}
