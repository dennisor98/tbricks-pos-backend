package com.pos.controller;

import com.pos.dto.TenantDto;
import com.pos.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "System-level tenant management APIs (System Admin only)")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @Operation(
        summary = "Get all tenants",
        description = "Retrieve all tenants in the system. This includes both active and disabled tenants. Requires SYSTEM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tenants retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class),
                examples = @ExampleObject(
                    name = "All Tenants",
                    value = "[{\"id\":1,\"name\":\"Demo Store\",\"subdomain\":\"demo\",\"description\":\"Demo retail store for testing\",\"companyName\":\"Demo Retail Inc.\",\"contactEmail\":\"admin@demo.com\",\"contactPhone\":\"5551234567\",\"maxUsers\":50,\"currentUserCount\":3,\"active\":true,\"subscriptionPlan\":\"PROFESSIONAL\",\"subscriptionExpires\":\"2025-01-01T00:00:00\",\"createdAt\":\"2024-01-01T00:00:00\"}]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TenantDto>> getAllTenants() {
        List<TenantDto> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/active")
    @Operation(
        summary = "Get active tenants",
        description = "Retrieve all active tenants in the system. Only tenants that are currently enabled and have valid subscriptions are returned."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active tenants retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TenantDto>> getActiveTenants() {
        List<TenantDto> tenants = tenantService.getActiveTenants();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/disabled")
    @Operation(
        summary = "Get disabled tenants",
        description = "Retrieve all disabled tenants in the system. Includes tenants that were manually disabled or have expired subscriptions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Disabled tenants retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TenantDto>> getDisabledTenants() {
        List<TenantDto> tenants = tenantService.getDisabledTenants();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get tenant by ID",
        description = "Retrieve a specific tenant by its ID. Returns complete tenant information including subscription details and user counts."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tenant retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantDto> getTenantById(
            @Parameter(description = "Tenant ID", required = true, example = "1")
            @PathVariable String id) {
        TenantDto tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(tenant);
    }

    @GetMapping("/subdomain/{subdomain}")
    @Operation(
        summary = "Get tenant by subdomain",
        description = "Retrieve a specific tenant by its subdomain. This endpoint is typically used by the tenant interceptor to validate requests."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tenant retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        )
    })
    public ResponseEntity<TenantDto> getTenantBySubdomain(
            @Parameter(description = "Tenant subdomain", required = true, example = "demo")
            @PathVariable String subdomain) {
        TenantDto tenant = tenantService.getTenantBySubdomain(subdomain);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping
    @Operation(
        summary = "Create tenant",
        description = "Create a new tenant with subscription plan and user limits. The tenant will be automatically enabled if subscription is valid.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Tenant details for creation",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class),
                examples = @ExampleObject(
                    name = "Create Tenant",
                    value = "{\"name\":\"New Store\",\"subdomain\":\"newstore\",\"description\":\"New retail store\",\"companyName\":\"New Store Inc.\",\"contactEmail\":\"admin@newstore.com\",\"contactPhone\":\"5559876543\",\"maxUsers\":25,\"subscriptionPlan\":\"BASIC\",\"subscriptionExpires\":\"2024-12-31T23:59:59\"}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tenant created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed or subdomain/name already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantDto> createTenant(
            @Parameter(description = "Tenant details", required = true)
            @Valid @RequestBody TenantDto tenantDto) {
        TenantDto createdTenant = tenantService.createTenant(tenantDto);
        return new ResponseEntity<>(createdTenant, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update tenant",
        description = "Update an existing tenant's information. Cannot change subdomain to an existing one.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated tenant details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class),
                examples = @ExampleObject(
                    name = "Update Tenant",
                    value = "{\"name\":\"Updated Store\",\"description\":\"Updated retail store\",\"companyName\":\"Updated Store Inc.\",\"contactEmail\":\"updated@store.com\",\"contactPhone\":\"5551234567\",\"maxUsers\":30}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tenant updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TenantDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed or subdomain/name already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantDto> updateTenant(
            @Parameter(description = "Tenant ID", required = true, example = "1")
            @PathVariable String id,
            @Parameter(description = "Updated tenant details", required = true)
            @Valid @RequestBody TenantDto tenantDto) {
        TenantDto updatedTenant = tenantService.updateTenant(id, tenantDto);
        return ResponseEntity.ok(updatedTenant);
    }

    @PutMapping("/{id}/disable")
    @Operation(
        summary = "Disable tenant",
        description = "Disable a tenant. Once disabled, all API requests for this tenant will be rejected with 403 Forbidden."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tenant disabled successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> disableTenant(
            @Parameter(description = "Tenant ID", required = true, example = "1")
            @PathVariable String id) {
        tenantService.disableTenant(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/enable")
    @Operation(
        summary = "Enable tenant",
        description = "Enable a tenant. Tenant can only be enabled if subscription is valid and not expired."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tenant enabled successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot enable tenant with expired subscription"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> enableTenant(
            @Parameter(description = "Tenant ID", required = true, example = "1")
            @PathVariable String id) {
        tenantService.enableTenant(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/subscription")
    @Operation(
        summary = "Update tenant subscription",
        description = "Update tenant subscription plan and expiration date. Tenant will be auto-disabled if subscription expires."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subscription updated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid subscription plan or date"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - SYSTEM_ADMIN role required"
        )
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> updateSubscription(
            @Parameter(description = "Tenant ID", required = true, example = "1")
            @PathVariable String id,
            @Parameter(description = "Subscription plan", required = true, example = "PROFESSIONAL")
            @RequestParam String plan,
            @Parameter(description = "Subscription expiration date", required = true, example = "2025-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expires) {
        tenantService.updateSubscription(id, com.pos.entity.Tenant.SubscriptionPlan.valueOf(plan), expires);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/active-status")
    @Operation(
        summary = "Check tenant active status",
        description = "Check if a tenant is currently active and can accept requests."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status check completed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Active Status",
                    value = "true"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        )
    })
    public ResponseEntity<Boolean> checkTenantActive(
            @Parameter(description = "Tenant ID", required = true, example = "1")
            @PathVariable String id) {
        TenantDto tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(tenant.getActive());
    }

    @GetMapping("/subdomain/{subdomain}/active-status")
    @Operation(
        summary = "Check tenant active status by subdomain",
        description = "Check if a tenant is currently active by subdomain. Used by tenant interceptor for request validation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status check completed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Active Status by Subdomain",
                    value = "true"
                )
            )
        )
    })
    public ResponseEntity<Boolean> checkTenantActiveBySubdomain(
            @Parameter(description = "Tenant subdomain", required = true, example = "demo")
            @PathVariable String subdomain) {
        boolean isActive = tenantService.isTenantActive(subdomain);
        return ResponseEntity.ok(isActive);
    }
}
