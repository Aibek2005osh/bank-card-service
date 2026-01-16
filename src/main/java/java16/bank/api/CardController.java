package java16.bank.api;

import java16.bank.dto.response.ApiResponse;
import java16.bank.dto.BlockCardRequest;
import java16.bank.dto.response.CardResponse;
import java16.bank.entity.User;
import java16.bank.enums.CardStatus;
import java16.bank.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CardResponse>>> getMyCards(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CardResponse> cards = cardService.getUserCards(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<CardResponse>>> getCardsByStatus(
            @AuthenticationPrincipal User user,
            @PathVariable CardStatus status,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<CardResponse> cards = cardService.getUserCardsByStatus(user, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardResponse>> getCardById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        CardResponse card = cardService.getCardById(id, user);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getCardBalance(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        BigDecimal balance = cardService.getCardBalance(id, user);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    @PostMapping("/request-block")
    public ResponseEntity<ApiResponse<CardResponse>> requestBlockCard(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BlockCardRequest request) {

        CardResponse card = cardService.requestBlockCard(request, user);
        return ResponseEntity.ok(ApiResponse.success("Block request submitted", card));
    }
}
