package com.lifeTracker.life_tracker_api.sleepLogs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create sleep logs against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class SleepLogServiceTest {

    @Autowired
    private SleepLogService sleepLogService;

    private static SleepLogSaveRequest night(String bedAt, String wokeAt) {
        return new SleepLogSaveRequest(Instant.parse(bedAt), Instant.parse(wokeAt));
    }

    private List<String> datesIn(String from, String to) {
        return sleepLogService.list(from, to).stream().map(SleepLog::getSleepDate).toList();
    }

    @Test
    void saveOnFreshDateCreatesTheRow() {
        SleepLog log = sleepLogService.save("2026-03-02", night("2026-03-01T21:40:00Z", "2026-03-02T05:00:00Z"));
        assertNotNull(log.getId());
        assertNotNull(log.getCreatedAt());
        assertEquals(Instant.parse("2026-03-02T05:00:00Z"), log.getWokeAt());
    }

    @Test
    void saveTwiceOnSameDateUpdatesRatherThanDuplicating() {
        SleepLog first = sleepLogService.save("2026-03-03", night("2026-03-02T22:00:00Z", "2026-03-03T05:00:00Z"));
        SleepLog second = sleepLogService.save("2026-03-03", night("2026-03-02T23:00:00Z", "2026-03-03T06:00:00Z"));
        assertEquals(first.getId(), second.getId());
        assertEquals(Instant.parse("2026-03-02T23:00:00Z"), second.getBedAt());
        assertEquals(1, datesIn("2026-03-01", "2026-04-01").stream().filter("2026-03-03"::equals).count());
    }

    @Test
    void saveAfterDeleteRevivesTheSameRow() {
        SleepLog original = sleepLogService.save("2026-03-04", night("2026-03-03T22:00:00Z", "2026-03-04T05:00:00Z"));
        sleepLogService.delete("2026-03-04");
        assertFalse(datesIn("2026-03-01", "2026-04-01").contains("2026-03-04"));

        // Must revive the existing row, not insert one that hits the unique index.
        SleepLog revived = sleepLogService.save("2026-03-04", night("2026-03-03T21:00:00Z", "2026-03-04T05:00:00Z"));
        assertEquals(original.getId(), revived.getId());
        assertTrue(datesIn("2026-03-01", "2026-04-01").contains("2026-03-04"));
    }

    @Test
    void restoreBringsBackADeletedNight() {
        sleepLogService.save("2026-03-05", night("2026-03-04T22:00:00Z", "2026-03-05T05:00:00Z"));
        sleepLogService.delete("2026-03-05");
        sleepLogService.restore("2026-03-05");
        assertTrue(datesIn("2026-03-01", "2026-04-01").contains("2026-03-05"));
    }

    @Test
    void deleteUnknownDateIs404() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> sleepLogService.delete("2026-03-06"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void wakingBeforeBedIs400() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> sleepLogService.save("2026-03-07", night("2026-03-07T05:00:00Z", "2026-03-06T22:00:00Z")));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void nightLongerThan24HoursIs400() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> sleepLogService.save("2026-03-08", night("2026-03-06T22:00:00Z", "2026-03-08T05:00:00Z")));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void malformedDateIs400() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> sleepLogService.save("March 9", night("2026-03-08T22:00:00Z", "2026-03-09T05:00:00Z")));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void listRespectsHalfOpenRange() {
        sleepLogService.save("2026-04-01", night("2026-03-31T21:00:00Z", "2026-04-01T04:00:00Z"));
        sleepLogService.save("2026-05-01", night("2026-04-30T21:00:00Z", "2026-05-01T04:00:00Z"));
        List<String> dates = datesIn("2026-04-01", "2026-05-01");
        assertTrue(dates.contains("2026-04-01"));
        assertFalse(dates.contains("2026-05-01"));
    }
}
