package com.finance.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummary(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        List<CategorySummary> categoryBreakdown,
        List<MonthlyTrend> monthlyTrends,
        List<WeeklyTrend> weeklyTrends, 
        List<FinancialRecordResponse> recentActivity
) {}