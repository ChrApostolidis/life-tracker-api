package com.lifeTracker.life_tracker_api.tasks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    // Bounds every expansion loop — comfortably covers decades of daily/weekly/
    // monthly recurrence without needing closed-form date math.
    private static final int MAX_OCCURRENCE_ITERATIONS = 5000;

    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final ZoneId zone;

    public TaskService(
            TaskRepository taskRepository,
            TaskCompletionRepository taskCompletionRepository,
            @Value("${app.timezone:Europe/Athens}") String timezoneId
    ) {
        this.taskRepository = taskRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.zone = ZoneId.of(timezoneId);
    }

    public Task createTask(TaskCreateRequest request) {
        if (request.recurrence() != null && request.scheduledAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recurring tasks must have a scheduledAt");
        }
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setTitle(request.title());
        task.setScheduledAt(request.scheduledAt());
        task.setDurationMin(request.durationMin());
        task.setRecurrence(request.recurrence());
        task.setRecurrenceDay(request.recurrenceDay());
        task.setRecurrenceUntil(request.recurrenceUntil());
        // Absent source keeps the entity default ('text') rather than nulling
        // a non-nullable column.
        if (request.source() != null) task.setSource(request.source());
        task.setRawTranscript(request.rawTranscript());
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id).filter(task -> task.getDeletedAt() == null);
    }

    public List<Task> getInbox() {
        return taskRepository.findByScheduledAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public Task updateTask(String id, TaskPatchRequest updates) {
        Task task = getLiveTaskOrThrow(id);

        if (updates.title() != null) {
            if (updates.title().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
            }
            task.setTitle(updates.title());
        }
        if (updates.scheduledAt() != null) task.setScheduledAt(updates.scheduledAt());
        if (updates.durationMin() != null) task.setDurationMin(updates.durationMin());
        if (updates.recurrence() != null) task.setRecurrence(updates.recurrence());
        if (updates.recurrenceDay() != null) task.setRecurrenceDay(updates.recurrenceDay());
        if (updates.recurrenceUntil() != null) task.setRecurrenceUntil(updates.recurrenceUntil());

        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    // The only way to clear a field, since PATCH reads null as "leave unchanged".
    // Clearing scheduledAt also clears the recurrence: a series with no start
    // instant has nothing to expand from, so leaving it would strand a template
    // that produces no occurrences.
    public Task unscheduleTask(String id) {
        Task task = getLiveTaskOrThrow(id);
        task.setScheduledAt(null);
        task.setRecurrence(null);
        task.setRecurrenceDay(null);
        task.setRecurrenceUntil(null);
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public void deleteTask(String id) {
        Task task = getLiveTaskOrThrow(id);
        task.setDeletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
    }

    public void restoreTask(String id) {
        Task task = getTaskOrThrow(id);
        task.setDeletedAt(null);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
    }

    public Task completeTask(String id) {
        Task task = getLiveTaskOrThrow(id);
        task.setCompletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public Task uncompleteTask(String id) {
        Task task = getLiveTaskOrThrow(id);
        task.setCompletedAt(null);
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    // Non-recurring tasks in range, plus every recurring template expanded
    // into occurrences that fall in the same window, merged and sorted.
    public List<Task> getTasksInRange(String from, String to) {
        Instant fromInstant = parseInstant(from, "from");
        Instant toInstant = parseInstant(to, "to");

        List<Task> plain = taskRepository.findTasksInRange(fromInstant, toInstant);
        List<Task> occurrences = expandTemplatesInRange(fromInstant, toInstant);

        List<Task> combined = new ArrayList<>(plain.size() + occurrences.size());
        combined.addAll(plain);
        combined.addAll(occurrences);
        combined.sort(Comparator.comparing(Task::getScheduledAt));
        return combined;
    }

    public List<Task> getOverdueTasks() {
        Instant startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        return taskRepository.findOverdueTasks(startOfToday);
    }

    public Task completeOccurrence(String id, String occurrenceDate) {
        Task template = getRecurringTemplateOrThrow(id);
        validateOccurrenceDate(template, occurrenceDate);

        TaskCompletion completion = taskCompletionRepository
                .findByTaskIdAndOccurrenceDate(id, occurrenceDate)
                .orElseGet(() -> {
                    TaskCompletion c = new TaskCompletion();
                    c.setId(UUID.randomUUID().toString());
                    c.setTaskId(id);
                    c.setOccurrenceDate(occurrenceDate);
                    c.setCreatedAt(Instant.now());
                    return c;
                });
        completion.setCompletedAt(Instant.now());
        taskCompletionRepository.save(completion);

        return occurrenceForDate(template, occurrenceDate, completion);
    }

    public Task uncompleteOccurrence(String id, String occurrenceDate) {
        Task template = getRecurringTemplateOrThrow(id);
        validateOccurrenceDate(template, occurrenceDate);
        taskCompletionRepository.deleteByTaskIdAndOccurrenceDate(id, occurrenceDate);
        return occurrenceForDate(template, occurrenceDate, null);
    }

    // ── Recurrence expansion ──

    private List<Task> expandTemplatesInRange(Instant from, Instant to) {
        List<Task> templates = taskRepository.findByRecurrenceIsNotNullAndDeletedAtIsNull();
        if (templates.isEmpty()) return List.of();

        List<String> templateIds = templates.stream().map(Task::getId).toList();
        Map<String, Map<String, TaskCompletion>> completionsByTaskAndDate =
                taskCompletionRepository.findByTaskIdIn(templateIds).stream()
                        .collect(Collectors.groupingBy(
                                TaskCompletion::getTaskId,
                                Collectors.toMap(TaskCompletion::getOccurrenceDate, c -> c)));

        List<Task> occurrences = new ArrayList<>();
        for (Task template : templates) {
            Map<String, TaskCompletion> completions =
                    completionsByTaskAndDate.getOrDefault(template.getId(), Map.of());
            for (Instant occurrenceInstant : expandOccurrences(template, from, to)) {
                String occurrenceDate = occurrenceInstant.atZone(zone).toLocalDate().toString();
                occurrences.add(toOccurrence(template, occurrenceInstant, occurrenceDate, completions.get(occurrenceDate)));
            }
        }
        return occurrences;
    }

    // Every instant this template's recurrence produces within [from, to).
    private List<Instant> expandOccurrences(Task template, Instant fromInstant, Instant toInstant) {
        List<Instant> result = new ArrayList<>();
        if (template.getScheduledAt() == null || template.getRecurrence() == null) return result;

        ZonedDateTime start = template.getScheduledAt().atZone(zone);
        ZonedDateTime from = fromInstant.atZone(zone);
        ZonedDateTime to = toInstant.atZone(zone);
        ZonedDateTime until = template.getRecurrenceUntil() != null
                ? template.getRecurrenceUntil().atZone(zone)
                : null;

        switch (template.getRecurrence()) {
            case "daily" -> {
                ZonedDateTime cursor = start;
                int guard = 0;
                while (cursor.isBefore(to) && guard++ < MAX_OCCURRENCE_ITERATIONS) {
                    addIfInRange(result, cursor, from, to, until);
                    cursor = cursor.plusDays(1);
                }
            }
            case "weekly" -> {
                int targetDow = weeklyTargetDow(template, start);
                ZonedDateTime cursor = alignToWeekday(start, targetDow);
                int guard = 0;
                while (cursor.isBefore(to) && guard++ < MAX_OCCURRENCE_ITERATIONS) {
                    addIfInRange(result, cursor, from, to, until);
                    cursor = cursor.plusWeeks(1);
                }
            }
            case "monthly" -> {
                int startDay = start.getDayOfMonth();
                YearMonth startYm = YearMonth.from(start);
                int guard = 0;
                while (guard < MAX_OCCURRENCE_ITERATIONS) {
                    YearMonth ym = startYm.plusMonths(guard++);
                    ZonedDateTime monthStart = ym.atDay(1).atStartOfDay(zone);
                    if (!monthStart.isBefore(to)) break; // this month and beyond are past the window
                    // Months without this day-of-month (e.g. no Feb 31) are skipped
                    // entirely rather than clamped, so the series never drifts off
                    // its original day.
                    if (startDay <= ym.lengthOfMonth()) {
                        ZonedDateTime candidate = ZonedDateTime.of(
                                ym.getYear(), ym.getMonthValue(), startDay,
                                start.getHour(), start.getMinute(), start.getSecond(), 0, zone);
                        addIfInRange(result, candidate, from, to, until);
                    }
                }
            }
            default -> { /* unknown recurrence value — no occurrences */ }
        }
        return result;
    }

    private void addIfInRange(List<Instant> out, ZonedDateTime candidate, ZonedDateTime from, ZonedDateTime to, ZonedDateTime until) {
        if (until != null && candidate.isAfter(until)) return;
        if (!candidate.isBefore(from) && candidate.isBefore(to)) {
            out.add(candidate.toInstant());
        }
    }

    // recurrenceDay is 0=Sun..6=Sat (the frontend's JS Date convention);
    // java.time's DayOfWeek is 1=Mon..7=Sun, hence the %7 conversion.
    private int weeklyTargetDow(Task template, ZonedDateTime start) {
        Integer day = template.getRecurrenceDay();
        return day != null ? Math.floorMod(day, 7) : (start.getDayOfWeek().getValue() % 7);
    }

    private ZonedDateTime alignToWeekday(ZonedDateTime start, int targetJsDow) {
        ZonedDateTime cursor = start;
        int guard = 0;
        while ((cursor.getDayOfWeek().getValue() % 7) != targetJsDow && guard++ < 7) {
            cursor = cursor.plusDays(1);
        }
        return cursor;
    }

    private Task toOccurrence(Task template, Instant occurrenceInstant, String occurrenceDate, TaskCompletion completion) {
        Task occurrence = new Task();
        occurrence.setId(template.getId());
        occurrence.setTitle(template.getTitle());
        occurrence.setScheduledAt(occurrenceInstant);
        occurrence.setDurationMin(template.getDurationMin());
        occurrence.setRecurrence(template.getRecurrence());
        occurrence.setRecurrenceDay(template.getRecurrenceDay());
        occurrence.setRecurrenceUntil(template.getRecurrenceUntil());
        occurrence.setCompletedAt(completion != null ? completion.getCompletedAt() : null);
        occurrence.setDeletedAt(null);
        occurrence.setSource(template.getSource());
        occurrence.setRawTranscript(template.getRawTranscript());
        occurrence.setCreatedAt(template.getCreatedAt());
        occurrence.setUpdatedAt(template.getUpdatedAt());
        occurrence.setOccurrenceDate(occurrenceDate);
        return occurrence;
    }

    // Re-runs expansion narrowed to a single day — the only way to turn a bare
    // occurrenceDate back into the instant it represents, and it reuses the
    // exact same date math the range query and validation use.
    private Task occurrenceForDate(Task template, String occurrenceDate, TaskCompletion completion) {
        LocalDate date = LocalDate.parse(occurrenceDate);
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();
        Instant occurrenceInstant = expandOccurrences(template, dayStart, dayEnd).get(0);
        return toOccurrence(template, occurrenceInstant, occurrenceDate, completion);
    }

    private void validateOccurrenceDate(Task template, String occurrenceDate) {
        LocalDate date;
        try {
            date = LocalDate.parse(occurrenceDate);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'date' must be 'YYYY-MM-DD'");
        }
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();
        if (expandOccurrences(template, dayStart, dayEnd).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid occurrence date for this series");
        }
    }

    private Task getRecurringTemplateOrThrow(String id) {
        Task task = getLiveTaskOrThrow(id);
        if (task.getRecurrence() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task is not recurring");
        }
        return task;
    }

    // Find a task including soft-deleted ones. Only /restore should use this —
    // everything else wants getLiveTaskOrThrow.
    private Task getTaskOrThrow(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    // A soft-deleted task is gone as far as the API is concerned: mutating one
    // used to succeed silently, resurrecting rows nothing could see.
    private Task getLiveTaskOrThrow(String id) {
        Task task = getTaskOrThrow(id);
        if (task.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        return task;
    }

    // Parse an ISO-8601 timestamp (e.g. 2026-06-21T00:00:00Z) into an Instant,
    // returning 400 Bad Request instead of 500 on malformed input.
    private Instant parseInstant(String value, String paramName) {
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "'" + paramName + "' must be an ISO-8601 timestamp, e.g. 2026-06-21T00:00:00Z");
        }
    }
}
