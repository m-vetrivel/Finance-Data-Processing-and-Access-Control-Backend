package com.finance.dashboard.controller;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.enums.*;
import com.finance.dashboard.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records")
@SecurityRequirement(name = "Bearer Auth")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @Operation(summary = "Create a new financial record — ANALYST, ADMIN only")
    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<FinancialRecordResponse> create(
            @Valid @RequestBody FinancialRecordRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(recordService.create(request));
    }

    @Operation(summary = "Get all records — paginated + filterable — ALL roles")
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<PageResponse<FinancialRecordResponse>> getAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.ok(
                recordService.getAll(type, category, from, to,
                        page, size, sortBy, direction));
    }

    @Operation(summary = "Search records by keyword in notes — ALL roles")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<PageResponse<FinancialRecordResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                recordService.search(keyword, page, size));
    }

    @Operation(summary = "Get record by ID — ALL roles")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<FinancialRecordResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(recordService.getById(id));
    }

    @Operation(summary = "Update a record — ANALYST, ADMIN only")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<FinancialRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request) {
        return ResponseEntity.ok(recordService.update(id, request));
    }

    @Operation(summary = "Soft delete a record — ADMIN only")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}