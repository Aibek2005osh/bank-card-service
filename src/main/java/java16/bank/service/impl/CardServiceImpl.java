package java16.bank.service.impl;


import java16.bank.dto.BlockCardRequest;
import java16.bank.dto.CardRequest;
import java16.bank.dto.CardResponse;
import java16.bank.entity.Card;
import java16.bank.entity.CardBlockRequest;
import java16.bank.entity.User;
import java16.bank.enums.CardStatus;
import java16.bank.enums.RequestStatus;
import java16.bank.mapper.CardMapper;
import java16.bank.repository.CardBlockRequestRepo;
import java16.bank.repository.CardRepo;
import java16.bank.repository.UserRepo;
import java16.bank.service.CardService;
import java16.bank.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepo cardRepo;
    private final UserRepo userRepo;
    private final CardBlockRequestRepo blockRequestRepo;
    private final EncryptionUtil encryptionUtil;
    private final CardMapper cardMapper;

    @Override
    @Transactional
    public CardResponse createCard(CardRequest request, User owner) {
        String encrypted = encryptionUtil.encrypt(request.getCardNumber());

        if (cardRepo.existsByEncryptedCardNumber(encrypted)) {
            throw new RuntimeException("Card number already exists");
        }

        Card card = new Card();
        card.setOwner(owner);
        card.setEncryptedCardNumber(encrypted);
        card.setMaskedCardNumber(maskCardNumber(request.getCardNumber()));
        card.setEncryptedCvv(encryptionUtil.encrypt(request.getCvv()));
        card.setExpiryDate(LocalDate.now().plusYears(request.getValidityYears()));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        Card saved = cardRepo.save(card);
        return cardMapper.toResponse(saved);
    }

    @Override
    public Page<CardResponse> getUserCards(User user, Pageable pageable) {
        return cardRepo.findByOwner(user, pageable)
                .map(cardMapper::toResponse);
    }

    @Override
    public Page<CardResponse> getUserCardsByStatus(User user, CardStatus status, Pageable pageable) {
        return cardRepo.findByOwnerAndStatus(user, status, pageable)
                .map(cardMapper::toResponse);
    }

    @Override
    public CardResponse getCardById(Long cardId, User user) {
        Card card = cardRepo.findByIdAndOwner(cardId, user)
                .orElseThrow(() -> new RuntimeException("Card not found or access denied"));
        return cardMapper.toResponse(card);
    }

    @Override
    @Transactional
    public CardResponse blockCard(Long cardId) {
        Card card = cardRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.BLOCKED);
        card.setBlockedAt(LocalDateTime.now());
        Card saved = cardRepo.save(card);
        return cardMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CardResponse activateCard(Long cardId) {
        Card card = cardRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.isExpired()) {
            throw new RuntimeException("Cannot activate expired card");
        }

        card.setStatus(CardStatus.ACTIVE);
        card.setBlockedAt(null);
        Card saved = cardRepo.save(card);
        return cardMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepo.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        cardRepo.delete(card);
    }

    @Override
    @Transactional
    public CardResponse requestBlockCard(BlockCardRequest request, User user) {
        Card card = cardRepo.findByIdAndOwner(request.getCardId(), user)
                .orElseThrow(() -> new RuntimeException("Card not found or access denied"));

        CardBlockRequest blockRequest = new CardBlockRequest();
        blockRequest.setCard(card);
        blockRequest.setUser(user);
        blockRequest.setReason(request.getReason());
        blockRequest.setStatus(RequestStatus.PENDING);
        blockRequestRepo.save(blockRequest);

        return cardMapper.toResponse(card);
    }

    @Override
    public BigDecimal getCardBalance(Long cardId, User user) {
        Card card = cardRepo.findByIdAndOwner(cardId, user)
                .orElseThrow(() -> new RuntimeException("Card not found or access denied"));
        return card.getBalance();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() != 16) {
            throw new RuntimeException("Invalid card number length");
        }
        return "**** **** **** " + cardNumber.substring(12);
    }
}
