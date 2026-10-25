package com.lifeTracker.life_tracker_api.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectItemRepository extends JpaRepository<ProjectItem, String> {

    // Board order (stage, then position) is applied by the service: the stage
    // order todo/doing/done/blocked isn't alphabetical.
    List<ProjectItem> findByProjectIdAndDeletedAtIsNull(String projectId);

    List<ProjectItem> findByProjectIdInAndDeletedAtIsNull(Collection<String> projectIds);

    List<ProjectItem> findByProjectIdAndStageAndDeletedAtIsNullOrderByPositionAsc(String projectId, String stage);

    Optional<ProjectItem> findTopByProjectIdAndStageAndDeletedAtIsNullOrderByPositionDesc(String projectId, String stage);
}
