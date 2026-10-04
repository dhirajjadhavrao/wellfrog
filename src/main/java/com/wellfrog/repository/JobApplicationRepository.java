package com.wellfrog.repository;

import com.wellfrog.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserIdOrderByAppliedDateDesc(Long userId);
    List<JobApplication> findByUserIdAndStatus(Long userId, String status);
}
