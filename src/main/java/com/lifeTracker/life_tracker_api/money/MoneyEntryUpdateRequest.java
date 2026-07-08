package com.lifeTracker.life_tracker_api.money;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

// PATCH payload — null = leave unchanged (same convention as tasks). type is
// deliberately not editable; delete and re-log to flip a direction.
public record MoneyEntryUpdateRequest(
        @Positive Integer amountCents,
        String label,
        String category,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String occurredOn
) {}
