package java16.bank.mapper;

import java16.bank.dto.response.CardResponse;
import java16.bank.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardResponse toResponse(Card card) {
        if (card == null) return null;

        return new CardResponse(
                card.getId(),
                card.getMaskedCardNumber(),
                card.getOwner().getFullName(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getCreatedAt(),
                card.isExpired()
        );
    }
}

