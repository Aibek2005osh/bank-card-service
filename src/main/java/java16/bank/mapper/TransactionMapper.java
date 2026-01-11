package java16.bank.mapper;


import java16.bank.dto.response.TransactionResponse;
import java16.bank.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;

        return new TransactionResponse(
                transaction.getId(),
                transaction.getSourceCard().getMaskedCardNumber(),
                transaction.getDestinationCard().getMaskedCardNumber(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}
