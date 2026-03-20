package com.gigshield.service;

import com.gigshield.dto.request.LoginRequest;
import com.gigshield.dto.request.RegisterRequest;
import com.gigshield.dto.response.JwtResponse;
import com.gigshield.model.User;
import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.Role;
import com.gigshield.repository.UserRepository;
import com.gigshield.repository.WorkerProfileRepository;
import com.gigshield.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final OtpService otpService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public String sendOtp(String mobile) {
        String otp = otpService.generateAndStoreOtp(mobile);
        log.info("OTP sent to mobile: {}", mobile);
        return otp; // In production, send via SMS. Return for dev/demo purposes.
    }

    @Transactional
    public JwtResponse register(RegisterRequest req) {
        // Validate OTP
        if (!otpService.verifyOtp(req.getMobile(), req.getOtp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Check duplicate
        if (userRepository.existsByMobile(req.getMobile())) {
            throw new RuntimeException("Mobile number already registered");
        }

        // Create User
        User user = User.builder()
                .name(req.getName())
                .mobile(req.getMobile())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.WORKER)
                .isVerified(true)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        // Create WorkerProfile
        WorkerProfile profile = WorkerProfile.builder()
                .user(user)
                .platform(req.getPlatform() != null ? req.getPlatform() : "ZOMATO")
                .platformPartnerId(req.getPlatformPartnerId())
                .city(req.getCity())
                .zone(req.getZone())
                .pincode(req.getPincode())
                .avgDailyEarnings(req.getAvgDailyEarnings() != null
                        ? BigDecimal.valueOf(req.getAvgDailyEarnings()) : BigDecimal.valueOf(800))
                .avgDailyHours(req.getAvgDailyHours() != null
                        ? BigDecimal.valueOf(req.getAvgDailyHours()) : BigDecimal.valueOf(8))
                .upiId(req.getUpiId())
                .bankAccount(req.getBankAccount())
                .ifsc(req.getIfsc())
                .aadharLast4(req.getAadharLast4())
                .riskScore(calculateInitialRiskScore(req.getCity(), req.getAvgDailyEarnings()))
                .build();
        profile = workerProfileRepository.save(profile);

        String token = tokenProvider.generateTokenForMobile(user.getMobile(), "WORKER", user.getId());

        return JwtResponse.builder()
                .token(token)
                .role("WORKER")
                .name(user.getName())
                .mobile(user.getMobile())
                .userId(user.getId())
                .workerId(profile.getId())
                .build();
    }

    public JwtResponse login(LoginRequest req) {
        User user = userRepository.findByMobile(req.getMobile())
                .orElseThrow(() -> new RuntimeException("User not found with mobile: " + req.getMobile()));

        if (req.isOtpLogin()) {
            // OTP-based login
            if (!otpService.verifyOtp(req.getMobile(), req.getOtp())) {
                throw new RuntimeException("Invalid or expired OTP");
            }
        } else {
            // Password-based login
            if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Invalid password");
            }
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        Long workerId = null;
        if (user.getRole() == Role.WORKER) {
            WorkerProfile profile = workerProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile != null) workerId = profile.getId();
        }

        String token = tokenProvider.generateTokenForMobile(user.getMobile(), user.getRole().name(), user.getId());

        return JwtResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .name(user.getName())
                .mobile(user.getMobile())
                .userId(user.getId())
                .workerId(workerId)
                .build();
    }

    private BigDecimal calculateInitialRiskScore(String city, Double avgEarnings) {
        double score = 50.0;
        if (city != null) {
            switch (city.toLowerCase()) {
                case "mumbai", "delhi", "chennai" -> score += 15;
                case "bengaluru", "hyderabad", "kolkata" -> score += 10;
                case "pune", "ahmedabad" -> score += 5;
            }
        }
        if (avgEarnings != null) {
            if (avgEarnings < 600) score += 10;
            else if (avgEarnings > 1200) score -= 10;
        }
        return BigDecimal.valueOf(Math.min(100, Math.max(0, score)));
    }
}
