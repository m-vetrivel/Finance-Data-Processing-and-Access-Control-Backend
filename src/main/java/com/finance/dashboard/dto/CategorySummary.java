package com.finance.dashboard.dto;

import com.finance.dashboard.enums.Category;
import java.math.BigDecimal;

public record CategorySummary(
        Category category,
        BigDecimal total
) {}