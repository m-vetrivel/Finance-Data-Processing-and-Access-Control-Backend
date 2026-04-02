package com.finance.dashboard.dto;

import java.math.BigDecimal;

public record WeeklyTrend(
        int year,
        int week,
        BigDecimal income,
        BigDecimal expense
) {}