package com.finance.dashboard.repository;

import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.enums.Category;
import com.finance.dashboard.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // exclude soft-deleted in all queries
    Page<FinancialRecord> findAllByDeletedFalse(Pageable pageable);

    // filtering — all params optional via a single JPQL query
    @Query("""
        SELECT r FROM FinancialRecord r
        WHERE r.deleted = false
          AND (:type     IS NULL OR r.type     = :type)
          AND (:category IS NULL OR r.category = :category)
          AND (:from     IS NULL OR r.date     >= :from)
          AND (:to       IS NULL OR r.date     <= :to)
        """)
    Page<FinancialRecord> findWithFilters(
            @Param("type")     TransactionType type,
            @Param("category") Category category,
            @Param("from")     LocalDate from,
            @Param("to")       LocalDate to,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM FinancialRecord r
    WHERE r.deleted = false
      AND (:keyword IS NULL
           OR LOWER(r.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<FinancialRecord> searchByKeyword(
        @Param("keyword") String keyword,
        Pageable pageable);

    // dashboard aggregates
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("""
        SELECT r.category, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.deleted = false
        GROUP BY r.category
        """)
    java.util.List<Object[]> sumGroupedByCategory();

    @Query("""
        SELECT FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date),
               r.type, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.deleted = false
          AND r.date >= :from
        GROUP BY FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type
        ORDER BY FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date)
        """)
    java.util.List<Object[]> monthlyTrends(@Param("from") LocalDate from);

    @Query("""
    SELECT FUNCTION('YEAR', r.date), FUNCTION('WEEK', r.date),
           r.type, COALESCE(SUM(r.amount), 0)
    FROM FinancialRecord r
    WHERE r.deleted = false
      AND r.date >= :from
    GROUP BY FUNCTION('YEAR', r.date), FUNCTION('WEEK', r.date), r.type
    ORDER BY FUNCTION('YEAR', r.date), FUNCTION('WEEK', r.date)
    """)
    java.util.List<Object[]> weeklyTrends(@Param("from") LocalDate from);

    // find by id only if not soft-deleted
    @Query("SELECT r FROM FinancialRecord r WHERE r.id = :id AND r.deleted = false")
    Optional<FinancialRecord> findActiveById(@Param("id") Long id);
}
