package com.lifeTracker.life_tracker_api.habits;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create habits/checks against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class HabitServiceTest {

    @Autowired
    private HabitService habitService;

    private Habit create(String name) {
        return habitService.create(new HabitCreateRequest(name));
    }

    @Test
    void createSetsDefaults() {
        Habit habit = create("Read");
        assertNotNull(habit.getId());
        assertNotNull(habit.getCreatedAt());
        assertNull(habit.getArchivedAt());
    }

    @Test
    void checkingTwiceForSameDayIsIdempotent() {
        Habit habit = create("Read");
        HabitCheck first = habitService.check(habit.getId(), "2026-01-05");
        HabitCheck second = habitService.check(habit.getId(), "2026-01-05");
        assertEquals(first.getId(), second.getId());

        List<HabitCheck> checks = habitService.listChecks("2026-01-01", "2026-02-01");
        long count = checks.stream()
                .filter(c -> c.getHabitId().equals(habit.getId()) && c.getCheckedOn().equals("2026-01-05"))
                .count();
        assertEquals(1, count);
    }

    @Test
    void uncheckThenRecheckSameDaySucceeds() {
        Habit habit = create("Read");
        habitService.check(habit.getId(), "2026-01-05");
        habitService.uncheck(habit.getId(), "2026-01-05");

        List<HabitCheck> afterUncheck = habitService.listChecks("2026-01-01", "2026-02-01");
        assertTrue(afterUncheck.stream().noneMatch(c -> c.getHabitId().equals(habit.getId())));

        HabitCheck rechecked = habitService.check(habit.getId(), "2026-01-05");
        assertNotNull(rechecked.getId());
    }

    @Test
    void listChecksRespectsHalfOpenRange() {
        Habit habit = create("Read");
        habitService.check(habit.getId(), "2026-01-01");
        habitService.check(habit.getId(), "2026-02-01");

        List<HabitCheck> checks = habitService.listChecks("2026-01-01", "2026-02-01");
        List<String> days = checks.stream()
                .filter(c -> c.getHabitId().equals(habit.getId()))
                .map(HabitCheck::getCheckedOn)
                .toList();
        assertTrue(days.contains("2026-01-01"));
        assertTrue(!days.contains("2026-02-01"));
    }

    @Test
    void archivingDoesNotHideCheckHistory() {
        Habit habit = create("Read");
        habitService.check(habit.getId(), "2026-01-05");
        habitService.archive(habit.getId());

        List<HabitCheck> checks = habitService.listChecks("2026-01-01", "2026-02-01");
        assertTrue(checks.stream().anyMatch(c -> c.getHabitId().equals(habit.getId())));
    }

    @Test
    void renamingToBlankIsRejected() {
        Habit habit = create("Read");
        assertThrows(ResponseStatusException.class, () -> habitService.update(
                habit.getId(), new HabitUpdateRequest("   ")));
    }

    @Test
    void checkingUnknownHabitThrows() {
        assertThrows(ResponseStatusException.class, () -> habitService.check("does-not-exist", "2026-01-05"));
    }
}
