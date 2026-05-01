package com.pos.repository;

import com.pos.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    
    // Tenant-aware methods
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId AND c.email = :email")
    Optional<Customer> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") String tenantId);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId AND c.phoneNumber = :phoneNumber")
    Optional<Customer> findByPhoneNumberAndTenantId(@Param("phoneNumber") String phoneNumber, @Param("tenantId") String tenantId);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId AND c.active = true")
    List<Customer> findByTenantIdAndActiveTrue(@Param("tenantId") String tenantId);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByTenantIdAndNameContainingIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);
    
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId AND c.active = true AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR c.phoneNumber LIKE CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchCustomersByTenantId(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tenant.id = :tenantId AND c.email = :email")
    boolean existsByEmailAndTenantId(@Param("email") String email, @Param("tenantId") String tenantId);
    
    // Legacy methods for system-level operations
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    List<Customer> findByActiveTrue();
    List<Customer> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Customer c WHERE c.active = true AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR c.phoneNumber LIKE CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchCustomers(@Param("searchTerm") String searchTerm);
    
    boolean existsByEmail(String email);
}
