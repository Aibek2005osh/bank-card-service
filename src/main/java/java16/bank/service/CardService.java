package java16.bank.service;

import java16.bank.dto.BlockCardRequest;
import java16.bank.dto.CardRequest;
import java16.bank.dto.CardResponse;
import java16.bank.entity.User;
import java16.bank.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CardService {

    CardResponse createCard(CardRequest request, User owner);

    Page<CardResponse> getUserCards(User user, Pageable pageable);

    Page<CardResponse> getUserCardsByStatus(User user, CardStatus status, Pageable pageable);

    CardResponse getCardById(Long cardId, User user);

    CardResponse blockCard(Long cardId);

    CardResponse activateCard(Long cardId);

    void deleteCard(Long cardId);

    CardResponse requestBlockCard(BlockCardRequest request, User user);

    BigDecimal getCardBalance(Long cardId, User user);
}

