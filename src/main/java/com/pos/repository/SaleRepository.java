package com.pos.repository;

import com.pos.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, String> {
    
    // Tenant-aware methods
    @Query("SELECT s FROM Sale s WHERE s.tenant.id = :tenantId AND s.invoiceNumber = :invoiceNumber")
    Optional<Sale> findByInvoiceNumberAndTenantId(@Param("invoiceNumber") String invoiceNumber, @Param("tenantId") String tenantId);
    
    @Query("SELECT s FROM Sale s WHERE s.tenant.id = :tenantId AND s.user.id = :userId")
    List<Sale> findByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") String userId);
    
    @Query("SELECT s FROM Sale s WHERE s.tenant.id = :tenantId AND s.customer.id = :customerId")
    List<Sale> findByTenantIdAndCustomerId(@Param("tenantId") String tenantId, @Param("customerId") String customerId);
    
    @Query("SELECT s FROM Sale s WHERE s.tenant.id = :tenantId AND s.paymentMethod = :paymentMethod")
    List<Sale> findByTenantIdAndPaymentMethod(@Param("tenantId") String tenantId, @Param("paymentMethod") Sale.PaymentMethod paymentMethod);
    
    @Query("SELECT s FROM Sale s WHERE s.tenant.id = :tenantId AND s.paymentStatus = :paymentStatus")
    List<Sale> findByTenantIdAndPaymentStatus(@Param("tenantId") String tenantId, @Param("paymentStatus") Sale.PaymentStatus paymentStatus);
    
    @Query("SELECT s FROM Sale s WHERE s.tenant.id = :tenantId AND s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<Sale> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Sale s WHERE s.tenant.id = :tenantId AND s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<Sale> findByTenantIdAndDateAfter(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.tenant.id = :tenantId AND s.createdAt BETWEEN :startDate AND :endDate")
    String countSalesByTenantIdAndDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.tenant.id = :tenantId AND s.createdAt BETWEEN :startDate AND :endDate AND s.paymentStatus = 'PAID'")
    Double totalRevenueByTenantIdAndDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.tenant.id = :tenantId AND s.invoiceNumber = :invoiceNumber")
    boolean existsByInvoiceNumberAndTenantId(@Param("invoiceNumber") String invoiceNumber, @Param("tenantId") String tenantId);
    
    // Legacy methods for system-level operations
    Optional<Sale> findByInvoiceNumber(String invoiceNumber);
    List<Sale> findByUserId(String userId);
    List<Sale> findByCustomerId(String customerId);
    List<Sale> findByPaymentMethod(Sale.PaymentMethod paymentMethod);
    List<Sale> findByPaymentStatus(Sale.PaymentStatus paymentStatus);
    
    @Query("SELECT s FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<Sale> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Sale s WHERE s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<Sale> findByDateAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    Long countSalesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate AND s.paymentStatus = 'PAID'")
    Double totalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
}
