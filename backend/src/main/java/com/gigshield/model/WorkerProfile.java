package com.gigshield.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "worker_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String platform = "ZOMATO";

    @Column(name = "platform_partner_id", length = 100)
    private String platformPartnerId;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String zone;

    @Column(length = 10)
    private String pincode;

    @Column(name = "avg_daily_earnings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal avgDailyEarnings = BigDecimal.valueOf(800.00);

    @Column(name = "avg_daily_hours", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal avgDailyHours = BigDecimal.valueOf(8.0);

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(name = "bank_account", length = 20)
    private String bankAccount;

    @Column(length = 12)
    private String ifsc;

    @Column(name = "aadhar_last4", length = 4)
    private String aadharLast4;

    @Column(name = "risk_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal riskScore = BigDecimal.valueOf(50.00);

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Policy> policies;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Claim> claims;
}
