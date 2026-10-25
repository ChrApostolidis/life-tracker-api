package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Request DTO so clients can't set id/timestamps/dates on create. A new project
// is a name, a status, maybe a pitch, a dictated description and tags; the
// Overview tab fills in the rest.
public record ProjectCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "idea|ongoing|finished") String status,
        @Size(max = 200) String pitch,
        String description,
        @Size(max = 500) String tags
) {}
