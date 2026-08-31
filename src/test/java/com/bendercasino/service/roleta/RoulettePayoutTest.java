package com.bendercasino.service.roleta;

import com.bendercasino.model.roleta.BetType;
import com.bendercasino.model.roleta.Colour;

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
        assertThat(RoulettePayout.payout(100, BetType.RED, 1)).isEqualTo(200);
    }

    @Test @DisplayName("aposta preto, sai preto, paga 1:1 (devolve o dobro)")
    void winningBlackBetPaysDouble() {
        assertThat(RoulettePayout.payout(100, BetType.BLACK, 2)).isEqualTo(200);
    }

    @Test @DisplayName("aposta vermelho, sai preto, perde tudo")
    void losingBetPaysNothing() {
        assertThat(RoulettePayout.payout(100, BetType.RED, 2)).isEqualTo(0);
    }

    @Test @DisplayName("aposta vermelho, sai zero, perde tudo (vantagem da casa)")
    void zeroAlwaysLoses() {
        assertThat(RoulettePayout.payout(100, BetType.RED, 0)).isEqualTo(0);
    }

    @Test @DisplayName("aposta preto, sai zero, perde tudo (vantagem da casa)")
    void zeroAlwaysLosesForBlackToo() {
        assertThat(RoulettePayout.payout(100, BetType.BLACK, 0)).isEqualTo(0);
    }

    @Test @DisplayName("aposta impar, sai numero impar, paga 1:1")
    void winningOddBetPaysDouble() {
        assertThat(RoulettePayout.payout(100, BetType.ODD, 3)).isEqualTo(200);
    }

    @Test @DisplayName("aposta impar, sai numero par, perde tudo")
    void losingOddBetPaysNothing() {
        assertThat(RoulettePayout.payout(100, BetType.ODD, 4)).isEqualTo(0);
    }

    @Test @DisplayName("aposta par, sai numero par, paga 1:1")
    void winningEvenBetPaysDouble() {
        assertThat(RoulettePayout.payout(100, BetType.EVEN, 4)).isEqualTo(200);
    }

    @Test @DisplayName("aposta par, sai numero impar, perde tudo")
    void losingEvenBetPaysNothing() {
        assertThat(RoulettePayout.payout(100, BetType.EVEN, 3)).isEqualTo(0);
    }

    @Test @DisplayName("zero nao conta nem como par nem como impar")
    void zeroLosesOddAndEvenBets() {
        assertThat(RoulettePayout.payout(100, BetType.ODD, 0)).isEqualTo(0);
        assertThat(RoulettePayout.payout(100, BetType.EVEN, 0)).isEqualTo(0);
    }

    @Test @DisplayName("aposta 1 a 18, sai numero nesse intervalo, paga 1:1")
    void winningLowBetPaysDouble() {
        assertThat(RoulettePayout.payout(100, BetType.LOW, 1)).isEqualTo(200);
        assertThat(RoulettePayout.payout(100, BetType.LOW, 18)).isEqualTo(200);
    }

    @Test @DisplayName("aposta 1 a 18, sai numero fora do intervalo, perde tudo")
    void losingLowBetPaysNothing() {
        assertThat(RoulettePayout.payout(100, BetType.LOW, 19)).isEqualTo(0);
        assertThat(RoulettePayout.payout(100, BetType.LOW, 0)).isEqualTo(0);
    }

    @Test @DisplayName("aposta 19 a 36, sai numero nesse intervalo, paga 1:1")
    void winningHighBetPaysDouble() {
        assertThat(RoulettePayout.payout(100, BetType.HIGH, 19)).isEqualTo(200);
        assertThat(RoulettePayout.payout(100, BetType.HIGH, 36)).isEqualTo(200);
    }

    @Test @DisplayName("aposta 19 a 36, sai numero fora do intervalo, perde tudo")
    void losingHighBetPaysNothing() {
        assertThat(RoulettePayout.payout(100, BetType.HIGH, 18)).isEqualTo(0);
        assertThat(RoulettePayout.payout(100, BetType.HIGH, 0)).isEqualTo(0);
    }
}
