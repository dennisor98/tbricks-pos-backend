package com.pos.interceptor;

import com.pos.context.TenantContext;
import com.pos.entity.Tenant;
import com.pos.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantRepository tenantRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // Skip subdomain checking for auth endpoints (login, register, request-code)
        if (requestURI.equals("/api/auth/login") || 
            requestURI.equals("/api/auth/register") || 
            requestURI.equals("/api/auth/request-code")) {
            return true;
        }
        
        // Check subdomain for authenticated requests
        String tenantSubdomain = extractTenantFromRequest(request);
        
        if (tenantSubdomain != null) {
            Tenant tenant = tenantRepository.findBySubdomain(tenantSubdomain)
                    .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantSubdomain));
            
            if (!tenant.getActive()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant is disabled");
                return false;
            }
            
            TenantContext.setCurrentTenant(tenant.getId());
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
    }

    private String extractTenantFromRequest(HttpServletRequest request) {
        String host = request.getHeader("Host");
        if (host == null) {
            host = request.getServerName();
        }
        
        // Remove port if present
        if (host != null && host.contains(":")) {
            host = host.split(":")[0];
        }
        
        if (host != null) {
            String[] parts = host.split("\\.");
            if (parts.length >= 2) {
                return parts[0];
            }
        }
        
        return null;
    }
}
