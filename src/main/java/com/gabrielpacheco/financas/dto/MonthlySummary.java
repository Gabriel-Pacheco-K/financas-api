package com.gabrielpacheco.financas.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlySummary(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        BigDecimal savingsRate,
        List<CategoryTotal> byCategory) {
}
