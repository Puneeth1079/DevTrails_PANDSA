package com.gigshield.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    private String mobile;

    private String password;   // for password-based login
    private String otp;        // for OTP-based login
    private boolean otpLogin;  // toggle between modes
}
