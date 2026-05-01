package com.pos.dto;

import lombok.Data;

@Data
public class VerifyOTPDto {
    private String email;
    private String code;
}
