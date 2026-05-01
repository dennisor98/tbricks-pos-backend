package com.pos.service;

import com.pos.dto.TenantPermissionDto;
import com.pos.entity.TenantPermission;
import com.pos.repository.TenantPermissionRepository;
import com.pos.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantPermissionService {

    private final TenantPermissionRepository tenantPermissionRepository;
    private final TenantRepository tenantRepository;

    public List<TenantPermissionDto> getPermissionsByTenantId(String tenantId) {
        return tenantPermissionRepository.findByTenantId(tenantId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TenantPermissionDto> getEnabledPermissionsByTenantId(String tenantId) {
        return tenantPermissionRepository.findByTenantIdAndEnabledTrue(tenantId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<String> getEnabledPermissionNamesByTenantId(String tenantId) {
        return tenantPermissionRepository.findEnabledPermissionNamesByTenantId(tenantId);
    }

    public TenantPermissionDto getPermissionById(String id) {
        TenantPermission permission = tenantPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant permission not found"));
        return convertToDto(permission);
    }

    public TenantPermissionDto getPermissionByNameAndTenant(String tenantId, String permissionName) {
        TenantPermission permission = tenantPermissionRepository.findByTenantIdAndPermissionNameAndEnabledTrue(tenantId, permissionName)
                .orElseThrow(() -> new RuntimeException("Tenant permission not found"));
        return convertToDto(permission);
    }

    @Transactional
    public TenantPermissionDto createPermission(TenantPermissionDto permissionDto) {
        // Verify tenant exists
        tenantRepository.findById(permissionDto.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Check if permission already exists
        if (tenantPermissionRepository.findByTenantIdAndPermissionName(
                permissionDto.getTenantId(), permissionDto.getPermissionName()).isPresent()) {
            throw new RuntimeException("Permission already exists for this tenant");
        }

        TenantPermission permission = convertToEntity(permissionDto);
        TenantPermission savedPermission = tenantPermissionRepository.save(permission);
        return convertToDto(savedPermission);
    }

    @Transactional
    public TenantPermissionDto updatePermission(String id, TenantPermissionDto permissionDto) {
        TenantPermission existingPermission = tenantPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant permission not found"));

        // Check if permission name conflicts with another permission
        if (!existingPermission.getPermissionName().equals(permissionDto.getPermissionName()) &&
            tenantPermissionRepository.findByTenantIdAndPermissionName(
                    existingPermission.getTenant().getId(), permissionDto.getPermissionName()).isPresent()) {
            throw new RuntimeException("Permission already exists for this tenant");
        }

        updatePermissionFromDto(existingPermission, permissionDto);
        TenantPermission updatedPermission = tenantPermissionRepository.save(existingPermission);
        return convertToDto(updatedPermission);
    }

    @Transactional
    public void enablePermission(String id) {
        TenantPermission permission = tenantPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant permission not found"));
        permission.setEnabled(true);
        tenantPermissionRepository.save(permission);
    }

    @Transactional
    public void disablePermission(String id) {
        TenantPermission permission = tenantPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant permission not found"));
        permission.setEnabled(false);
        tenantPermissionRepository.save(permission);
    }

    @Transactional
    public void deletePermission(String id) {
        TenantPermission permission = tenantPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant permission not found"));
        tenantPermissionRepository.delete(permission);
    }

    @Transactional
    public void initializeDefaultPermissions(String tenantId) {
        String[] defaultPermissions = {
            "VIEW_PRODUCTS", "CREATE_PRODUCTS", "UPDATE_PRODUCTS", "DELETE_PRODUCTS",
            "VIEW_CATEGORIES", "CREATE_CATEGORIES", "UPDATE_CATEGORIES", "DELETE_CATEGORIES",
            "VIEW_CUSTOMERS", "CREATE_CUSTOMERS", "UPDATE_CUSTOMERS", "DELETE_CUSTOMERS",
            "VIEW_SALES", "CREATE_SALES", "REFUND_SALES",
            "VIEW_ANALYTICS", "VIEW_REPORTS",
            "MANAGE_USERS", "MANAGE_PERMISSIONS"
        };

        for (String permissionName : defaultPermissions) {
            if (!tenantPermissionRepository.findByTenantIdAndPermissionName(tenantId, permissionName).isPresent()) {
                TenantPermission permission = TenantPermission.builder()
                        .tenant(tenantRepository.findById(tenantId).orElseThrow(() -> new RuntimeException("Tenant not found")))
                        .permissionName(permissionName)
                        .description("Default permission: " + permissionName.replace("_", " ").toLowerCase())
                        .enabled(true)
                        .build();
                tenantPermissionRepository.save(permission);
            }
        }
    }

    public boolean hasPermission(String tenantId, String permissionName) {
        return tenantPermissionRepository.findByTenantIdAndPermissionNameAndEnabledTrue(tenantId, permissionName).isPresent();
    }

    private TenantPermissionDto convertToDto(TenantPermission permission) {
        return TenantPermissionDto.builder()
                .id(permission.getId())
                .tenantId(permission.getTenant().getId())
                .permissionName(permission.getPermissionName())
                .description(permission.getDescription())
                .enabled(permission.getEnabled())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    private TenantPermission convertToEntity(TenantPermissionDto dto) {
        return TenantPermission.builder()
                .tenant(tenantRepository.findById(dto.getTenantId())
                        .orElseThrow(() -> new RuntimeException("Tenant not found")))
                .permissionName(dto.getPermissionName())
                .description(dto.getDescription())
                .enabled(dto.getEnabled() != null ? dto.getEnabled() : true)
                .build();
    }

    private void updatePermissionFromDto(TenantPermission permission, TenantPermissionDto dto) {
        permission.setPermissionName(dto.getPermissionName());
        permission.setDescription(dto.getDescription());
        permission.setEnabled(dto.getEnabled());
    }
}
