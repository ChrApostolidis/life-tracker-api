package com.lifeTracker.life_tracker_api.money;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

// Request DTO so clients can't set id/timestamps/deletedAt on create. The
// category whitelist depends on type, so it's checked in the service.
public record MoneyEntryCreateRequest(
        @NotBlank @Pattern(regexp = "expense|income") String type,
        @NotNull @Positive Integer amountCents,
        @NotBlank String label,
        String category,
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String occurredOn
) {}
