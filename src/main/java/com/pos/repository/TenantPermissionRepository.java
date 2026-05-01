package com.pos.repository;

import com.pos.entity.TenantPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantPermissionRepository extends JpaRepository<TenantPermission, String> {
    List<TenantPermission> findByTenantId(String tenantId);
    List<TenantPermission> findByTenantIdAndEnabledTrue(String tenantId);
    
    Optional<TenantPermission> findByTenantIdAndPermissionName(String tenantId, String permissionName);
    Optional<TenantPermission> findByTenantIdAndPermissionNameAndEnabledTrue(String tenantId, String permissionName);
    
    @Query("SELECT tp.permissionName FROM TenantPermission tp WHERE tp.tenant.id = :tenantId AND tp.enabled = true")
    List<String> findEnabledPermissionNamesByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(tp) FROM TenantPermission tp WHERE tp.tenant.id = :tenantId AND tp.enabled = true")
    String countEnabledPermissionsByTenantId(@Param("tenantId") String tenantId);
    
    void deleteByTenantId(String tenantId);
}
