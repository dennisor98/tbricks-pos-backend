package com.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDto {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalRevenue;
    private Long totalSales;
    private BigDecimal averageOrderValue;
    private Map<String, BigDecimal> salesByPaymentMethod;
    private Map<String, Long> salesByStatus;
}
