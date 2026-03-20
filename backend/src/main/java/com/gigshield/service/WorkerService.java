package com.gigshield.service;

import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.PolicyStatus;
import com.gigshield.repository.WorkerProfileRepository;
import com.gigshield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerProfileRepository workerProfileRepository;
    private final UserRepository userRepository;

    public WorkerProfile getWorkerByUserId(Long userId) {
        return workerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found for user: " + userId));
    }

    public WorkerProfile getWorkerById(Long workerId) {
        return workerProfileRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found: " + workerId));
    }

    public Page<WorkerProfile> getAllWorkers(String city, String platform, Pageable pageable) {
        return workerProfileRepository.findWithFilters(city, platform, pageable);
    }

    @Transactional
    public WorkerProfile updateWorkerProfile(Long workerId, WorkerProfile updatedProfile) {
        WorkerProfile existing = getWorkerById(workerId);
        if (updatedProfile.getCity() != null) existing.setCity(updatedProfile.getCity());
        if (updatedProfile.getZone() != null) existing.setZone(updatedProfile.getZone());
        if (updatedProfile.getPincode() != null) existing.setPincode(updatedProfile.getPincode());
        if (updatedProfile.getUpiId() != null) existing.setUpiId(updatedProfile.getUpiId());
        if (updatedProfile.getBankAccount() != null) existing.setBankAccount(updatedProfile.getBankAccount());
        if (updatedProfile.getIfsc() != null) existing.setIfsc(updatedProfile.getIfsc());
        if (updatedProfile.getAvgDailyEarnings() != null) existing.setAvgDailyEarnings(updatedProfile.getAvgDailyEarnings());
        if (updatedProfile.getAvgDailyHours() != null) existing.setAvgDailyHours(updatedProfile.getAvgDailyHours());
        if (updatedProfile.getLatitude() != null) existing.setLatitude(updatedProfile.getLatitude());
        if (updatedProfile.getLongitude() != null) existing.setLongitude(updatedProfile.getLongitude());
        return workerProfileRepository.save(existing);
    }

    public long getTotalWorkerCount() {
        return workerProfileRepository.count();
    }
}
