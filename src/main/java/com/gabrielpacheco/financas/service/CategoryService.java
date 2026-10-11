package com.gabrielpacheco.financas.service;

import com.gabrielpacheco.financas.domain.Category;
import com.gabrielpacheco.financas.domain.User;
import com.gabrielpacheco.financas.dto.CategoryDtos.CategoryRequest;
import com.gabrielpacheco.financas.dto.CategoryDtos.CategoryResponse;
import com.gabrielpacheco.financas.exception.BusinessException;
import com.gabrielpacheco.financas.exception.ResourceNotFoundException;
import com.gabrielpacheco.financas.repository.CategoryRepository;
import com.gabrielpacheco.financas.repository.TransactionRepository;
import com.gabrielpacheco.financas.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long userId) {
        return categoryRepository.findAllByUserIdOrderByName(userId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new BusinessException("Você já tem uma categoria chamada '" + name + "'.");
        }

        User user = userRepository.getReferenceById(userId);
        return CategoryResponse.from(categoryRepository.save(new Category(name, request.type(), user)));
    }

    @Transactional
    public CategoryResponse update(Long userId, Long id, CategoryRequest request) {
        Category category = findOwned(userId, id);
        String name = request.name().trim();

        boolean renamed = !category.getName().equalsIgnoreCase(name);
        if (renamed && categoryRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new BusinessException("Você já tem uma categoria chamada '" + name + "'.");
        }
        if (category.getType() != request.type() && transactionRepository.existsByCategoryId(id)) {
            throw new BusinessException("Não é possível mudar o tipo de uma categoria que já tem transações.");
        }

        category.update(name, request.type());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Category category = findOwned(userId, id);
        if (transactionRepository.existsByCategoryId(id)) {
            throw new BusinessException("Esta categoria possui transações e não pode ser removida.");
        }
        categoryRepository.delete(category);
    }

    Category findOwned(Long userId, Long id) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria " + id + " não encontrada."));
    }
}
