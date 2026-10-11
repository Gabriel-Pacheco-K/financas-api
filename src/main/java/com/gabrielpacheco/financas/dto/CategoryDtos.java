package com.gabrielpacheco.financas.dto;

import com.gabrielpacheco.financas.domain.Category;
import com.gabrielpacheco.financas.domain.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CategoryDtos {

    private CategoryDtos() {
    }

    public record CategoryRequest(
            @NotBlank @Size(max = 60) String name,
            @NotNull TransactionType type) {
    }

    public record CategoryResponse(Long id, String name, TransactionType type) {

        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getType());
        }
    }
}
