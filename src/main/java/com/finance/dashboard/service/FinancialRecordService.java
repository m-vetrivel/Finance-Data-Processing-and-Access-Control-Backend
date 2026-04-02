package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.entity.*;
import com.finance.dashboard.enums.*;
import com.finance.dashboard.exception.AppException;
import com.finance.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository            userRepository;

    // ── Create ────────────────────────────────────────────
    public FinancialRecordResponse create(FinancialRecordRequest request) {
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.amount())
                .type(request.type())
                .category(request.category())
                .date(request.date())
                .notes(request.notes())
                .createdBy(currentUser)
                .build();

        return FinancialRecordResponse.from(recordRepository.save(record));
    }

    // ── Read all (paginated + filtered) ──────────────────
    public PageResponse<FinancialRecordResponse> getAll(
            TransactionType type,
            Category category,
            LocalDate from,
            LocalDate to,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // use filtered query if any filter is provided
        boolean hasFilter = type != null || category != null
                || from != null || to != null;

        Page<FinancialRecordResponse> result = hasFilter
                ? recordRepository
                    .findWithFilters(type, category, from, to, pageable)
                    .map(FinancialRecordResponse::from)
                : recordRepository
                    .findAllByDeletedFalse(pageable)
                    .map(FinancialRecordResponse::from);

        return PageResponse.from(result);
    }

    // ── Read one ──────────────────────────────────────────
    public FinancialRecordResponse getById(Long id) {
        return recordRepository.findActiveById(id)
                .map(FinancialRecordResponse::from)
                .orElseThrow(() -> new AppException(
                        "Record not found with id: " + id,
                        HttpStatus.NOT_FOUND));
    }

    // ── Update ────────────────────────────────────────────
    public FinancialRecordResponse update(Long id,
                                         FinancialRecordRequest request) {
        FinancialRecord record = recordRepository.findActiveById(id)
                .orElseThrow(() -> new AppException(
                        "Record not found with id: " + id,
                        HttpStatus.NOT_FOUND));

        record.setAmount(request.amount());
        record.setType(request.type());
        record.setCategory(request.category());
        record.setDate(request.date());
        record.setNotes(request.notes());

        return FinancialRecordResponse.from(recordRepository.save(record));
    }

    // ── Soft Delete ───────────────────────────────────────
    public void delete(Long id) {
        FinancialRecord record = recordRepository.findActiveById(id)
                .orElseThrow(() -> new AppException(
                        "Record not found with id: " + id,
                        HttpStatus.NOT_FOUND));

        record.setDeleted(true);
        recordRepository.save(record);
    }

    // ── Helper ────────────────────────────────────────────
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(
                        "Authenticated user not found",
                        HttpStatus.UNAUTHORIZED));
    }

    // ── Search by keyword ─────────────────────────────
        public PageResponse<FinancialRecordResponse> search(
                String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("date").descending());

        return PageResponse.from(
                recordRepository.searchByKeyword(keyword, pageable)
                        .map(FinancialRecordResponse::from));
        }
}