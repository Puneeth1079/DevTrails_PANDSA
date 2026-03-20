package com.gigshield.repository;

import com.gigshield.model.Claim;
import com.gigshield.model.enums.ClaimStatus;
import com.gigshield.model.enums.TriggerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Page<Claim> findByWorkerId(Long workerId, Pageable pageable);
    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);
    long countByStatus(ClaimStatus status);

    @Query("SELECT c FROM Claim c WHERE c.worker.id = :workerId AND c.triggerType = :triggerType AND c.claimedAt >= :since")
    List<Claim> findRecentClaimsByWorkerAndType(@Param("workerId") Long workerId,
                                                 @Param("triggerType") TriggerType triggerType,
                                                 @Param("since") LocalDateTime since);

    @Query("SELECT c FROM Claim c WHERE c.worker.id = :workerId AND c.claimedAt >= :since")
    List<Claim> findRecentClaimsByWorker(@Param("workerId") Long workerId,
                                          @Param("since") LocalDateTime since);

    @Query("SELECT c FROM Claim c WHERE c.disruptionEvent.id = :eventId")
    List<Claim> findByDisruptionEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.worker.id = :workerId AND MONTH(c.claimedAt) = MONTH(CURRENT_DATE) AND YEAR(c.claimedAt) = YEAR(CURRENT_DATE)")
    Long countClaimsThisMonth(@Param("workerId") Long workerId);

    @Query("SELECT c.triggerType, COUNT(c) FROM Claim c GROUP BY c.triggerType")
    List<Object[]> countByTriggerType();
}
