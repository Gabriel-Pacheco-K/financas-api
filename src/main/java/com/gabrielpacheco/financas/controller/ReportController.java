package com.gabrielpacheco.financas.controller;

import com.gabrielpacheco.financas.dto.MonthlySummary;
import com.gabrielpacheco.financas.security.AuthenticatedUser;
import com.gabrielpacheco.financas.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Relatórios")
@SecurityRequirement(name = "bearer")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    @Operation(summary = "Resumo do mês: receitas, despesas, saldo, taxa de poupança e totais por categoria")
    public MonthlySummary monthly(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                                  @Parameter(example = "2026-10")
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return reportService.monthlySummary(user.id(), month);
    }
}
