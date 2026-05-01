package com.pos.service;

import com.pos.context.TenantContext;
import com.pos.dto.*;
import com.pos.entity.Tenant;
import com.pos.entity.User;
import com.pos.exception.TenantException;
import com.pos.repository.TenantRepository;
import com.pos.repository.UserRepository;
import com.pos.security.JwtUtil;
import com.pos.utility.JsonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CODE_PREFIX = "verification_code:" ;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;
    private static final SecureRandom random = new SecureRandom();

    private final RedisTemplate<String,String> redisTemplate;
    @Autowired
    private EmailService emailService;

    public AuthResponse authenticate(AuthRequest request) {
        // Verify the code first
        if (!verificationCodeService.verifyCode(request.getEmail(), request.getCode())) {
            throw new RuntimeException("Invalid or expired verification code");
        }

        String currentTenant = TenantContext.getCurrentTenant();
        
        // If no tenant context, find user by email across all tenants
        if (currentTenant == null) {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));
            
            currentTenant = user.getTenant().getId();
        }

        // Verify tenant is active
        Tenant tenant = tenantRepository.findById(currentTenant)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        if (!tenant.getActive()) {
            throw new RuntimeException("Tenant is disabled");
        }
        
        User user = userRepository.findByEmailAndTenantId(request.getEmail(), currentTenant)
                .orElseThrow(() -> new RuntimeException("User not found in this tenant"));
        
        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }
        
        String token = jwtUtil.generateToken(user.getUsername());
        
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(convertToDto(user))
                .build();
    }

    public UserDto register(UserDto userDto) {
        Optional<Tenant> tenantOptional =  tenantRepository.findByContactEmailOrContactPhone(userDto.getEmail(),userDto.getPhoneNumber());
        if(tenantOptional.isPresent()){
            throw new TenantException("Account with email already exist");
        }
        Tenant tenant =  Tenant.builder()
                .active(false)
                .contactEmail(userDto.getEmail())
                .companyName(userDto.getCompanyName())
                .name(userDto.getCompanyName())
                .maxUsers(5)
                .subscriptionPlan(Tenant.SubscriptionPlan.BASIC)
                .subdomain(null)
                .currentUserCount(1)
                .build();

        Tenant saveTenant =  tenantRepository.save(tenant);
        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .enabled(false)
                .tenant(saveTenant)
                .lastName(userDto.getLastName())
                .phoneNumber(userDto.getPhoneNumber())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        //send email to user for verification
        emailService.sendVerificationLink(savedUser.getEmail(),savedUser.getId());
        return convertToDto(savedUser);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.getEnabled())
                .build();
    }

    public ResponseEntity<?> getOTPCode(@Valid GetOTPDto req){
        String email = req.getEmail();
        Optional<User> userOptional =  userRepository.findByEmail(email);
        if (userOptional.isEmpty()){
            return JsonResponse.notFound("Account with this email not found");
        }

        User user =  userOptional.get();
        Tenant tenant =  user.getTenant();

        if(!tenant.getActive() || !user.getEnabled()){
            return JsonResponse.error(HttpStatus.FORBIDDEN,"Account disabled");
        }


        String code =  generateCode(4);

        String key = CODE_PREFIX + email;

        //add otp code to redi
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
        emailService.sendVerificationCode(email, code);
        return JsonResponse.success(HttpStatus.OK,"OTP code sent to "+ email);
    }

    private String generateCode(Integer length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }


    public ResponseEntity<?> verifyOTP(@Valid VerifyOTPDto req){
        String email =  req.getEmail();
        String code =  req.getCode();
        String otpIsPresent =  redisTemplate.opsForValue().get(CODE_PREFIX+email);
        if(otpIsPresent == null){
            return JsonResponse.error(HttpStatus.FORBIDDEN,"No OTP request found or code expired");
        }

        if(!code.equals(otpIsPresent)){
            return JsonResponse.error(HttpStatus.FORBIDDEN,"Invalid OTP code");

        }

        Optional<User> userOptional =  userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            User user =  userOptional.get();

            Tenant tenant =  user.getTenant();


            VerifyOTPResponse response =  new VerifyOTPResponse();
            response.setAccess_token(jwtUtil.generateToken(user));
            response.setRefresh_token(jwtUtil.generateRefreshToken(user));
            response.setName(user.getFirstName()+" "+user.getLastName());
            response.setEmail(user.getEmail());
            response.setTenantId(tenant.getId().toString());
            response.setTenantName(tenant.getName());

            return JsonResponse.success(HttpStatus.OK,response);
        }

        return null;

    }


}
