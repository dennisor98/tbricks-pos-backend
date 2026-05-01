package com.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryDto {
    private BigDecimal totalRevenue;
    private Long totalSales;
    private BigDecimal averageOrderValue;
}
