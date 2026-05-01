package com.pos.repository;

import com.pos.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    
    // Tenant-aware methods
    @Query("SELECT c FROM Category c WHERE c.tenant.id = :tenantId AND c.name = :name")
    Optional<Category> findByNameAndTenantId(@Param("name") String name, @Param("tenantId") String tenantId);
    
    @Query("SELECT c FROM Category c WHERE c.tenant.id = :tenantId AND c.active = true")
    List<Category> findByTenantIdAndActiveTrue(@Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.tenant.id = :tenantId AND c.name = :name")
    boolean existsByNameAndTenantId(@Param("name") String name, @Param("tenantId") String tenantId);
    
    // Legacy methods for system-level operations
    Optional<Category> findByName(String name);
    List<Category> findByActiveTrue();
    boolean existsByName(String name);
}
