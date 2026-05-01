package com.pos.repository;

import com.pos.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, String> {
    List<SaleItem> findBySaleId(String saleId);
    List<SaleItem> findByProductId(String productId);
    
    @Query("SELECT si FROM SaleItem si JOIN si.sale s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<SaleItem> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT si.product.id, SUM(si.quantity) as totalSold FROM SaleItem si JOIN si.sale s WHERE s.createdAt BETWEEN :startDate AND :endDate AND s.paymentStatus = 'PAID' GROUP BY si.product.id ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProductsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
