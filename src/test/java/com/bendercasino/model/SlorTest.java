package com.bendercasino.model;

import com.bendercasino.model.slots.Slot;
import com.bendercasino.model.slots.Symbol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlorTest {

    private Slot slot;

    @BeforeEach
    void setUp() {
        slot = new Slot();
    }

    @Test
    public void checkThatValuesAreProperlyAssigned() {
        Map<Symbol, Double> values = {
                Symbol.CHERRY,
        }
        assertEquals()
    }


    @AfterEach
    void close() {
        slot = null;
    }
}
