package com.finance.dashboard.service;

import com.finance.dashboard.dto.DashboardSummary;
import com.finance.dashboard.entity.*;
import com.finance.dashboard.enums.*;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock FinancialRecordRepository recordRepository;

    @InjectMocks DashboardService dashboardService;

    @Test
    @DisplayName("getSummary: computes totals and net balance correctly")
    void getSummary_computesTotalsCorrectly() {
        User user = TestDataFactory.makeUser("admin", Role.ADMIN);
        FinancialRecord record = TestDataFactory.makeRecord(user);

        when(recordRepository.sumByType(TransactionType.INCOME))
                .thenReturn(new BigDecimal("5000.00"));
        when(recordRepository.sumByType(TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("1200.00"));
        when(recordRepository.sumGroupedByCategory())
                .thenReturn(List.of(
                        new Object[]{Category.SALARY,
                                new BigDecimal("5000.00")},
                        new Object[]{Category.FOOD,
                                new BigDecimal("1200.00")}
                ));
        when(recordRepository.monthlyTrends(any()))
                .thenReturn(List.of(
                        new Object[]{2024, 3,
                                TransactionType.INCOME,
                                new BigDecimal("5000.00")},
                        new Object[]{2024, 3,
                                TransactionType.EXPENSE,
                                new BigDecimal("1200.00")}
                ));

        Page<FinancialRecord> page =
                new PageImpl<>(List.of(record));
        when(recordRepository.findAllByDeletedFalse(any()))
                .thenReturn(page);

        DashboardSummary summary =
                dashboardService.getSummary(6);

        assertThat(summary.totalIncome())
                .isEqualByComparingTo("5000.00");
        assertThat(summary.totalExpense())
                .isEqualByComparingTo("1200.00");
        assertThat(summary.netBalance())
                .isEqualByComparingTo("3800.00");
        assertThat(summary.categoryBreakdown()).hasSize(2);
        assertThat(summary.monthlyTrends()).hasSize(1);
        assertThat(summary.recentActivity()).hasSize(1);
    }

    @Test
    @DisplayName("getSummary: category breakdown sorted by total descending")
    void getSummary_categoryBreakdown_sortedDescending() {
        // User user = TestDataFactory.makeUser("admin", Role.ADMIN);

        when(recordRepository.sumByType(any()))
                .thenReturn(BigDecimal.ZERO);
        when(recordRepository.sumGroupedByCategory())
                .thenReturn(List.of(
                        new Object[]{Category.FOOD,
                                new BigDecimal("300.00")},
                        new Object[]{Category.SALARY,
                                new BigDecimal("5000.00")},
                        new Object[]{Category.RENT,
                                new BigDecimal("1000.00")}
                ));
        when(recordRepository.monthlyTrends(any()))
                .thenReturn(List.of());
        when(recordRepository.findAllByDeletedFalse(any()))
                .thenReturn(Page.empty());

        DashboardSummary summary =
                dashboardService.getSummary(6);

        assertThat(summary.categoryBreakdown())
                .extracting(c -> c.category())
                .containsExactly(
                        Category.SALARY,
                        Category.RENT,
                        Category.FOOD);
    }
}