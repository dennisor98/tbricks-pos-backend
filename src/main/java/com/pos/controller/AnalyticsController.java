package com.pos.controller;

import com.pos.dto.*;
import com.pos.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard data", description = "Retrieve comprehensive dashboard analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DashboardDto> getDashboardData() {
        DashboardDto dashboard = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/sales-report")
    @Operation(summary = "Get sales report", description = "Generate sales report for a date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesReportDto> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        SalesReportDto report = analyticsService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/top-selling-products")
    @Operation(summary = "Get top selling products", description = "Retrieve top selling products for a date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProductSalesDto>> getTopSellingProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<ProductSalesDto> products = analyticsService.getTopSellingProducts(startDate, endDate, limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/inventory-report")
    @Operation(summary = "Get inventory report", description = "Generate comprehensive inventory report")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryReportDto> getInventoryReport() {
        InventoryReportDto report = analyticsService.getInventoryReport();
        return ResponseEntity.ok(report);
    }
}
