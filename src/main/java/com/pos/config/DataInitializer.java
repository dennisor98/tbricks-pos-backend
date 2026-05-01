package com.pos.config;

import com.pos.entity.Tenant;
import com.pos.entity.User;
import com.pos.repository.TenantRepository;
import com.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SYSTEM_TENANT_NAME = "SYSTEM";
    private static final String SYSTEM_TENANT_SUBDOMAIN = "system";
    private static final String SUPER_ADMIN_USERNAME = "superadmin";
    private static final String SUPER_ADMIN_EMAIL = "tbricks.official@gmail.com";
    private static final String SUPER_ADMIN_PASSWORD = "SuperAdmin123!"; // Should be changed in production

    @Override
    public void run(String... args) {
        initializeSystemTenant();
        initializeSuperAdmin();
    }

    private void initializeSystemTenant() {
        if (tenantRepository.findBySubdomain(SYSTEM_TENANT_SUBDOMAIN).isEmpty()) {
            Tenant systemTenant = Tenant.builder()
                    .name(SYSTEM_TENANT_NAME)
                    .subdomain(SYSTEM_TENANT_SUBDOMAIN)
                    .description("System tenant for super admin")
                    .companyName("POS System")
                    .contactEmail(SUPER_ADMIN_EMAIL)
                    .active(true)
                    .subscriptionPlan(Tenant.SubscriptionPlan.ENTERPRISE)
                    .maxUsers(1)
                    .currentUserCount(0)
                    .build();

            tenantRepository.save(systemTenant);
            log.info("System tenant created: {}", SYSTEM_TENANT_NAME);
        } else {
            log.info("System tenant already exists: {}", SYSTEM_TENANT_NAME);
        }
    }

    private void initializeSuperAdmin() {
        Tenant systemTenant = tenantRepository.findBySubdomain(SYSTEM_TENANT_SUBDOMAIN)
                .orElseThrow(() -> new RuntimeException("System tenant not found"));

        if (userRepository.findByUsernameAndTenantId(SUPER_ADMIN_USERNAME, systemTenant.getId()).isEmpty()) {
            User superAdmin = User.builder()
                    .username(SUPER_ADMIN_USERNAME)
                    .password(passwordEncoder.encode(SUPER_ADMIN_PASSWORD))
                    .email(SUPER_ADMIN_EMAIL)
                    .firstName("Super")
                    .lastName("Admin")
                    .role(User.Role.SUPER_ADMIN)
                    .tenant(systemTenant)
                    .enabled(true)
                    .build();

            userRepository.save(superAdmin);

            // Update tenant user count
            systemTenant.setCurrentUserCount(systemTenant.getCurrentUserCount() + 1);
            tenantRepository.save(systemTenant);

            log.info("Super admin user created: {}", SUPER_ADMIN_USERNAME);
            log.warn("Default super admin password is: {}. Please change it in production!", SUPER_ADMIN_PASSWORD);
        } else {
            log.info("Super admin user already exists: {}", SUPER_ADMIN_USERNAME);
        }
    }
}
