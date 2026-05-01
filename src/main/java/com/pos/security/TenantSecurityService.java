package com.pos.security;

import com.pos.context.TenantContext;
import com.pos.service.TenantPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantSecurityService {

    private final TenantPermissionService tenantPermissionService;

    public boolean hasPermission(String tenantId, String permissionName) {
        // Check if the current tenant context matches the requested tenant
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null || !currentTenant.equals(tenantId)) {
            return false;
        }

        return tenantPermissionService.hasPermission(tenantId, permissionName);
    }

    public boolean hasPermission(String permissionName) {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return false;
        }

        return tenantPermissionService.hasPermission(currentTenant, permissionName);
    }

    public boolean canManageUsers(String tenantId) {
        return hasPermission(tenantId, "MANAGE_USERS");
    }

    public boolean canManagePermissions(String tenantId) {
        return hasPermission(tenantId, "MANAGE_PERMISSIONS");
    }

    public boolean canViewProducts(String tenantId) {
        return hasPermission(tenantId, "VIEW_PRODUCTS");
    }

    public boolean canCreateProducts(String tenantId) {
        return hasPermission(tenantId, "CREATE_PRODUCTS");
    }

    public boolean canUpdateProducts(String tenantId) {
        return hasPermission(tenantId, "UPDATE_PRODUCTS");
    }

    public boolean canDeleteProducts(String tenantId) {
        return hasPermission(tenantId, "DELETE_PRODUCTS");
    }

    public boolean canViewCategories(String tenantId) {
        return hasPermission(tenantId, "VIEW_CATEGORIES");
    }

    public boolean canCreateCategories(String tenantId) {
        return hasPermission(tenantId, "CREATE_CATEGORIES");
    }

    public boolean canUpdateCategories(String tenantId) {
        return hasPermission(tenantId, "UPDATE_CATEGORIES");
    }

    public boolean canDeleteCategories(String tenantId) {
        return hasPermission(tenantId, "DELETE_CATEGORIES");
    }

    public boolean canViewCustomers(String tenantId) {
        return hasPermission(tenantId, "VIEW_CUSTOMERS");
    }

    public boolean canCreateCustomers(String tenantId) {
        return hasPermission(tenantId, "CREATE_CUSTOMERS");
    }

    public boolean canUpdateCustomers(String tenantId) {
        return hasPermission(tenantId, "UPDATE_CUSTOMERS");
    }

    public boolean canDeleteCustomers(String tenantId) {
        return hasPermission(tenantId, "DELETE_CUSTOMERS");
    }

    public boolean canViewSales(String tenantId) {
        return hasPermission(tenantId, "VIEW_SALES");
    }

    public boolean canCreateSales(String tenantId) {
        return hasPermission(tenantId, "CREATE_SALES");
    }

    public boolean canRefundSales(String tenantId) {
        return hasPermission(tenantId, "REFUND_SALES");
    }

    public boolean canViewAnalytics(String tenantId) {
        return hasPermission(tenantId, "VIEW_ANALYTICS");
    }

    public boolean canViewReports(String tenantId) {
        return hasPermission(tenantId, "VIEW_REPORTS");
    }
}
