package com.pos.repository;

import com.pos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    // Tenant-aware methods
    @Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND p.sku = :sku")
    Optional<Product> findBySkuAndTenantId(@Param("sku") String sku, @Param("tenantId") String tenantId);
    
    @Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND p.active = true")
    List<Product> findByTenantIdAndActiveTrue(@Param("tenantId") String tenantId);
    
    @Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND p.category.id = :categoryId")
    List<Product> findByTenantIdAndCategoryId(@Param("tenantId") String tenantId, @Param("categoryId") String categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND p.active = true AND p.category.id = :categoryId")
    List<Product> findByTenantIdAndActiveTrueAndCategoryId(@Param("tenantId") String tenantId, @Param("categoryId") String categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByTenantIdAndNameContainingIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);
    
    @Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND p.quantity <= p.minQuantity AND p.active = true")
    List<Product> findLowStockProductsByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND p.active = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchProductsByTenantId(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.tenant.id = :tenantId AND p.sku = :sku")
    boolean existsBySkuAndTenantId(@Param("sku") String sku, @Param("tenantId") String tenantId);
    
    // Legacy methods for system-level operations
    Optional<Product> findBySku(String sku);
    List<Product> findByActiveTrue();
    List<Product> findByCategoryId(String categoryId);
    List<Product> findByActiveTrueAndCategoryId(String categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE p.quantity <= p.minQuantity AND p.active = true")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
    
    boolean existsBySku(String sku);
}
