package java16.bank.service.impl;


import java16.bank.dto.response.TransactionResponse;
import java16.bank.dto.TransferRequest;
import java16.bank.entity.Card;
import java16.bank.entity.Transaction;
import java16.bank.entity.User;
import java16.bank.enums.TransactionStatus;
import java16.bank.mapper.TransactionMapper;
import java16.bank.repository.CardRepo;
import java16.bank.repository.TransactionRepo;
import java16.bank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final CardRepo cardRepo;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionResponse transferBetweenOwnCards(TransferRequest request, User user) {
        Card sourceCard = cardRepo.findByIdAndOwner(request.getSourceCardId(), user)
                .orElseThrow(() -> new RuntimeException("Source card not found"));

        Card destCard = cardRepo.findByIdAndOwner(request.getDestinationCardId(), user)
                .orElseThrow(() -> new RuntimeException("Destination card not found"));

        if (!sourceCard.canTransfer(request.getAmount())) {
            throw new RuntimeException("Cannot transfer: insufficient funds or card inactive");
        }

        Transaction transaction = new Transaction();
        transaction.setSourceCard(sourceCard);
        transaction.setDestinationCard(destCard);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(TransactionStatus.PENDING);

        try {
            sourceCard.setBalance(sourceCard.getBalance().subtract(request.getAmount()));
            destCard.setBalance(destCard.getBalance().add(request.getAmount()));

            cardRepo.save(sourceCard);
            cardRepo.save(destCard);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
        }

        Transaction saved = transactionRepo.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Override
    public Page<TransactionResponse> getCardTransactions(Long cardId, User user, Pageable pageable) {
        Card card = cardRepo.findByIdAndOwner(cardId, user)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        return transactionRepo.findByCard(card, pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId, User user) {
        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        boolean isOwner = transaction.getSourceCard().getOwner().equals(user) ||
                transaction.getDestinationCard().getOwner().equals(user);

        if (!isOwner) {
            throw new RuntimeException("Access denied");
        }

        return transactionMapper.toResponse(transaction);
    }
}