package com.gabrielpacheco.financas.service;

import com.gabrielpacheco.financas.dto.CategoryTotal;
import com.gabrielpacheco.financas.dto.MonthlySummary;
import com.gabrielpacheco.financas.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.gabrielpacheco.financas.domain.TransactionType.EXPENSE;
import static com.gabrielpacheco.financas.domain.TransactionType.INCOME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void calculatesBalanceAndSavingsRate() {
        when(transactionRepository.sumByCategory(1L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(
                        new CategoryTotal("Salário", INCOME, new BigDecimal("5000.00")),
                        new CategoryTotal("Aluguel", EXPENSE, new BigDecimal("1500.00")),
                        new CategoryTotal("Mercado", EXPENSE, new BigDecimal("800.00"))));

        MonthlySummary summary = reportService.monthlySummary(1L, OCTOBER);

        assertThat(summary.totalIncome()).isEqualByComparingTo("5000.00");
        assertThat(summary.totalExpense()).isEqualByComparingTo("2300.00");
        assertThat(summary.balance()).isEqualByComparingTo("2700.00");
        assertThat(summary.savingsRate()).isEqualByComparingTo("54.00");
        assertThat(summary.byCategory()).hasSize(3);
    }

    @Test
    void savingsRateIsZeroWhenThereIsNoIncome() {
        when(transactionRepository.sumByCategory(1L, OCTOBER.atDay(1), OCTOBER.atEndOfMonth()))
                .thenReturn(List.of(new CategoryTotal("Mercado", EXPENSE, new BigDecimal("300.00"))));

        MonthlySummary summary = reportService.monthlySummary(1L, OCTOBER);

        assertThat(summary.balance()).isEqualByComparingTo("-300.00");
        assertThat(summary.savingsRate()).isEqualByComparingTo("0");
    }

    @Test
    void emptyMonthReturnsZeros() {
        when(transactionRepository.sumByCategory(1L, OCTOBER.atDay(1), OCTOBER.atEndOfMonth())).thenReturn(List.of());

        MonthlySummary summary = reportService.monthlySummary(1L, OCTOBER);

        assertThat(summary.totalIncome()).isEqualByComparingTo("0");
        assertThat(summary.totalExpense()).isEqualByComparingTo("0");
        assertThat(summary.byCategory()).isEmpty();
    }
}
