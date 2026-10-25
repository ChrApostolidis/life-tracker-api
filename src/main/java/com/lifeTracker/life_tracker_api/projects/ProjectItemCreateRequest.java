package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// projectId comes from the path and position is assigned by the service.
// stage defaults to 'todo' and priority to 'normal'.
public record ProjectItemCreateRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @Pattern(regexp = "todo|doing|done|blocked") String stage,
        @Pattern(regexp = "low|normal|urgent") String priority,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String dueOn
) {}
