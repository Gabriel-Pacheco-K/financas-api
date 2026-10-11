package com.gabrielpacheco.financas.dto;

import com.gabrielpacheco.financas.domain.TransactionType;

import java.math.BigDecimal;

public record CategoryTotal(String category, TransactionType type, BigDecimal total) {
}
