package java16.bank.repository;


import java16.bank.entity.Card;
import java16.bank.entity.Transaction;
import java16.bank.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.sourceCard = :card OR t.destinationCard = :card")
    Page<Transaction> findByCard(@Param("card") Card card, Pageable pageable);

    List<Transaction> findBySourceCardAndCreatedAtBetween(
            Card sourceCard, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByStatus(TransactionStatus status);

}


