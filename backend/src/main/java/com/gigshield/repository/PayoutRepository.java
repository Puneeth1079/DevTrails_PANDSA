package com.gigshield.repository;

import com.gigshield.model.Payout;
import com.gigshield.model.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    Optional<Payout> findByClaimId(Long claimId);
    List<Payout> findByWorkerId(Long workerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.worker.id = :workerId AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPayoutsByWorker(@Param("workerId") Long workerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.status = 'SUCCESS' AND MONTH(p.completedAt) = MONTH(CURRENT_DATE) AND YEAR(p.completedAt) = YEAR(CURRENT_DATE)")
    BigDecimal sumPayoutsThisMonth();
}
