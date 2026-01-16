package java16.bank.service;

import java16.bank.dto.TransferRequest;
import java16.bank.dto.response.TransactionResponse;
import java16.bank.entity.Card;
import java16.bank.entity.Transaction;
import java16.bank.entity.User;
import java16.bank.enums.CardStatus;
import java16.bank.enums.Role;
import java16.bank.enums.TransactionStatus;
import java16.bank.mapper.TransactionMapper;
import java16.bank.repository.CardRepo;
import java16.bank.repository.TransactionRepo;
import java16.bank.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private CardRepo cardRepo;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Card sourceCard;
    private Card destCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setEmail("test@mail.com");
        user.setRole(Role.USER);
        user.setFullName("Test User");

        sourceCard = new Card();
        sourceCard.setId(10L);
        sourceCard.setOwner(user);
        sourceCard.setMaskedCardNumber("**** **** **** 1111");
        sourceCard.setBalance(BigDecimal.valueOf(1000));
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setExpiryDate(LocalDate.now().plusYears(1));

        destCard = new Card();
        destCard.setId(20L);
        destCard.setOwner(user);
        destCard.setMaskedCardNumber("**** **** **** 2222");
        destCard.setBalance(BigDecimal.valueOf(100));
        destCard.setStatus(CardStatus.ACTIVE);
        destCard.setExpiryDate(LocalDate.now().plusYears(1));

        transferRequest = new TransferRequest();
        transferRequest.setSourceCardId(10L);
        transferRequest.setDestinationCardId(20L);
        transferRequest.setAmount(BigDecimal.valueOf(200));
        transferRequest.setDescription("my transfer");
    }

    // ---------------- transferBetweenOwnCards ----------------

    @Test
    void transferBetweenOwnCards_success_completed() {
        when(cardRepo.findByIdAndOwner(10L, user)).thenReturn(Optional.of(sourceCard));
        when(cardRepo.findByIdAndOwner(20L, user)).thenReturn(Optional.of(destCard));

        // cardRepo.save(source) and save(dest) succeed
        when(cardRepo.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        // transactionRepo.save returns the same object (like persist)
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        when(transactionMapper.toResponse(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return new TransactionResponse(
                    t.getId(),
                    t.getSourceCard().getMaskedCardNumber(),
                    t.getDestinationCard().getMaskedCardNumber(),
                    t.getAmount(),
                    t.getStatus(),
                    t.getDescription(),
                    t.getCreatedAt(),
                    t.getCompletedAt()
            );
        });

        TransactionResponse resp = transactionService.transferBetweenOwnCards(transferRequest, user);

        assertNotNull(resp);
        assertEquals(TransactionStatus.COMPLETED, resp.getStatus());
        assertEquals(BigDecimal.valueOf(200), resp.getAmount());
        assertEquals("**** **** **** 1111", resp.getSourceCardMasked());
        assertEquals("**** **** **** 2222", resp.getDestinationCardMasked());

        // balances changed
        assertEquals(BigDecimal.valueOf(800), sourceCard.getBalance());
        assertEquals(BigDecimal.valueOf(300), destCard.getBalance());

        verify(cardRepo).save(sourceCard);
        verify(cardRepo).save(destCard);
        verify(transactionRepo).save(any(Transaction.class));
    }

    @Test
    void transferBetweenOwnCards_sourceNotFound_throwException() {
        when(cardRepo.findByIdAndOwner(10L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> transactionService.transferBetweenOwnCards(transferRequest, user));

        verify(transactionRepo, never()).save(any());
        verify(cardRepo, never()).save(any());
    }

    @Test
    void transferBetweenOwnCards_destinationNotFound_throwException() {
        when(cardRepo.findByIdAndOwner(10L, user)).thenReturn(Optional.of(sourceCard));
        when(cardRepo.findByIdAndOwner(20L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> transactionService.transferBetweenOwnCards(transferRequest, user));

        verify(transactionRepo, never()).save(any());
        verify(cardRepo, never()).save(any());
    }

    @Test
    void transferBetweenOwnCards_cannotTransfer_throwException() {
        // insufficient funds
        sourceCard.setBalance(BigDecimal.valueOf(10));

        when(cardRepo.findByIdAndOwner(10L, user)).thenReturn(Optional.of(sourceCard));
        when(cardRepo.findByIdAndOwner(20L, user)).thenReturn(Optional.of(destCard));

        assertThrows(RuntimeException.class,
                () -> transactionService.transferBetweenOwnCards(transferRequest, user));

        verify(transactionRepo, never()).save(any());
        verify(cardRepo, never()).save(any());
    }

    @Test
    void transferBetweenOwnCards_whenCardRepoSaveFails_transactionFailedButSaved() {
        when(cardRepo.findByIdAndOwner(10L, user)).thenReturn(Optional.of(sourceCard));
        when(cardRepo.findByIdAndOwner(20L, user)).thenReturn(Optional.of(destCard));

        // fail on saving card (simulate DB error)
        when(cardRepo.save(any(Card.class))).thenThrow(new RuntimeException("DB error"));

        // transaction still saved with FAILED
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        when(transactionMapper.toResponse(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return new TransactionResponse(
                    t.getId(),
                    t.getSourceCard().getMaskedCardNumber(),
                    t.getDestinationCard().getMaskedCardNumber(),
                    t.getAmount(),
                    t.getStatus(),
                    t.getDescription(),
                    t.getCreatedAt(),
                    t.getCompletedAt()
            );
        });

        TransactionResponse resp = transactionService.transferBetweenOwnCards(transferRequest, user);

        assertNotNull(resp);
        assertEquals(TransactionStatus.FAILED, resp.getStatus());

        verify(transactionRepo).save(argThat(t ->
                t.getStatus() == TransactionStatus.FAILED &&
                        t.getErrorMessage() != null
        ));
    }

    // ---------------- getCardTransactions ----------------

    @Test
    void getCardTransactions_success() {
        when(cardRepo.findByIdAndOwner(10L, user)).thenReturn(Optional.of(sourceCard));

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setSourceCard(sourceCard);
        tx.setDestinationCard(destCard);
        tx.setAmount(BigDecimal.valueOf(50));
        tx.setStatus(TransactionStatus.COMPLETED);

        when(transactionRepo.findByCard(eq(sourceCard), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(tx)));

        when(transactionMapper.toResponse(any(Transaction.class)))
                .thenReturn(new TransactionResponse(
                        1L, "**** **** **** 1111", "**** **** **** 2222",
                        BigDecimal.valueOf(50), TransactionStatus.COMPLETED,
                        null, null, null
                ));

        Page<TransactionResponse> page =
                transactionService.getCardTransactions(10L, user, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("**** **** **** 1111", page.getContent().get(0).getSourceCardMasked());
    }

    @Test
    void getCardTransactions_cardNotFound_throwException() {
        when(cardRepo.findByIdAndOwner(10L, user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> transactionService.getCardTransactions(10L, user, PageRequest.of(0, 10)));
    }

    // ---------------- getTransactionById ----------------

    @Test
    void getTransactionById_success_ownerIsSource() {
        Transaction tx = new Transaction();
        tx.setId(5L);
        tx.setSourceCard(sourceCard);
        tx.setDestinationCard(destCard);

        when(transactionRepo.findById(5L)).thenReturn(Optional.of(tx));
        when(transactionMapper.toResponse(tx)).thenReturn(new TransactionResponse());

        TransactionResponse resp = transactionService.getTransactionById(5L, user);

        assertNotNull(resp);
        verify(transactionMapper).toResponse(tx);
    }

    @Test
    void getTransactionById_accessDenied_throwException() {
        User другой = new User();
        другой.setId(99L);
        другой.setFullName("Other");

        Card чужаяКарта = new Card();
        чужаяКарта.setOwner(другой);

        Transaction tx = new Transaction();
        tx.setId(7L);
        tx.setSourceCard(чужаяКарта);
        tx.setDestinationCard(чужаяКарта);

        when(transactionRepo.findById(7L)).thenReturn(Optional.of(tx));

        assertThrows(RuntimeException.class,
                () -> transactionService.getTransactionById(7L, user));

        verify(transactionMapper, never()).toResponse(any());
    }

    @Test
    void getTransactionById_notFound_throwException() {
        when(transactionRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> transactionService.getTransactionById(999L, user));
    }
}
