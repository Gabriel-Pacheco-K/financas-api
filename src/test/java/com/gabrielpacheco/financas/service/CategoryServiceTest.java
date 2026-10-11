package com.gabrielpacheco.financas.service;

import com.gabrielpacheco.financas.domain.Category;
import com.gabrielpacheco.financas.domain.User;
import com.gabrielpacheco.financas.dto.CategoryDtos.CategoryRequest;
import com.gabrielpacheco.financas.exception.BusinessException;
import com.gabrielpacheco.financas.exception.ResourceNotFoundException;
import com.gabrielpacheco.financas.repository.CategoryRepository;
import com.gabrielpacheco.financas.repository.TransactionRepository;
import com.gabrielpacheco.financas.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.gabrielpacheco.financas.domain.TransactionType.EXPENSE;
import static com.gabrielpacheco.financas.domain.TransactionType.INCOME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private final User user = new User("Gabriel", "gabriel@email.com", "hash");

    @Test
    void rejectsDuplicateNameIgnoringCase() {
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Mercado")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(1L, new CategoryRequest("  Mercado ", EXPENSE)))
                .isInstanceOf(BusinessException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void doesNotDeleteCategoryWithTransactions() {
        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(new Category("Mercado", EXPENSE, user)));
        when(transactionRepository.existsByCategoryId(5L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L, 5L)).isInstanceOf(BusinessException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void doesNotChangeTypeOfCategoryWithTransactions() {
        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(new Category("Freela", EXPENSE, user)));
        when(transactionRepository.existsByCategoryId(5L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(1L, 5L, new CategoryRequest("Freela", INCOME)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tipo");
    }

    @Test
    void renamesCategoryWithoutTransactionsCheck() {
        Category category = new Category("Mercado", EXPENSE, user);
        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(category));

        var response = categoryService.update(1L, 5L, new CategoryRequest("Supermercado", EXPENSE));

        assertThat(response.name()).isEqualTo("Supermercado");
        verify(transactionRepository, never()).existsByCategoryId(any());
    }

    @Test
    void categoryOfAnotherUserIsNotFound() {
        when(categoryRepository.findByIdAndUserId(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(2L, 5L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
