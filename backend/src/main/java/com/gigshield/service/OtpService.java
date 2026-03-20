package com.gigshield.service;

import com.gigshield.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OtpRepository otpRepository;

    @Value("${otp.expiry.minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.max.attempts:3}")
    private int maxAttempts;

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_ATTEMPT_PREFIX = "otp_attempt:";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateAndStoreOtp(String mobile) {
        // Check rate limiting
        String attemptKey = OTP_ATTEMPT_PREFIX + mobile;
        Object attempts = redisTemplate.opsForValue().get(attemptKey);
        if (attempts != null && Integer.parseInt(attempts.toString()) >= maxAttempts) {
            throw new RuntimeException("OTP request limit exceeded. Please try again after 1 hour.");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", RANDOM.nextInt(999999));

        // Store in Redis with TTL
        String key = OTP_KEY_PREFIX + mobile;
        redisTemplate.opsForValue().set(key, otp, otpExpiryMinutes, TimeUnit.MINUTES);

        // Increment attempts
        redisTemplate.opsForValue().increment(attemptKey);
        redisTemplate.expire(attemptKey, 1, TimeUnit.HOURS);

        // Log OTP (replace with Twilio or SMS gateway in production)
        log.info("📱 OTP for mobile {}: {} (expires in {} minutes)", mobile, otp, otpExpiryMinutes);

        return otp;
    }

    public boolean verifyOtp(String mobile, String otp) {
        String key = OTP_KEY_PREFIX + mobile;
        Object storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("OTP not found or expired for mobile: {}", mobile);
            return false;
        }

        if (storedOtp.toString().equals(otp)) {
            // Invalidate OTP after use
            redisTemplate.delete(key);
            // Clear attempt counter on success
            redisTemplate.delete(OTP_ATTEMPT_PREFIX + mobile);
            return true;
        }

        log.warn("OTP mismatch for mobile: {}", mobile);
        return false;
    }

    public int getOtpExpirySeconds() {
        return otpExpiryMinutes * 60;
    }
}
