package com.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private SalesSummaryDto todaySales;
    private SalesSummaryDto weeklySales;
    private SalesSummaryDto monthlySales;
    private Integer lowStockProducts;
    private Long totalCustomers;
    private List<ProductSalesDto> topSellingProducts;
    private List<SaleDto> recentSales;
}
