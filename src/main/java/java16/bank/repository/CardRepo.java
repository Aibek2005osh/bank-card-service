package java16.bank.repository;

import java16.bank.entity.Card;
import java16.bank.entity.User;
import java16.bank.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepo extends JpaRepository<Card, Long> {
    Page<Card> findByOwner(User owner, Pageable pageable);
    Page<Card> findByOwnerAndStatus(User owner, CardStatus status, Pageable pageable);
    Optional<Card> findByIdAndOwner(Long id, User owner);
    Optional<Card> findByEncryptedCardNumber(String encryptedCardNumber);
    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
}