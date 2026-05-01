package com.pos.dto;

import lombok.Data;

@Data
public class VerifyOTPResponse {
    private String access_token;
    private String refresh_token;
    private String tenantId;
    private String name;
    private String tenantName;
    private String email;
}
