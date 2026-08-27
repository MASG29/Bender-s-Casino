package com.bendercasino.service;

import com.bendercasino.model.Colour;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoulettePayoutTest {

    @Test @DisplayName("1 e vermelho")
    void oneIsRed() {
        assertThat(RoulettePayout.colourOf(1)).isEqualTo(Colour.RED);
    }

    @Test @DisplayName("2 e preto")
    void twoIsBlack() {
        assertThat(RoulettePayout.colourOf(2)).isEqualTo(Colour.BLACK);
    }

    @Test @DisplayName("36 e vermelho")
    void thirtySixIsRed() {
        assertThat(RoulettePayout.colourOf(36)).isEqualTo(Colour.RED);
    }

    @Test @DisplayName("35 e preto")
    void thirtyFiveIsBlack() {
        assertThat(RoulettePayout.colourOf(35)).isEqualTo(Colour.BLACK);
    }

    @Test @DisplayName("0 e verde")
    void zeroIsGreen() {
        assertThat(RoulettePayout.colourOf(0)).isEqualTo(Colour.GREEN);
    }

    @Test @DisplayName("numero negativo lanca IllegalArgumentException")
    void negativeNumberThrows() {
        assertThatThrownBy(() -> RoulettePayout.colourOf(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test @DisplayName("numero acima de 36 lanca IllegalArgumentException")
    void tooHighNumberThrows() {
        assertThatThrownBy(() -> RoulettePayout.colourOf(37))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test @DisplayName("aposta vermelho, sai vermelho, paga 1:1 (devolve o dobro)")
    void winningRedBetPaysDouble() {
        assertThat(RoulettePayout.payout(100, Colour.RED, 1)).isEqualTo(200);
    }

    @Test @DisplayName("aposta preto, sai preto, paga 1:1 (devolve o dobro)")
    void winningBlackBetPaysDouble() {
        assertThat(RoulettePayout.payout(100, Colour.BLACK, 2)).isEqualTo(200);
    }

    @Test @DisplayName("aposta vermelho, sai preto, perde tudo")
    void losingBetPaysNothing() {
        assertThat(RoulettePayout.payout(100, Colour.RED, 2)).isEqualTo(0);
    }

    @Test @DisplayName("aposta vermelho, sai zero, perde tudo (vantagem da casa)")
    void zeroAlwaysLoses() {
        assertThat(RoulettePayout.payout(100, Colour.RED, 0)).isEqualTo(0);
    }

    @Test @DisplayName("aposta preto, sai zero, perde tudo (vantagem da casa)")
    void zeroAlwaysLosesForBlackToo() {
        assertThat(RoulettePayout.payout(100, Colour.BLACK, 0)).isEqualTo(0);
    }

    @Test @DisplayName("apostar em verde nao e permitido")
    void greenBetThrows() {
        assertThatThrownBy(() -> RoulettePayout.payout(100, Colour.GREEN, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
