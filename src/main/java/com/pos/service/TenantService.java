package com.pos.service;

import com.pos.dto.TenantDto;
import com.pos.entity.Tenant;
import com.pos.entity.User;
import com.pos.repository.TenantRepository;
import com.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<TenantDto> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TenantDto> getActiveTenants() {
        return tenantRepository.findByActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TenantDto> getDisabledTenants() {
        return tenantRepository.findByActiveFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TenantDto getTenantById(String id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return convertToDto(tenant);
    }

    public TenantDto getTenantBySubdomain(String subdomain) {
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return convertToDto(tenant);
    }

    @Transactional
    public TenantDto createTenant(TenantDto tenantDto) {
        if (tenantRepository.existsBySubdomain(tenantDto.getSubdomain())) {
            throw new RuntimeException("Subdomain already exists");
        }
        
        if (tenantRepository.existsByName(tenantDto.getName())) {
            throw new RuntimeException("Tenant name already exists");
        }

        Tenant tenant = convertToEntity(tenantDto);
        Tenant savedTenant = tenantRepository.save(tenant);
        
        // Create default admin user for the new tenant
        createDefaultAdminUser(savedTenant);
        
        return convertToDto(savedTenant);
    }
    
    private void createDefaultAdminUser(Tenant tenant) {
        String defaultUsername = "admin";
        String defaultPassword = "admin123";
        String defaultEmail = tenant.getContactEmail();
        
        User adminUser = User.builder()
                .username(defaultUsername)
                .password(passwordEncoder.encode(defaultPassword))
                .email(defaultEmail)
                .firstName("Tenant")
                .lastName("Super Admin")
                .role(User.Role.ADMIN)
                .tenant(tenant)
                .enabled(true)
                .build();
        
        userRepository.save(adminUser);
        
        // Update tenant user count
        tenant.setCurrentUserCount(1);
        tenantRepository.save(tenant);
        
        log.info("Created tenant super admin user for tenant: {} (subdomain: {}). Email: {}, Username: {}, Password: {} (CHANGE ON FIRST LOGIN)", 
                tenant.getName(), tenant.getSubdomain(), defaultEmail, defaultUsername, defaultPassword);
    }

    @Transactional
    public TenantDto updateTenant(String id, TenantDto tenantDto) {
        Tenant existingTenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (!existingTenant.getSubdomain().equals(tenantDto.getSubdomain()) && 
            tenantRepository.existsBySubdomain(tenantDto.getSubdomain())) {
            throw new RuntimeException("Subdomain already exists");
        }

        if (!existingTenant.getName().equals(tenantDto.getName()) && 
            tenantRepository.existsByName(tenantDto.getName())) {
            throw new RuntimeException("Tenant name already exists");
        }

        updateTenantFromDto(existingTenant, tenantDto);
        Tenant updatedTenant = tenantRepository.save(existingTenant);
        return convertToDto(updatedTenant);
    }

    @Transactional
    public void disableTenant(String id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setActive(false);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void enableTenant(String id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        // Check if subscription is valid
        if (tenant.getSubscriptionExpires() != null && tenant.getSubscriptionExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot enable tenant with expired subscription");
        }
        
        tenant.setActive(true);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void updateSubscription(String id, Tenant.SubscriptionPlan plan, LocalDateTime expires) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        tenant.setSubscriptionPlan(plan);
        tenant.setSubscriptionExpires(expires);
        
        // Auto-disable if subscription is expired
        if (expires != null && expires.isBefore(LocalDateTime.now())) {
            tenant.setActive(false);
        } else if (expires == null || expires.isAfter(LocalDateTime.now())) {
            tenant.setActive(true);
        }
        
        tenantRepository.save(tenant);
    }

    @Transactional
    public void incrementUserCount(String id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        if (tenant.getMaxUsers() != null && tenant.getCurrentUserCount() >= tenant.getMaxUsers()) {
            throw new RuntimeException("Maximum user limit reached for this tenant");
        }
        
        tenant.setCurrentUserCount(tenant.getCurrentUserCount() + 1);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void decrementUserCount(String id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        if (tenant.getCurrentUserCount() > 0) {
            tenant.setCurrentUserCount(tenant.getCurrentUserCount() - 1);
            tenantRepository.save(tenant);
        }
    }

    public boolean isTenantActive(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain)
                .map(Tenant::getActive)
                .orElse(false);
    }

    private TenantDto convertToDto(Tenant tenant) {
        return TenantDto.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .description(tenant.getDescription())
                .companyName(tenant.getCompanyName())
                .contactEmail(tenant.getContactEmail())
                .contactPhone(tenant.getContactPhone())
                .maxUsers(tenant.getMaxUsers())
                .currentUserCount(tenant.getCurrentUserCount())
                .active(tenant.getActive())
                .subscriptionPlan(tenant.getSubscriptionPlan() != null ? tenant.getSubscriptionPlan().name() : null)
                .subscriptionExpires(tenant.getSubscriptionExpires())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }

    private Tenant convertToEntity(TenantDto dto) {
        return Tenant.builder()
                .name(dto.getName())
                .subdomain(dto.getSubdomain())
                .description(dto.getDescription())
                .companyName(dto.getCompanyName())
                .contactEmail(dto.getContactEmail())
                .contactPhone(dto.getContactPhone())
                .maxUsers(dto.getMaxUsers())
                .currentUserCount(dto.getCurrentUserCount() != null ? dto.getCurrentUserCount() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .subscriptionPlan(dto.getSubscriptionPlan() != null ? 
                        Tenant.SubscriptionPlan.valueOf(dto.getSubscriptionPlan()) : Tenant.SubscriptionPlan.BASIC)
                .subscriptionExpires(dto.getSubscriptionExpires())
                .build();
    }

    private void updateTenantFromDto(Tenant tenant, TenantDto dto) {
        tenant.setName(dto.getName());
        tenant.setSubdomain(dto.getSubdomain());
        tenant.setDescription(dto.getDescription());
        tenant.setCompanyName(dto.getCompanyName());
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setContactPhone(dto.getContactPhone());
        tenant.setMaxUsers(dto.getMaxUsers());
        tenant.setActive(dto.getActive());
        
        if (dto.getSubscriptionPlan() != null) {
            tenant.setSubscriptionPlan(Tenant.SubscriptionPlan.valueOf(dto.getSubscriptionPlan()));
        }
        tenant.setSubscriptionExpires(dto.getSubscriptionExpires());
    }
}
