package com.pos.service;

import com.pos.dto.*;
import com.pos.entity.Product;
import com.pos.entity.Sale;
import com.pos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public DashboardDto getDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusWeeks(1);
        LocalDateTime startOfMonth = now.minusMonths(1);

        return DashboardDto.builder()
                .todaySales(getSalesSummary(startOfDay, now))
                .weeklySales(getSalesSummary(startOfWeek, now))
                .monthlySales(getSalesSummary(startOfMonth, now))
                .lowStockProducts(getLowStockCount())
                .totalCustomers(getTotalCustomers())
                .topSellingProducts(getTopSellingProducts(startOfMonth, now, 5))
                .recentSales(getRecentSales(5))
                .build();
    }

    public SalesReportDto getSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> salesData = saleRepository.findByDateRange(startDate, endDate);
        
        BigDecimal totalRevenue = saleRepository.totalRevenueByDateRange(startDate, endDate) != null ? 
                BigDecimal.valueOf(saleRepository.totalRevenueByDateRange(startDate, endDate)) : BigDecimal.ZERO;
        Long totalSales = saleRepository.countSalesByDateRange(startDate, endDate) != null ? 
                saleRepository.countSalesByDateRange(startDate, endDate) : 0L;
        
        Map<String, BigDecimal> salesByPaymentMethod = new HashMap<>();
        Map<String, Long> salesByStatus = new HashMap<>();
        
        for (Sale sale : salesData) {
            // This would need to be implemented based on the actual query results
        }

        return SalesReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalSales(totalSales)
                .averageOrderValue(totalSales > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO)
                .salesByPaymentMethod(salesByPaymentMethod)
                .salesByStatus(salesByStatus)
                .build();
    }

    public List<ProductSalesDto> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, Integer limit) {
        List<Object[]> results = saleItemRepository.findTopSellingProductsByDateRange(startDate, endDate);
        
        return results.stream()
                .limit(limit)
                .map(result -> {
                    String productId = result[0].toString();
                    Long totalSold = (Long) result[1];
                    
                    return productRepository.findById(productId)
                            .map(product -> ProductSalesDto.builder()
                                    .productId(productId)
                                    .productName(product.getName())
                                    .productSku(product.getSku())
                                    .totalSold(totalSold.intValue())
                                    .build())
                            .orElse(null);
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public InventoryReportDto getInventoryReport() {
        List<Product> allProducts = productRepository.findByActiveTrue();
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        
        Map<String, Integer> categoryCounts = new HashMap<>();
        BigDecimal totalInventoryValue = BigDecimal.ZERO;
        
        for (Product product : allProducts) {
            String categoryName = product.getCategory().getName();
            categoryCounts.put(categoryName, categoryCounts.getOrDefault(categoryName, 0) + 1);
            
            if (product.getCostPrice() != null) {
                totalInventoryValue = totalInventoryValue.add(
                        product.getCostPrice().multiply(BigDecimal.valueOf(product.getQuantity())));
            }
        }

        return InventoryReportDto.builder()
                .totalProducts(allProducts.size())
                .lowStockProducts(lowStockProducts.size())
                .totalInventoryValue(totalInventoryValue)
                .categoryDistribution(categoryCounts)
                .lowStockProductDetails(lowStockProducts.stream()
                        .map(product -> ProductSalesDto.builder()
                                .productId(product.getId())
                                .productName(product.getName())
                                .productSku(product.getSku())
                                .totalSold(product.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private SalesSummaryDto getSalesSummary(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalRevenue = saleRepository.totalRevenueByDateRange(startDate, endDate) != null ? 
                BigDecimal.valueOf(saleRepository.totalRevenueByDateRange(startDate, endDate)) : BigDecimal.ZERO;
        Long totalSales = saleRepository.countSalesByDateRange(startDate, endDate) != null ? 
                saleRepository.countSalesByDateRange(startDate, endDate) : 0L;
        
        return SalesSummaryDto.builder()
                .totalRevenue(totalRevenue)
                .totalSales(totalSales)
                .averageOrderValue(totalSales > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO)
                .build();
    }

    private Integer getLowStockCount() {
        return productRepository.findLowStockProducts().size();
    }

    private Long getTotalCustomers() {
        return customerRepository.findByActiveTrue().stream().count();
    }

//     private List<ProductSalesDto> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, Integer limit) {
//         return getTopSellingProducts(startDate, endDate, limit);
//     }

    private List<SaleDto> getRecentSales(Integer limit) {
        return saleRepository.findByDateRange(LocalDateTime.now().minusDays(7), LocalDateTime.now())
                .stream()
                .limit(limit)
                .map(this::convertToSaleDto)
                .collect(Collectors.toList());
    }

    private SaleDto convertToSaleDto(com.pos.entity.Sale sale) {
        return SaleDto.builder()
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .paymentMethod(sale.getPaymentMethod())
                .paymentStatus(sale.getPaymentStatus())
                .notes(sale.getNotes())
                .build();
    }
}
