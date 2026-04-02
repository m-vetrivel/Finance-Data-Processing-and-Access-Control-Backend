package com.finance.dashboard.dto;

import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.enums.Category;
import com.finance.dashboard.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FinancialRecordResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        Category category,
        LocalDate date,
        String notes,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FinancialRecordResponse from(FinancialRecord r) {
        return new FinancialRecordResponse(
                r.getId(),
                r.getAmount(),
                r.getType(),
                r.getCategory(),
                r.getDate(),
                r.getNotes(),
                r.getCreatedBy().getUsername(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}