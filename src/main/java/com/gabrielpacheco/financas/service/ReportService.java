package com.gabrielpacheco.financas.service;

import com.gabrielpacheco.financas.domain.TransactionType;
import com.gabrielpacheco.financas.dto.CategoryTotal;
import com.gabrielpacheco.financas.dto.MonthlySummary;
import com.gabrielpacheco.financas.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReportService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public MonthlySummary monthlySummary(Long userId, YearMonth month) {
        List<CategoryTotal> byCategory = transactionRepository.sumByCategory(userId, month.atDay(1), month.atEndOfMonth());

        BigDecimal income = sum(byCategory, TransactionType.INCOME);
        BigDecimal expense = sum(byCategory, TransactionType.EXPENSE);
        BigDecimal balance = income.subtract(expense);

        return new MonthlySummary(month, income, expense, balance, savingsRate(income, balance), byCategory);
    }

    /**
     * Percentual da receita que sobrou no mês. Sem receita, a taxa é zero.
     */
    private BigDecimal savingsRate(BigDecimal income, BigDecimal balance) {
        if (income.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return balance.multiply(ONE_HUNDRED).divide(income, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CategoryTotal> totals, TransactionType type) {
        return totals.stream()
                .filter(total -> total.type() == type)
                .map(CategoryTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
