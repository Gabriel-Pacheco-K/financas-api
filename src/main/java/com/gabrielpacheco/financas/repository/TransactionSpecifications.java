package com.gabrielpacheco.financas.repository;

import com.gabrielpacheco.financas.domain.Transaction;
import com.gabrielpacheco.financas.domain.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Filtros opcionais da listagem de transações. Cada filtro nulo é ignorado,
 * o que evita escrever uma query para cada combinação de parâmetros.
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> ofUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> from(LocalDate start) {
        return (root, query, cb) -> start == null ? null : cb.greaterThanOrEqualTo(root.get("date"), start);
    }

    public static Specification<Transaction> until(LocalDate end) {
        return (root, query, cb) -> end == null ? null : cb.lessThanOrEqualTo(root.get("date"), end);
    }

    public static Specification<Transaction> inCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> ofType(TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("category").get("type"), type);
    }
}
