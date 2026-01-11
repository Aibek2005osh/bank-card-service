package java16.bank.service;

import java16.bank.dto.response.TransactionResponse;
import java16.bank.dto.TransferRequest;
import java16.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    TransactionResponse transferBetweenOwnCards(TransferRequest request, User user);

    Page<TransactionResponse> getCardTransactions(Long cardId, User user, Pageable pageable);

    TransactionResponse getTransactionById(Long transactionId, User user);
}
