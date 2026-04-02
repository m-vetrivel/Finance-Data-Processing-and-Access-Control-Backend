package com.finance.dashboard.util;

import com.finance.dashboard.entity.*;
import com.finance.dashboard.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestDataFactory {

    public static User makeUser(String username, Role role) {
        return User.builder()
                .username(username)
                .email(username + "@test.com")
                .password("encoded-password")
                .role(role)
                .active(true)
                .build();
    }

    public static FinancialRecord makeRecord(User createdBy) {
        return FinancialRecord.builder()
                .amount(new BigDecimal("1000.00"))
                .type(TransactionType.INCOME)
                .category(Category.SALARY)
                .date(LocalDate.now())
                .notes("Test record")
                .createdBy(createdBy)
                .deleted(false)
                .build();
    }
}