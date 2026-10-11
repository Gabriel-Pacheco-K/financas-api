package com.gabrielpacheco.financas.service;

import com.gabrielpacheco.financas.domain.Category;
import com.gabrielpacheco.financas.domain.Transaction;
import com.gabrielpacheco.financas.domain.TransactionType;
import com.gabrielpacheco.financas.dto.TransactionDtos.TransactionRequest;
import com.gabrielpacheco.financas.dto.TransactionDtos.TransactionResponse;
import com.gabrielpacheco.financas.exception.BusinessException;
import com.gabrielpacheco.financas.exception.ResourceNotFoundException;
import com.gabrielpacheco.financas.repository.TransactionRepository;
import com.gabrielpacheco.financas.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.gabrielpacheco.financas.repository.TransactionSpecifications.from;
import static com.gabrielpacheco.financas.repository.TransactionSpecifications.inCategory;
import static com.gabrielpacheco.financas.repository.TransactionSpecifications.ofType;
import static com.gabrielpacheco.financas.repository.TransactionSpecifications.ofUser;
import static com.gabrielpacheco.financas.repository.TransactionSpecifications.until;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> search(Long userId, LocalDate start, LocalDate end,
                                            Long categoryId, TransactionType type, Pageable pageable) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException("A data inicial deve ser anterior ou igual à data final.");
        }

        Specification<Transaction> filters = Specification.where(ofUser(userId))
                .and(from(start))
                .and(until(end))
                .and(inCategory(categoryId))
                .and(ofType(type));

        return transactionRepository.findAll(filters, pageable).map(TransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(Long userId, Long id) {
        return TransactionResponse.from(findOwned(userId, id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Category category = categoryService.findOwned(userId, request.categoryId());
        Transaction transaction = new Transaction(
                request.description().trim(),
                request.amount(),
                request.date(),
                category,
                userRepository.getReferenceById(userId));
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = findOwned(userId, id);
        Category category = categoryService.findOwned(userId, request.categoryId());
        transaction.update(request.description().trim(), request.amount(), request.date(), category);
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        transactionRepository.delete(findOwned(userId, id));
    }

    private Transaction findOwned(Long userId, Long id) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação " + id + " não encontrada."));
    }
}
