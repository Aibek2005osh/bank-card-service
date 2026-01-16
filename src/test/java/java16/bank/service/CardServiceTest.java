package java16.bank.service;

import java16.bank.dto.CardRequest;
import java16.bank.dto.response.CardResponse;
import java16.bank.entity.Card;
import java16.bank.entity.User;
import java16.bank.enums.CardStatus;
import java16.bank.enums.Role;
import java16.bank.mapper.CardMapper;
import java16.bank.repository.CardBlockRequestRepo;
import java16.bank.repository.CardRepo;
import java16.bank.repository.UserRepo;
import java16.bank.service.impl.CardServiceImpl;
import java16.bank.util.EncryptionUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepo cardRepo;

    @Mock
    private CardBlockRequestRepo blockRequestRepo;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private CardRequest cardRequest;
    private Card card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setEmail("test@mail.com");
        user.setRole(Role.USER);
        user.setFullName("Test User");

        cardRequest = new CardRequest();
        cardRequest.setCardNumber("1234567890123456");
        cardRequest.setCvv("123");
        cardRequest.setValidityYears(3);

        card = new Card();
        card.setId(1L);
        card.setOwner(user);
        card.setEncryptedCardNumber("encrypted_1234567890123456");
        card.setEncryptedCvv("encrypted_123");
        card.setMaskedCardNumber("**** **** **** 3456");
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
    }

    // ---------------- CREATE CARD ----------------

    @Test
    void createCard_success() {
        Mockito.when(encryptionUtil.encrypt("1234567890123456"))
                .thenReturn("encrypted_1234567890123456");
        Mockito.when(encryptionUtil.encrypt("123"))
                .thenReturn("encrypted_123");
        Mockito.when(cardRepo.existsByEncryptedCardNumber("encrypted_1234567890123456"))
                .thenReturn(false);

        Mockito.when(cardRepo.save(ArgumentMatchers.any(Card.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(cardMapper.toResponse(ArgumentMatchers.any(Card.class)))
                .thenAnswer(inv -> {
                    Card c = inv.getArgument(0);
                    return new CardResponse(
                            1L,
                            c.getMaskedCardNumber(),
                            c.getOwner().getFullName(),
                            c.getExpiryDate(),
                            c.getStatus(),
                            c.getBalance(),
                            c.getCreatedAt(),
                            c.isExpired()
                    );
                });

        CardResponse response = cardService.createCard(cardRequest, user);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("**** **** **** 3456", response.getMaskedCardNumber());
        Assertions.assertEquals(CardStatus.ACTIVE, response.getStatus());

        Mockito.verify(cardRepo).save(ArgumentMatchers.any(Card.class));
        Mockito.verify(encryptionUtil).encrypt("1234567890123456");
        Mockito.verify(encryptionUtil).encrypt("123");
    }

    @Test
    void createCard_cardAlreadyExists_throwException() {
        Mockito.when(encryptionUtil.encrypt("1234567890123456"))
                .thenReturn("encrypted_1234567890123456");
        Mockito.when(cardRepo.existsByEncryptedCardNumber("encrypted_1234567890123456"))
                .thenReturn(true);

        Assertions.assertThrows(RuntimeException.class,
                () -> cardService.createCard(cardRequest, user));

        Mockito.verify(cardRepo, Mockito.never()).save(ArgumentMatchers.any());
    }

    // ---------------- GET USER CARDS ----------------

    @Test
    void getUserCards_success() {
        Mockito.when(cardRepo.findByOwner(ArgumentMatchers.eq(user), ArgumentMatchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        Mockito.when(cardMapper.toResponse(ArgumentMatchers.any(Card.class)))
                .thenReturn(new CardResponse(
                        1L, "**** **** **** 3456", "Test User",
                        card.getExpiryDate(), CardStatus.ACTIVE,
                        BigDecimal.ZERO, null, false
                ));

        Page<CardResponse> page =
                cardService.getUserCards(user, PageRequest.of(0, 10));

        Assertions.assertEquals(1, page.getTotalElements());
    }

    // ---------------- GET CARD BY ID ----------------

    @Test
    void getCardById_success() {
        Mockito.when(cardRepo.findByIdAndOwner(1L, user))
                .thenReturn(Optional.of(card));

        Mockito.when(cardMapper.toResponse(card))
                .thenReturn(new CardResponse(1L, "**** **** **** 3456",
                        "Test User", card.getExpiryDate(),
                        CardStatus.ACTIVE, BigDecimal.ZERO, null, false));

        CardResponse response = cardService.getCardById(1L, user);

        Assertions.assertEquals(1L, response.getId());
    }

    @Test
    void getCardById_notFound() {
        Mockito.when(cardRepo.findByIdAndOwner(99L, user))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class,
                () -> cardService.getCardById(99L, user));
    }

    // ---------------- BLOCK CARD ----------------

    @Test
    void blockCard_success() {
        Mockito.when(cardRepo.findById(1L)).thenReturn(Optional.of(card));
        Mockito.when(cardRepo.save(ArgumentMatchers.any(Card.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(cardMapper.toResponse(ArgumentMatchers.any(Card.class)))
                .thenReturn(new CardResponse());

        CardResponse response = cardService.blockCard(1L);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(CardStatus.BLOCKED, card.getStatus());
    }

    // ---------------- ACTIVATE CARD ----------------

    @Test
    void activateCard_success() {
        card.setStatus(CardStatus.BLOCKED);

        Mockito.when(cardRepo.findById(1L)).thenReturn(Optional.of(card));
        Mockito.when(cardRepo.save(ArgumentMatchers.any(Card.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(cardMapper.toResponse(ArgumentMatchers.any(Card.class)))
                .thenReturn(new CardResponse());

        CardResponse response = cardService.activateCard(1L);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(CardStatus.ACTIVE, card.getStatus());
    }

    @Test
    void activateCard_expired_throwException() {
        card.setExpiryDate(LocalDate.now().minusDays(1));
        Mockito.when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        Assertions.assertThrows(RuntimeException.class,
                () -> cardService.activateCard(1L));
    }

    // ---------------- BALANCE ----------------

    @Test
    void getCardBalance_success() {
        card.setBalance(BigDecimal.valueOf(1000));
        Mockito.when(cardRepo.findByIdAndOwner(1L, user))
                .thenReturn(Optional.of(card));

        BigDecimal balance = cardService.getCardBalance(1L, user);

        Assertions.assertEquals(BigDecimal.valueOf(1000), balance);
    }
}
