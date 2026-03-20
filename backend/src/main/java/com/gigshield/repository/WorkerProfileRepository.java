package com.gigshield.repository;

import com.gigshield.model.WorkerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long> {
    Optional<WorkerProfile> findByUserId(Long userId);
    List<WorkerProfile> findByCity(String city);
    List<WorkerProfile> findByCityAndZone(String city, String zone);

    @Query("SELECT w FROM WorkerProfile w WHERE " +
           "(:city IS NULL OR w.city = :city) AND " +
           "(:platform IS NULL OR w.platform = :platform)")
    Page<WorkerProfile> findWithFilters(@Param("city") String city,
                                        @Param("platform") String platform,
                                        Pageable pageable);
    long count();
}
