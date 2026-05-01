package com.pos.service;

import com.pos.entity.User;
import com.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${verification.code.expiry}")
    private long codeExpiryMillis;

    private static final String CODE_PREFIX = "verification_code:";
    private static final int CODE_LENGTH = 4;
    private static final SecureRandom random = new SecureRandom();

    public void generateAndSendCode(String email) {
        Optional<User> userOptional =  userRepository.findByEmail(email);

        String code = generateCode();
        String key = CODE_PREFIX + email;
        
        redisTemplate.opsForValue().set(key, code, codeExpiryMillis, TimeUnit.MILLISECONDS);
        emailService.sendVerificationCode(email, code);
        
        log.info("Verification code generated and stored for email: {}", email);
    }

    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            log.warn("No verification code found for email: {}", email);
            return false;
        }
        
        boolean isValid = storedCode.equals(code);
        
        if (isValid) {
            redisTemplate.delete(key);
            log.info("Verification code verified and removed for email: {}", email);
        } else {
            log.warn("Invalid verification code for email: {}", email);
        }
        
        return isValid;
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
