package com.bendercasino.util;

import com.bendercasino.client.dto.ApiCard;
import com.bendercasino.dto.CardDto;
import com.bendercasino.dto.blackjack.HandDto;
import com.bendercasino.model.Card;
import com.bendercasino.model.blackjack.Hand;
import java.util.List;

public class CardMapper {
    private CardMapper() {}

    public static Card toDomain(ApiCard api) {
        return new Card(api.code(), api.value(), api.suit(), api.image());
    }

    public static CardDto toDto(Card card) {
        return new CardDto(card.code(), card.value(), card.suit(), card.image());
    }

    public static HandDto toDto(Hand hand) {
        List<CardDto> cards = hand.getCards().stream().map(CardMapper::toDto).toList();
        return new HandDto(cards, hand.value(), hand.isSoft(), hand.isBlackjack(), hand.isBusted());
    }

    public static HandDto toDtoDealerHidden(Hand hand) {
        List<CardDto> visible = hand.getCards().stream().limit(1).map(CardMapper::toDto).toList();
        return new HandDto(visible, 0, false, false, false);
    }
}
