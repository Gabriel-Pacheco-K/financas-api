package com.gabrielpacheco.financas.controller;

import com.gabrielpacheco.financas.domain.TransactionType;
import com.gabrielpacheco.financas.dto.TransactionDtos.TransactionRequest;
import com.gabrielpacheco.financas.dto.TransactionDtos.TransactionResponse;
import com.gabrielpacheco.financas.security.AuthenticatedUser;
import com.gabrielpacheco.financas.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transações")
@SecurityRequirement(name = "bearer")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Lista transações com filtros opcionais e paginação")
    public Page<TransactionResponse> search(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @ParameterObject @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.search(user.id(), start, end, categoryId, type, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma transação pelo id")
    public TransactionResponse get(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable Long id) {
        return transactionService.get(user.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma receita ou despesa")
    public TransactionResponse create(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                                      @Valid @RequestBody TransactionRequest request) {
        return transactionService.create(user.id(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma transação")
    public TransactionResponse update(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable Long id,
                                      @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma transação")
    public void delete(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                       @PathVariable Long id) {
        transactionService.delete(user.id(), id);
    }
}
