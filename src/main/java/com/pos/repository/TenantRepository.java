package com.pos.repository;

import com.pos.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findBySubdomain(String subdomain);
    Optional<Tenant> findByContactEmailOrContactPhone(String email,String phone);
    Optional<Tenant> findByName(String name);
    List<Tenant> findByActiveTrue();
    List<Tenant> findByActiveFalse();
    
    @Query("SELECT t FROM Tenant t WHERE t.active = true AND (t.subscriptionExpires IS NULL OR t.subscriptionExpires > :currentDate)")
    List<Tenant> findActiveTenantsWithValidSubscription(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tenant t WHERE t.active = false OR (t.subscriptionExpires IS NOT NULL AND t.subscriptionExpires <= :currentDate)")
    List<Tenant> findDisabledOrExpiredTenants(@Param("currentDate") LocalDateTime currentDate);
    
    boolean existsBySubdomain(String subdomain);
    boolean existsByName(String name);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId")
    String countUsersByTenantId(@Param("tenantId") String tenantId);
}
