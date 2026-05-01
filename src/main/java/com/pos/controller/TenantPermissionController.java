package com.pos.controller;

import com.pos.dto.TenantPermissionDto;
import com.pos.service.TenantPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/permissions")
@RequiredArgsConstructor
@Tag(name = "Tenant Permissions", description = "Tenant permission management APIs")
public class TenantPermissionController {

    private final TenantPermissionService tenantPermissionService;

    @GetMapping
    @Operation(summary = "Get all tenant permissions", description = "Retrieve all permissions for a tenant")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'VIEW_PERMISSIONS')")
    public ResponseEntity<List<TenantPermissionDto>> getPermissionsByTenantId(@PathVariable String tenantId) {
        List<TenantPermissionDto> permissions = tenantPermissionService.getPermissionsByTenantId(tenantId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/enabled")
    @Operation(summary = "Get enabled tenant permissions", description = "Retrieve all enabled permissions for a tenant")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'VIEW_PERMISSIONS')")
    public ResponseEntity<List<TenantPermissionDto>> getEnabledPermissionsByTenantId(@PathVariable String tenantId) {
        List<TenantPermissionDto> permissions = tenantPermissionService.getEnabledPermissionsByTenantId(tenantId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/names")
    @Operation(summary = "Get enabled permission names", description = "Retrieve all enabled permission names for a tenant")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'VIEW_PERMISSIONS')")
    public ResponseEntity<List<String>> getEnabledPermissionNamesByTenantId(@PathVariable String tenantId) {
        List<String> permissionNames = tenantPermissionService.getEnabledPermissionNamesByTenantId(tenantId);
        return ResponseEntity.ok(permissionNames);
    }

    @GetMapping("/{permissionId}")
    @Operation(summary = "Get permission by ID", description = "Retrieve a specific permission by ID")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'VIEW_PERMISSIONS')")
    public ResponseEntity<TenantPermissionDto> getPermissionById(@PathVariable String tenantId, @PathVariable String permissionId) {
        TenantPermissionDto permission = tenantPermissionService.getPermissionById(permissionId);
        return ResponseEntity.ok(permission);
    }

    @GetMapping("/name/{permissionName}")
    @Operation(summary = "Get permission by name", description = "Retrieve a specific permission by name")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'VIEW_PERMISSIONS')")
    public ResponseEntity<TenantPermissionDto> getPermissionByName(@PathVariable String tenantId, @PathVariable String permissionName) {
        TenantPermissionDto permission = tenantPermissionService.getPermissionByNameAndTenant(tenantId, permissionName);
        return ResponseEntity.ok(permission);
    }

    @PostMapping
    @Operation(summary = "Create tenant permission", description = "Create a new permission for a tenant")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'MANAGE_PERMISSIONS')")
    public ResponseEntity<TenantPermissionDto> createPermission(@PathVariable String tenantId, @Valid @RequestBody TenantPermissionDto permissionDto) {
        permissionDto.setTenantId(tenantId);
        TenantPermissionDto createdPermission = tenantPermissionService.createPermission(permissionDto);
        return new ResponseEntity<>(createdPermission, HttpStatus.CREATED);
    }

    @PutMapping("/{permissionId}")
    @Operation(summary = "Update tenant permission", description = "Update an existing permission")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'MANAGE_PERMISSIONS')")
    public ResponseEntity<TenantPermissionDto> updatePermission(@PathVariable String tenantId, @PathVariable String permissionId, @Valid @RequestBody TenantPermissionDto permissionDto) {
        TenantPermissionDto updatedPermission = tenantPermissionService.updatePermission(permissionId, permissionDto);
        return ResponseEntity.ok(updatedPermission);
    }

    @PutMapping("/{permissionId}/enable")
    @Operation(summary = "Enable tenant permission", description = "Enable a specific permission")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'MANAGE_PERMISSIONS')")
    public ResponseEntity<Void> enablePermission(@PathVariable String tenantId, @PathVariable String permissionId) {
        tenantPermissionService.enablePermission(permissionId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{permissionId}/disable")
    @Operation(summary = "Disable tenant permission", description = "Disable a specific permission")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'MANAGE_PERMISSIONS')")
    public ResponseEntity<Void> disablePermission(@PathVariable String tenantId, @PathVariable String permissionId) {
        tenantPermissionService.disablePermission(permissionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{permissionId}")
    @Operation(summary = "Delete tenant permission", description = "Delete a specific permission")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'MANAGE_PERMISSIONS')")
    public ResponseEntity<Void> deletePermission(@PathVariable String tenantId, @PathVariable String permissionId) {
        tenantPermissionService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize")
    @Operation(summary = "Initialize default permissions", description = "Initialize default permissions for a tenant")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> initializeDefaultPermissions(@PathVariable String tenantId) {
        tenantPermissionService.initializeDefaultPermissions(tenantId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{permissionName}")
    @Operation(summary = "Check permission", description = "Check if tenant has a specific permission")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or @tenantSecurityService.hasPermission(#tenantId, 'VIEW_PERMISSIONS')")
    public ResponseEntity<Boolean> checkPermission(@PathVariable String tenantId, @PathVariable String permissionName) {
        boolean hasPermission = tenantPermissionService.hasPermission(tenantId, permissionName);
        return ResponseEntity.ok(hasPermission);
    }
}
