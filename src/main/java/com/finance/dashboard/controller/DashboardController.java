package com.finance.dashboard.controller;

import com.finance.dashboard.dto.DashboardSummary;
import com.finance.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
@SecurityRequirement(name = "Bearer Auth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Full dashboard summary — ALL roles",
               description = """
                       Returns:
                       - Total income, expense, net balance
                       - Category-wise totals (sorted highest first)
                       - Monthly income vs expense trends
                       - Last 5 transactions (recent activity)
                       
                       Use `trendMonths` to control how many months
                       of trend data to include (default: 6).
                       """)
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<DashboardSummary> getSummary(
            @RequestParam(defaultValue = "6") int trendMonths) {
        return ResponseEntity.ok(
                dashboardService.getSummary(trendMonths));
    }
}