package com.bendercasino.model.slots;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlotTest {

    private Slot slot;

    @BeforeEach
    void setUp() {
        slot = new Slot();
    }

    @Test
    void containsEverySymbolExceptNone() {
        assertThat(slot.getMultipliers())
                .containsExactlyInAnyOrder(
                        Symbol.CHERRY, Symbol.BAR, Symbol.DOUBLE_BAR, Symbol.TRIPLE_BAR,
                        Symbol.BELL, Symbol.LEELA, Symbol.FRY, Symbol.BENDER)
                .doesNotContain(Symbol.NONE);
    }
}
