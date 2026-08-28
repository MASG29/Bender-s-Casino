import com.bendercasino.dto.CardDto;
import com.bendercasino.dto.PeixinhoStateResponse;

import java.util.List;

public record AskResultResponse(
        boolean gotCards,
        List<CardDto> cardsReceived,
        boolean drewFromDeck,
        CardDto drawnCard,
        boolean formedBook,
        String message,
        PeixinhoStateResponse gameState
) {}