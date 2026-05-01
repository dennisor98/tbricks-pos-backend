package com.pos.controller;

import com.pos.dto.SaleDto;
import com.pos.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Sales management APIs")
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    @Operation(summary = "Get all sales", description = "Retrieve all sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<SaleDto>> getAllSales() {
        List<SaleDto> sales = saleService.getAllSales();
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID", description = "Retrieve a specific sale by its ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<SaleDto> getSaleById(@PathVariable String id) {
        SaleDto sale = saleService.getSaleById(id);
        return ResponseEntity.ok(sale);
    }

    @GetMapping("/invoice/{invoiceNumber}")
    @Operation(summary = "Get sale by invoice number", description = "Retrieve a specific sale by its invoice number")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<SaleDto> getSaleByInvoiceNumber(@PathVariable String invoiceNumber) {
        SaleDto sale = saleService.getSaleByInvoiceNumber(invoiceNumber);
        return ResponseEntity.ok(sale);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get sales by date range", description = "Retrieve sales within a specific date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<SaleDto>> getSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SaleDto> sales = saleService.getSalesByDateRange(startDate, endDate);
        return ResponseEntity.ok(sales);
    }

    @PostMapping
    @Operation(summary = "Create sale", description = "Create a new sale")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<SaleDto> createSale(@Valid @RequestBody SaleDto saleDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        SaleDto createdSale = saleService.createSale(saleDto, username);
        return new ResponseEntity<>(createdSale, HttpStatus.CREATED);
    }

    @PostMapping("/{saleId}/refund")
    @Operation(summary = "Refund sale items", description = "Refund specific items from a sale")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> refundSale(@PathVariable String saleId, @RequestBody List<String> itemIds) {
        saleService.refundSale(saleId, itemIds);
        return ResponseEntity.ok().build();
    }
}
