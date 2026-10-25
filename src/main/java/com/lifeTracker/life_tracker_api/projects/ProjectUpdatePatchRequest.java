package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.constraints.Pattern;

// PATCH payload — null = leave unchanged. A blank body is a 400, not a delete.
public record ProjectUpdatePatchRequest(
        String body,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String entryDate
) {}
