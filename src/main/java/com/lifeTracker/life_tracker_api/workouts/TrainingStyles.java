package com.lifeTracker.life_tracker_api.workouts;

/**
 * Default rep ranges per training style. The style only moves compound lifts
 * far: isolation work stays in higher reps in every style, because a 5-rep
 * lateral raise or curl costs joints and adds little strength.
 */
final class TrainingStyles {

    // Used until a style is chosen, and the range most gym apps default to.
    static final String DEFAULT = "hypertrophy";

    record RepRange(int min, int max) {}

    private TrainingStyles() {}

    static RepRange rangeFor(String style, String kind) {
        boolean compound = "compound".equals(kind);
        return switch (style == null ? DEFAULT : style) {
            case "strength" -> compound ? new RepRange(3, 6) : new RepRange(8, 12);
            case "balanced" -> compound ? new RepRange(5, 8) : new RepRange(8, 12);
            default -> compound ? new RepRange(8, 12) : new RepRange(10, 15);
        };
    }
}
