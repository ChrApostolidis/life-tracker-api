package com.lifeTracker.life_tracker_api.projects;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// A pool card's worth. counts carries all four stages, zeros included, over
// live items. lastActivityAt is the latest of the project's updatedAt, its
// items' updatedAt and its updates' createdAt.
public record ProjectSummaryResponse(
        Project project,
        Map<String, Integer> counts,
        Instant lastActivityAt
) {

    /** {@code items} and {@code updates} must be the project's live rows. */
    static ProjectSummaryResponse of(Project project, List<ProjectItem> items, List<ProjectUpdate> updates) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        ProjectItemService.STAGES.forEach(stage -> counts.put(stage, 0));
        items.forEach(item -> counts.merge(item.getStage(), 1, Integer::sum));

        Instant lastActivityAt = Stream.concat(
                        Stream.concat(Stream.of(project.getUpdatedAt()), items.stream().map(ProjectItem::getUpdatedAt)),
                        updates.stream().map(ProjectUpdate::getCreatedAt))
                .max(Comparator.naturalOrder())
                .orElseThrow();
        return new ProjectSummaryResponse(project, counts, lastActivityAt);
    }
}
