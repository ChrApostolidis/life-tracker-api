package com.lifeTracker.life_tracker_api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// client decides how the session travels: a cookie for 'web', a token in the
// response body for the others. Absent means web.
public record LoginRequest(
        @NotBlank String password,
        @Pattern(regexp = "web|extension|mobile") String client
) {}
