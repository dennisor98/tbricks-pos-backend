package com.pos.security;

import com.pos.context.TenantContext;
import com.pos.entity.User;
import com.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String currentTenant = TenantContext.getCurrentTenant();
        
        log.info("Loading user: {}, tenant context: {}", username, currentTenant);
        
        User user;
        if (currentTenant != null) {
            // Multi-tenant authentication
            user = userRepository.findByUsernameAndTenantId(username, currentTenant)
                    .orElse(null);
            
            // If user not found in current tenant, try loading without tenant for super admin
            if (user == null) {
                log.info("User not found in tenant {}, trying system-wide lookup", currentTenant);
                user = userRepository.findById(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            }
        } else {
            // Fallback to system-level authentication (for system admin)
            user = userRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }
        
        log.info("User loaded: {}, role: {}, tenantId: {}", user.getUsername(), user.getRole(), user.getTenant().getId());
        return user;
    }
}
