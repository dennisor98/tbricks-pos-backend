package com.pos.repository;

import com.pos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.username = :username")
    Optional<User> findByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") String tenantId);
    
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.email = :email")
    Optional<User> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") String tenantId);
    
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId")
    List<User> findByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId AND u.username = :username")
    boolean existsByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId AND u.email = :email")
    boolean existsByEmailAndTenantId(@Param("email") String email, @Param("tenantId") String tenantId);
    
    // Legacy methods for system-level operations
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
