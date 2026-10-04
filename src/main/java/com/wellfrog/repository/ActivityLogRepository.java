package com.wellfrog.repository;

import com.wellfrog.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    List<ActivityLog> findByUserIdAndActivityIdAndLogDate(Long userId, Long activityId, LocalDate logDate);
    List<ActivityLog> findByUserIdAndActivityIdOrderByLogDateDesc(Long userId, Long activityId);

    @Modifying
    @Transactional
    void deleteByUserIdAndActivityId(Long userId, Long activityId);
}
