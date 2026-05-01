package com.pos.controller;

import com.pos.dto.*;
import com.pos.service.AuthService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Multi-tenant authentication management APIs")
public class AuthController {

    private final AuthService authService;
    private final com.pos.service.VerificationCodeService verificationCodeService;

    @PostMapping("/request-code")
    @Operation(
        summary = "Request verification code",
        description = "Request a 4-digit verification code to be sent to the provided email. The code will expire in 5 minutes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verification code sent successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid email format"
        )
    })
    public ResponseEntity<?> requestVerificationCode(@Valid @RequestBody GetOTPDto req) {
        return authService.getOTPCode(req);
    }


    @PostMapping("/verify-otp")
    @Operation(
            summary = "Verify otp code",
            description = "Verify oTP code sent to email to get authentication details"
    )
    public ResponseEntity<?> requestVerificationCode(@Valid @RequestBody VerifyOTPDto req) {
        return authService.verifyOTP(req);
    }

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticate user with username and password. The tenant is automatically detected from the subdomain in the request URL. " +
                    "Returns a JWT token that includes the tenant ID for subsequent requests.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User login credentials",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthRequest.class),
                examples = @ExampleObject(
                    name = "Login Example",
                    value = "{\"username\":\"admin\",\"password\":\"password\"}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"type\":\"Bearer\",\"user\":{\"id\":1,\"username\":\"admin\",\"email\":\"admin@demo.com\",\"role\":\"ADMIN\",\"tenantId\":1,\"tenantName\":\"Demo Store\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed - invalid credentials or disabled tenant",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = "{\"timestamp\":\"2024-01-01T12:00:00\",\"status\":401,\"error\":\"Authentication Failed\",\"message\":\"Invalid username or password\",\"path\":\"/api/auth/login\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Tenant is disabled or subscription expired",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Tenant Disabled",
                    value = "{\"timestamp\":\"2024-01-01T12:00:00\",\"status\":403,\"error\":\"Tenant Error\",\"message\":\"Tenant is disabled\",\"path\":\"/api/auth/login\"}"
                )
            )
        )
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Login request containing username and password", required = true)
            @Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Register a new user in the system. The user will be associated with the current tenant detected from the subdomain. " +
                    "Tenant must be active and have available user slots.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration details",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class),
                examples = @ExampleObject(
//                    name = "Registration Example",
//                    value = "{\"username\":\"newuser\",\"password\":\"password123\",\"email\":\"newuser@demo.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"phoneNumber\":\"5551234567\",\"role\":\"CASHIER\"}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registration successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class)
//                examples = @ExampleObject(
//                    name = "Registration Success"
////                    value = "{\"id\":4,\"username\":\"newuser\",\"email\":\"newuser@demo.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"phoneNumber\":\"5551234567\",\"role\":\"CASHIER\",\"enabled\":true,\"tenantId\":1,\"tenantName\":\"Demo Store\"}"
//                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed or user limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = "{\"timestamp\":\"2024-01-01T12:00:00\",\"status\":400,\"error\":\"Validation Failed\",\"message\":\"Request validation failed\",\"validationErrors\":{\"username\":\"Username must be between 3 and 50 characters\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Tenant is disabled or maximum user limit reached",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "User Limit Error",
                    value = "{\"timestamp\":\"2024-01-01T12:00:00\",\"status\":403,\"error\":\"Tenant Error\",\"message\":\"Maximum user limit reached for this tenant\",\"path\":\"/api/auth/register\"}"
                )
            )
        )
    })
    public ResponseEntity<UserDto> register(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody UserDto userDto) {
        UserDto response = authService.register(userDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
