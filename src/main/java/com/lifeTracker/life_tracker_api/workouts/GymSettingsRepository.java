package com.lifeTracker.life_tracker_api.workouts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymSettingsRepository extends JpaRepository<GymSettings, String> {}
