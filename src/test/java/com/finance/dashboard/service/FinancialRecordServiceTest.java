package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.entity.*;
import com.finance.dashboard.enums.*;
import com.finance.dashboard.exception.AppException;
import com.finance.dashboard.repository.*;
import com.finance.dashboard.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock FinancialRecordRepository recordRepository;
    @Mock UserRepository            userRepository;

    @InjectMocks FinancialRecordService recordService;

    private User adminUser;

        @BeforeEach
        void setupSecurityContext() {
        adminUser = TestDataFactory.makeUser("admin", Role.ADMIN);

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);

        // lenient: only create() needs getCurrentUser()
        // other tests don't call it — intentionally unused there
        lenient().when(auth.getName()).thenReturn("admin");
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        lenient().when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(adminUser));

        SecurityContextHolder.setContext(ctx);
        }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── Create ────────────────────────────────────────────

    @Test
    @DisplayName("create: valid request saves and returns record")
    void create_validRequest_returnsResponse() {
        FinancialRecord saved =
                TestDataFactory.makeRecord(adminUser);

        when(recordRepository.save(any())).thenReturn(saved);

        FinancialRecordRequest req = new FinancialRecordRequest(
                new BigDecimal("1000.00"),
                TransactionType.INCOME,
                Category.SALARY,
                LocalDate.now(),
                "Test"
        );

        FinancialRecordResponse response = recordService.create(req);

        assertThat(response.amount())
                .isEqualByComparingTo("1000.00");
        assertThat(response.type())
                .isEqualTo(TransactionType.INCOME);
        verify(recordRepository).save(any());
    }

    // ── Read ──────────────────────────────────────────────

    @Test
    @DisplayName("getById: existing id returns record")
    void getById_existingId_returnsRecord() {
        FinancialRecord record =
                TestDataFactory.makeRecord(adminUser);

        when(recordRepository.findActiveById(1L))
                .thenReturn(Optional.of(record));

        FinancialRecordResponse response = recordService.getById(1L);

        assertThat(response.category()).isEqualTo(Category.SALARY);
    }

    @Test
    @DisplayName("getById: missing id throws NOT_FOUND")
    void getById_missingId_throwsNotFound() {
        when(recordRepository.findActiveById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getById(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex ->
                    assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── Update ────────────────────────────────────────────

    @Test
    @DisplayName("update: existing record is updated")
    void update_existingRecord_isUpdated() {
        FinancialRecord record =
                TestDataFactory.makeRecord(adminUser);

        when(recordRepository.findActiveById(1L))
                .thenReturn(Optional.of(record));
        when(recordRepository.save(any())).thenReturn(record);

        FinancialRecordRequest req = new FinancialRecordRequest(
                new BigDecimal("2000.00"),
                TransactionType.EXPENSE,
                Category.FOOD,
                LocalDate.now(),
                "Updated"
        );

        FinancialRecordResponse response =
                recordService.update(1L, req);

        assertThat(response.amount())
                .isEqualByComparingTo("2000.00");
        assertThat(response.type())
                .isEqualTo(TransactionType.EXPENSE);
    }

    // ── Delete ────────────────────────────────────────────

    @Test
    @DisplayName("delete: soft deletes the record")
    void delete_existingRecord_softDeletes() {
        FinancialRecord record =
                TestDataFactory.makeRecord(adminUser);

        when(recordRepository.findActiveById(1L))
                .thenReturn(Optional.of(record));
        when(recordRepository.save(any())).thenReturn(record);

        recordService.delete(1L);

        assertThat(record.isDeleted()).isTrue();
        verify(recordRepository).save(record);
    }

    @Test
    @DisplayName("delete: missing id throws NOT_FOUND")
    void delete_missingId_throwsNotFound() {
        when(recordRepository.findActiveById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.delete(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex ->
                    assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));
    }
}