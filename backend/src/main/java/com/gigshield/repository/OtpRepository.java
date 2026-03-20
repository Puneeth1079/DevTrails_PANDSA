package com.gigshield.repository;

import com.gigshield.model.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord, Long> {
    Optional<OtpRecord> findTopByMobileAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(String mobile, LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE OtpRecord o SET o.isUsed = true WHERE o.mobile = :mobile")
    void invalidateAllOtpsForMobile(@Param("mobile") String mobile);

    @Query("SELECT COUNT(o) FROM OtpRecord o WHERE o.mobile = :mobile AND o.createdAt >= :since")
    long countOtpRequestsSince(@Param("mobile") String mobile, @Param("since") LocalDateTime since);
}
