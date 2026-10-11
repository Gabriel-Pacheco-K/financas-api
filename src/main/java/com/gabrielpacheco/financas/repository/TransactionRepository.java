package com.gabrielpacheco.financas.repository;

import com.gabrielpacheco.financas.domain.Transaction;
import com.gabrielpacheco.financas.dto.CategoryTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            select new com.gabrielpacheco.financas.dto.CategoryTotal(c.name, c.type, sum(t.amount))
            from Transaction t join t.category c
            where t.user.id = :userId and t.date between :start and :end
            group by c.name, c.type
            order by sum(t.amount) desc
            """)
    List<CategoryTotal> sumByCategory(@Param("userId") Long userId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);
}
