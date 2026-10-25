package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// PATCH payload — null = leave unchanged. A blank string clears the optional
// text fields. Moving status to ongoing/finished auto-stamps startedOn/finishedOn
// in the service when still empty; sending the date here overrides it.
public record ProjectPatchRequest(
        @Size(max = 120) String name,
        @Size(max = 200) String pitch,
        String description,
        @Pattern(regexp = "idea|ongoing|finished") String status,
        @Size(max = 500) String repoUrl,
        @Size(max = 500) String liveUrl,
        @Size(max = 500) String techStack,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String startedOn,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String finishedOn,
        @Size(max = 500) String tags
) {}
