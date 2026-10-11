package com.gabrielpacheco.financas.controller;

import com.gabrielpacheco.financas.dto.CategoryDtos.CategoryRequest;
import com.gabrielpacheco.financas.dto.CategoryDtos.CategoryResponse;
import com.gabrielpacheco.financas.security.AuthenticatedUser;
import com.gabrielpacheco.financas.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorias")
@SecurityRequirement(name = "bearer")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Lista as categorias do usuário")
    public List<CategoryResponse> list(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return categoryService.list(user.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma categoria de receita ou despesa")
    public CategoryResponse create(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                                   @Valid @RequestBody CategoryRequest request) {
        return categoryService.create(user.id(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma categoria")
    public CategoryResponse update(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable Long id,
                                   @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma categoria sem transações")
    public void delete(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                       @PathVariable Long id) {
        categoryService.delete(user.id(), id);
    }
}
