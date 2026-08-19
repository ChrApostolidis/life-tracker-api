package com.lifeTracker.life_tracker_api.tasks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create tasks against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class TaskServiceTest {

    private static final ZoneId ATHENS = ZoneId.of("Europe/Athens");

    @Autowired
    private TaskService taskService;

    private Task recurringTemplate(String title, String recurrence, ZonedDateTime start) {
        return taskService.createTask(new TaskCreateRequest(
                title, start.toInstant(), null, recurrence, null, null, null, null));
    }

    private Task scheduledTask(String title, Instant when) {
        return taskService.createTask(new TaskCreateRequest(title, when, null, null, null, null, null, null));
    }

    private String iso(ZonedDateTime z) {
        return z.toInstant().toString();
    }

    @Test
    void dailyRecurrenceExpandsOneOccurrencePerDay() {
        ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 9, 0, 0, 0, ATHENS);
        recurringTemplate("Daily standup", "daily", start);

        ZonedDateTime from = ZonedDateTime.of(2026, 6, 5, 0, 0, 0, 0, ATHENS);
        ZonedDateTime to = ZonedDateTime.of(2026, 6, 10, 0, 0, 0, 0, ATHENS);
        List<Task> occurrences = taskService.getTasksInRange(iso(from), iso(to));

        assertEquals(5, occurrences.size());
        assertTrue(occurrences.stream().allMatch(o -> "daily".equals(o.getRecurrence())));
        assertTrue(occurrences.stream().allMatch(o -> o.getOccurrenceDate() != null));
        assertNull(occurrences.get(0).getCompletedAt());
    }

    @Test
    void weeklyRecurrenceRespectsRecurrenceDay() {
        // Start on a Monday, target Wednesday (JS dow 3).
        ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 9, 0, 0, 0, ATHENS); // Monday
        Task task = recurringTemplate("Gym", "weekly", start);
        taskService.updateTask(task.getId(), new TaskPatchRequest(null, null, null, null, 3, null)); // Wednesday

        ZonedDateTime from = ZonedDateTime.of(2026, 6, 1, 0, 0, 0, 0, ATHENS);
        ZonedDateTime to = ZonedDateTime.of(2026, 6, 22, 0, 0, 0, 0, ATHENS);
        List<Task> occurrences = taskService.getTasksInRange(iso(from), iso(to));

        assertEquals(3, occurrences.size());
        occurrences.forEach(o ->
                assertEquals(java.time.DayOfWeek.WEDNESDAY, o.getScheduledAt().atZone(ATHENS).getDayOfWeek()));
    }

    @Test
    void monthlyRecurrenceSkipsMonthsMissingTheDay() {
        // Jan 31 start: Feb has no 31st, should be skipped (not clamped to 28).
        ZonedDateTime start = ZonedDateTime.of(2026, 1, 31, 9, 0, 0, 0, ATHENS);
        recurringTemplate("Rent", "monthly", start);

        ZonedDateTime from = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ATHENS);
        ZonedDateTime to = ZonedDateTime.of(2026, 4, 1, 0, 0, 0, 0, ATHENS);
        List<Task> occurrences = taskService.getTasksInRange(iso(from), iso(to));

        // Jan 31 and Mar 31 only — Feb skipped entirely.
        assertEquals(2, occurrences.size());
        assertEquals(1, occurrences.get(0).getScheduledAt().atZone(ATHENS).getMonthValue());
        assertEquals(3, occurrences.get(1).getScheduledAt().atZone(ATHENS).getMonthValue());
        assertEquals(31, occurrences.get(1).getScheduledAt().atZone(ATHENS).getDayOfMonth());
    }

    @Test
    void completingOneOccurrenceDoesNotAffectOthers() {
        ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 9, 0, 0, 0, ATHENS);
        Task template = recurringTemplate("Daily journal", "daily", start);

        Task completed = taskService.completeOccurrence(template.getId(), "2026-06-03");
        assertEquals("2026-06-03", completed.getOccurrenceDate());
        assertTrue(completed.getCompletedAt() != null);

        ZonedDateTime from = ZonedDateTime.of(2026, 6, 1, 0, 0, 0, 0, ATHENS);
        ZonedDateTime to = ZonedDateTime.of(2026, 6, 6, 0, 0, 0, 0, ATHENS);
        List<Task> occurrences = taskService.getTasksInRange(iso(from), iso(to));

        assertEquals(5, occurrences.size());
        long completedCount = occurrences.stream().filter(o -> o.getCompletedAt() != null).count();
        assertEquals(1, completedCount);
        assertEquals("2026-06-03",
                occurrences.stream().filter(o -> o.getCompletedAt() != null).findFirst().get().getOccurrenceDate());
    }

    @Test
    void uncompleteReversesCompletion() {
        ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 9, 0, 0, 0, ATHENS);
        Task template = recurringTemplate("Daily journal", "daily", start);

        taskService.completeOccurrence(template.getId(), "2026-06-02");
        Task reverted = taskService.uncompleteOccurrence(template.getId(), "2026-06-02");
        assertNull(reverted.getCompletedAt());
    }

    @Test
    void completingAnInvalidOccurrenceDateFails() {
        ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 9, 0, 0, 0, ATHENS);
        // Weekly on Monday — Tuesday is never a valid occurrence date.
        Task template = recurringTemplate("Gym", "weekly", start);

        assertThrows(ResponseStatusException.class,
                () -> taskService.completeOccurrence(template.getId(), "2026-06-02"));
    }

    @Test
    void templateRowNeverAppearsRawInRangeResults() {
        ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 9, 0, 0, 0, ATHENS);
        Task template = recurringTemplate("Daily journal", "daily", start);

        ZonedDateTime from = ZonedDateTime.of(2026, 6, 1, 0, 0, 0, 0, ATHENS);
        ZonedDateTime to = ZonedDateTime.of(2026, 6, 2, 0, 0, 0, 0, ATHENS);
        List<Task> occurrences = taskService.getTasksInRange(iso(from), iso(to));

        assertEquals(1, occurrences.size());
        // Same id as the template (occurrences carry the template's id), but it's
        // the expanded occurrence, distinguishable by occurrenceDate being set.
        assertEquals(template.getId(), occurrences.get(0).getId());
        assertEquals("2026-06-01", occurrences.get(0).getOccurrenceDate());
    }

    @Test
    void creatingRecurringTaskWithoutScheduledAtFails() {
        assertThrows(ResponseStatusException.class, () -> taskService.createTask(
                new TaskCreateRequest("No date", null, null, "daily", null, null, null, null)));
    }

    @Test
    void overdueExcludesRecurringAndFutureAndCompletedTasks() {
        Instant yesterday = ZonedDateTime.now(ATHENS).minusDays(1).toInstant();
        Instant tomorrow = ZonedDateTime.now(ATHENS).plusDays(1).toInstant();

        scheduledTask("Overdue open", yesterday);

        Task overdueDone = scheduledTask("Overdue but done", yesterday);
        taskService.completeTask(overdueDone.getId());

        scheduledTask("Future", tomorrow);

        recurringTemplate("Missed recurring", "daily", ZonedDateTime.now(ATHENS).minusDays(5));

        List<Task> overdue = taskService.getOverdueTasks();

        assertTrue(overdue.stream().anyMatch(t -> t.getTitle().equals("Overdue open")));
        assertTrue(overdue.stream().noneMatch(t -> t.getTitle().equals("Overdue but done")));
        assertTrue(overdue.stream().noneMatch(t -> t.getTitle().equals("Future")));
        assertTrue(overdue.stream().noneMatch(t -> t.getTitle().equals("Missed recurring")));
    }

    @Test
    void softDeletedTaskIsInvisibleAndImmutable() {
        Task task = scheduledTask("Gone", Instant.now());
        taskService.deleteTask(task.getId());

        assertTrue(taskService.getTaskById(task.getId()).isEmpty());
        assertThrows(ResponseStatusException.class, () -> taskService.updateTask(
                task.getId(), new TaskPatchRequest("New title", null, null, null, null, null)));
        assertThrows(ResponseStatusException.class, () -> taskService.completeTask(task.getId()));
        assertThrows(ResponseStatusException.class, () -> taskService.unscheduleTask(task.getId()));

        // Restore is the one verb that still reaches a deleted row.
        taskService.restoreTask(task.getId());
        assertTrue(taskService.getTaskById(task.getId()).isPresent());
    }

    @Test
    void unscheduleClearsDateAndRecurrence() {
        Task template = recurringTemplate("Standup", "daily", ZonedDateTime.now(ATHENS));

        Task unscheduled = taskService.unscheduleTask(template.getId());

        assertNull(unscheduled.getScheduledAt());
        assertNull(unscheduled.getRecurrence());
        assertNull(unscheduled.getRecurrenceDay());
        assertNull(unscheduled.getRecurrenceUntil());
        assertTrue(taskService.getInbox().stream().anyMatch(t -> t.getId().equals(template.getId())));
    }

    @Test
    void patchingBlankTitleIsRejected() {
        Task task = scheduledTask("Real title", Instant.now());
        assertThrows(ResponseStatusException.class, () -> taskService.updateTask(
                task.getId(), new TaskPatchRequest("   ", null, null, null, null, null)));
    }

    @Test
    void createIgnoresClientSuppliedCompletionAndKeepsDefaultSource() {
        // The DTO has no completedAt/deletedAt/id fields at all, so a client
        // simply cannot set them — this pins that contract.
        Task task = taskService.createTask(
                new TaskCreateRequest("Fresh", null, null, null, null, null, null, null));

        assertNull(task.getCompletedAt());
        assertNull(task.getDeletedAt());
        assertEquals("text", task.getSource());
        assertNotNull(task.getId());
    }
}
