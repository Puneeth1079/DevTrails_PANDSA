package com.gigshield.repository;

import com.gigshield.model.DisruptionEvent;
import com.gigshield.model.enums.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisruptionEventRepository extends JpaRepository<DisruptionEvent, Long> {
    List<DisruptionEvent> findByCityAndIsActiveTrue(String city);
    List<DisruptionEvent> findByIsActiveTrue();
    List<DisruptionEvent> findByCityAndTriggerTypeAndIsActiveTrue(String city, TriggerType triggerType);
}
