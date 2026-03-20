package com.gigshield.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobile;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @NotBlank(message = "Platform is required")
    private String platform;   // ZOMATO or SWIGGY

    private String platformPartnerId;

    @NotBlank(message = "City is required")
    private String city;

    private String zone;
    private String pincode;

    @DecimalMin(value = "400", message = "Min earnings ₹400")
    @DecimalMax(value = "2000", message = "Max earnings ₹2000")
    private Double avgDailyEarnings;

    @DecimalMin(value = "4")
    @DecimalMax(value = "14")
    private Double avgDailyHours;

    private String upiId;
    private String bankAccount;
    private String ifsc;
    private String aadharLast4;
}
