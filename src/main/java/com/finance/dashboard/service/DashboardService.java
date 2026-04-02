package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.enums.Category;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public DashboardSummary getSummary(int trendMonths) {

        // ── Totals ────────────────────────────────────────
        BigDecimal totalIncome  =
                recordRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpense =
                recordRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance   = totalIncome.subtract(totalExpense);

        // ── Category breakdown ────────────────────────────
        List<CategorySummary> categoryBreakdown =
                recordRepository.sumGroupedByCategory()
                        .stream()
                        .map(row -> new CategorySummary(
                                (Category) row[0],
                                (BigDecimal) row[1]))
                        .sorted(Comparator
                                .comparing(CategorySummary::total)
                                .reversed())
                        .toList();

        // ── Monthly trends ────────────────────────────────
        LocalDate trendFrom = LocalDate.now()
                .minusMonths(trendMonths)
                .withDayOfMonth(1);

        List<MonthlyTrend> monthlyTrends =
                buildMonthlyTrends(trendFrom);

        // ── Weekly trends ────────────────────────────────
        LocalDate weeklyFrom = LocalDate.now().minusWeeks(8);
        List<WeeklyTrend> weeklyTrends = buildWeeklyTrends(weeklyFrom);

        // ── Recent activity (last 5 records) ─────────────
        List<FinancialRecordResponse> recentActivity =
                recordRepository.findAllByDeletedFalse(
                        PageRequest.of(0, 5,
                                Sort.by("date").descending()))
                        .map(FinancialRecordResponse::from)
                        .getContent();

        return new DashboardSummary(
                totalIncome,
                totalExpense,
                netBalance,
                categoryBreakdown,
                monthlyTrends,
                weeklyTrends,
                recentActivity
        );
    }

    // ── Helpers ───────────────────────────────────────────

    private List<MonthlyTrend> buildMonthlyTrends(LocalDate from) {

        // raw rows: [year, month, type, total]
        List<Object[]> rows = recordRepository.monthlyTrends(from);

        // group by year+month into a map
        Map<String, MonthlyTrendAccumulator> map = new LinkedHashMap<>();

        for (Object[] row : rows) {
            int year   = ((Number) row[0]).intValue();
            int month  = ((Number) row[1]).intValue();
            TransactionType type = (TransactionType) row[2];
            BigDecimal total = (BigDecimal) row[3];

            String key = year + "-" + month;
            map.computeIfAbsent(key,
                    k -> new MonthlyTrendAccumulator(year, month));

            MonthlyTrendAccumulator acc = map.get(key);
            if (type == TransactionType.INCOME)
                acc.income = acc.income.add(total);
            else
                acc.expense = acc.expense.add(total);
        }

        return map.values().stream()
                .map(acc -> new MonthlyTrend(
                        acc.year, acc.month,
                        acc.income, acc.expense))
                .toList();
    }

    // simple mutable accumulator — private to this service
    private static class MonthlyTrendAccumulator {
        int year, month;
        BigDecimal income  = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        MonthlyTrendAccumulator(int year, int month) {
            this.year  = year;
            this.month = month;
        }
    }
    private List<WeeklyTrend> buildWeeklyTrends(LocalDate from) {
    List<Object[]> rows = recordRepository.weeklyTrends(from);
    Map<String, WeeklyTrendAccumulator> map = new LinkedHashMap<>();

    for (Object[] row : rows) {
        int year  = ((Number) row[0]).intValue();
        int week  = ((Number) row[1]).intValue();
        TransactionType type  = (TransactionType) row[2];
        BigDecimal total = (BigDecimal) row[3];

        String key = year + "-W" + week;
        map.computeIfAbsent(key,
                k -> new WeeklyTrendAccumulator(year, week));

        WeeklyTrendAccumulator acc = map.get(key);
        if (type == TransactionType.INCOME)
            acc.income = acc.income.add(total);
        else
            acc.expense = acc.expense.add(total);
    }

    return map.values().stream()
            .map(acc -> new WeeklyTrend(
                    acc.year, acc.week,
                    acc.income, acc.expense))
            .toList();
}

        private static class WeeklyTrendAccumulator {
        int year, week;
        BigDecimal income  = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        WeeklyTrendAccumulator(int year, int week) {
                this.year = year;
                this.week = week;
        }
        }
}