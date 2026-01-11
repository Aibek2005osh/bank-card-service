package java16.bank.api;

import java16.bank.dto.response.ApiResponse;
import java16.bank.dto.response.TransactionResponse;
import java16.bank.dto.TransferRequest;
import java16.bank.entity.User;
import java16.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request) {

        TransactionResponse transaction = transactionService.transferBetweenOwnCards(request, user);
        return ResponseEntity.ok(ApiResponse.success("Transfer completed", transaction));
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getCardTransactions(
            @AuthenticationPrincipal User user,
            @PathVariable Long cardId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TransactionResponse> transactions = transactionService.getCardTransactions(cardId, user, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        TransactionResponse transaction = transactionService.getTransactionById(id, user);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }
}