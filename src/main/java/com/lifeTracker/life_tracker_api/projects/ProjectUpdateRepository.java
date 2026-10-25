package com.lifeTracker.life_tracker_api.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProjectUpdateRepository extends JpaRepository<ProjectUpdate, String> {

    // Several updates can share an entryDate, so createdAt is the tiebreak.
    List<ProjectUpdate> findByProjectIdAndDeletedAtIsNullOrderByEntryDateDescCreatedAtDesc(String projectId);

    List<ProjectUpdate> findByProjectIdInAndDeletedAtIsNull(Collection<String> projectIds);
}
