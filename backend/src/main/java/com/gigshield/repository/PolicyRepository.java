package com.gigshield.repository;

import com.gigshield.model.Policy;
import com.gigshield.model.enums.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Page<Policy> findByWorkerId(Long workerId, Pageable pageable);
    List<Policy> findByWorkerIdAndStatus(Long workerId, PolicyStatus status);
    long countByStatus(PolicyStatus status);

    @Query("SELECT p FROM Policy p WHERE p.worker.city = :city AND p.status = 'ACTIVE'")
    List<Policy> findActivePoliciesByCity(@Param("city") String city);

    @Query("SELECT p FROM Policy p WHERE p.worker.city = :city AND p.worker.zone = :zone AND p.status = 'ACTIVE'")
    List<Policy> findActivePoliciesByCityAndZone(@Param("city") String city, @Param("zone") String zone);

    @Query("SELECT p FROM Policy p WHERE p.endDate BETWEEN :today AND :twoDaysLater AND p.status = 'ACTIVE'")
    List<Policy> findExpiringSoon(@Param("today") LocalDate today, @Param("twoDaysLater") LocalDate twoDaysLater);
}
