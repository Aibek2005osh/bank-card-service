package java16.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlockCardRequest {

    @NotNull(message = "Card ID is required")
    private Long cardId;

    @NotBlank(message = "Reason is required")
    private String reason;
}