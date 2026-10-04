package com.wellfrog.repository;

import com.wellfrog.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserId(Long userId);
    List<Activity> findByUserIdAndParentIdIsNull(Long userId);
    List<Activity> findByUserIdAndParentIdIsNullAndActiveTrue(Long userId);
    List<Activity> findByUserIdAndActiveTrue(Long userId);
    List<Activity> findByUserIdAndParentId(Long userId, Long parentId);
}
