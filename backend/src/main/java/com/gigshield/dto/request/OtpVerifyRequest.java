package com.gigshield.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OtpVerifyRequest {

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String mobile;

    @NotBlank
    @Size(min = 6, max = 6)
    private String otp;
}
