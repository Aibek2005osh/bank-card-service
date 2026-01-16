package java16.bank.api;

import java16.bank.dto.response.ApiResponse;
import java16.bank.dto.CardRequest;
import java16.bank.dto.response.CardResponse;
import java16.bank.entity.User;
import java16.bank.service.CardService;
import java16.bank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCardController {

    private final CardService cardService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<CardResponse>> createCard(@Valid @RequestBody CardRequest request) {
        User owner = userService.findById(request.getOwnerId());
        CardResponse card = cardService.createCard(request, owner);
        return ResponseEntity.ok(ApiResponse.success("Card created successfully", card));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<ApiResponse<CardResponse>> blockCard(@PathVariable Long id) {
        CardResponse card = cardService.blockCard(id);
        return ResponseEntity.ok(ApiResponse.success("Card blocked", card));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CardResponse>> activateCard(@PathVariable Long id) {
        CardResponse card = cardService.activateCard(id);
        return ResponseEntity.ok(ApiResponse.success("Card activated", card));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok(ApiResponse.success("Card deleted", null));
    }
}