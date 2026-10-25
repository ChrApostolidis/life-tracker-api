package com.lifeTracker.life_tracker_api.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    // Unordered: the list is sorted by lastActivityAt, which only the service can compute.
    List<Project> findByDeletedAtIsNull();
}
