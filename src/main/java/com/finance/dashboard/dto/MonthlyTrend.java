package com.finance.dashboard.dto;

import java.math.BigDecimal;

public record MonthlyTrend(
        int year,
        int month,
        BigDecimal income,
        BigDecimal expense
) {}