package com.pos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {
    private String id;
    
    @NotBlank(message = "Tenant name is required")
    @Size(max = 100, message = "Tenant name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Subdomain is required")
    @Size(max = 50, message = "Subdomain must not exceed 50 characters")
    private String subdomain;
    
    private String description;
    
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Contact email is required")
    private String contactEmail;
    
    private String contactPhone;
    
    private Integer maxUsers;
    
    private Integer currentUserCount;
    
    private Boolean active;
    
    private String subscriptionPlan;
    
    private LocalDateTime subscriptionExpires;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
