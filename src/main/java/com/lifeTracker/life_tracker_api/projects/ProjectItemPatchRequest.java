package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// PATCH payload — null = leave unchanged. Stage and position change only
// through /move, and clearing dueOn has its own verb (/clear-due).
public record ProjectItemPatchRequest(
        @Size(max = 200) String title,
        String description,
        @Pattern(regexp = "low|normal|urgent") String priority,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String dueOn
) {}
