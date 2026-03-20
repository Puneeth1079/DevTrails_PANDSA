package com.gigshield.controller;

import com.gigshield.dto.request.LoginRequest;
import com.gigshield.dto.request.OtpVerifyRequest;
import com.gigshield.dto.request.RegisterRequest;
import com.gigshield.dto.response.ApiResponse;
import com.gigshield.dto.response.JwtResponse;
import com.gigshield.service.AuthService;
import com.gigshield.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OTP-based registration and login")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    // @PostMapping("/send-otp")
    // @Operation(summary = "Send OTP to mobile number")
    // public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(@RequestBody
    // Map<String, String> req) {
    // String mobile = req.get("mobile");
    // authService.sendOtp(mobile);
    // return ResponseEntity.ok(ApiResponse.success(
    // Map.of("expiresIn", otpService.getOtpExpirySeconds()),
    // "OTP sent successfully"
    // ));
    // }
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(@RequestBody Map<String, String> req) {
        String mobile = req.get("mobile");

        if (mobile == null || mobile.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mobile number is required"));
        }

        authService.sendOtp(mobile);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("expiresIn", otpService.getOtpExpirySeconds()),
                "OTP sent successfully"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new worker with OTP verification")
    public ResponseEntity<ApiResponse<JwtResponse>> register(@Valid @RequestBody RegisterRequest req) {
        JwtResponse jwt = authService.register(req);
        return ResponseEntity.ok(ApiResponse.success(jwt, "Registration successful"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with password or OTP")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest req) {
        JwtResponse jwt = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(jwt, "Login successful"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP without registration")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        boolean valid = otpService.verifyOtp(req.getMobile(), req.getOtp());
        return ResponseEntity.ok(ApiResponse.success(valid, valid ? "OTP verified" : "Invalid OTP"));
    }
}
