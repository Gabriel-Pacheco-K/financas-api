package com.gabrielpacheco.financas.dto;

import com.gabrielpacheco.financas.domain.Transaction;
import com.gabrielpacheco.financas.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TransactionDtos {

    private TransactionDtos() {
    }

    public record TransactionRequest(
            @NotBlank @Size(max = 150) String description,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
            @NotNull LocalDate date,
            @NotNull Long categoryId) {
    }

    public record TransactionResponse(
            Long id,
            String description,
            BigDecimal amount,
            LocalDate date,
            TransactionType type,
            CategoryDtos.CategoryResponse category) {

        public static TransactionResponse from(Transaction transaction) {
            return new TransactionResponse(
                    transaction.getId(),
                    transaction.getDescription(),
                    transaction.getAmount(),
                    transaction.getDate(),
                    transaction.getType(),
                    CategoryDtos.CategoryResponse.from(transaction.getCategory()));
        }
    }
}
