package com.lifeTracker.life_tracker_api.watchItems;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create watch items/episode watches against the shared test DB
// without cleaning up.
@SpringBootTest
@Transactional
class WatchItemServiceTest {

    @Autowired
    private WatchItemService watchItemService;

    private WatchItem createMovie(int tmdbId, String status) {
        return watchItemService.create(new WatchItemCreateRequest(
                tmdbId, "movie", "Dune", "2021", null, "Science Fiction, Adventure", status, null, null));
    }

    private WatchItem createSeries(int tmdbId, String status, int totalEpisodes) {
        return watchItemService.create(new WatchItemCreateRequest(
                tmdbId, "series", "Dune: Prophecy", "2024", null, "Sci-Fi & Fantasy", status, 1, totalEpisodes));
    }

    @Test
    void createSetsDefaults() {
        WatchItem item = createMovie(1001, "watchlist");
        assertNotNull(item.getId());
        assertNotNull(item.getCreatedAt());
        assertNull(item.getDeletedAt());
        assertNull(item.getStartedOn());
        assertNull(item.getFinishedOn());
    }

    @Test
    void movingToWatchingStampsStartedOnOnlyWhenEmpty() {
        WatchItem item = createSeries(1002, "watchlist", 10);
        WatchItem updated = watchItemService.update(item.getId(), new WatchItemUpdateRequest(
                null, "watching", null, null, null, null, null, null));
        assertNotNull(updated.getStartedOn());

        String firstStartedOn = updated.getStartedOn();
        WatchItem updatedAgain = watchItemService.update(item.getId(), new WatchItemUpdateRequest(
                null, "watching", null, null, null, null, null, null));
        assertEquals(firstStartedOn, updatedAgain.getStartedOn());
    }

    @Test
    void movingToWatchedStampsFinishedOn() {
        WatchItem item = createMovie(1003, "watchlist");
        WatchItem updated = watchItemService.update(item.getId(), new WatchItemUpdateRequest(
                null, "watched", null, null, null, null, null, null));
        assertNotNull(updated.getFinishedOn());
    }

    @Test
    void creatingAMovieAsWatchedStampsFinishedOnImmediately() {
        WatchItem item = createMovie(1004, "watched");
        assertNotNull(item.getFinishedOn());
    }

    @Test
    void watchingIsRejectedForAMovie() {
        assertThrows(ResponseStatusException.class, () -> createMovie(1005, "watching"));

        WatchItem item = createMovie(1006, "watchlist");
        assertThrows(ResponseStatusException.class, () -> watchItemService.update(
                item.getId(), new WatchItemUpdateRequest(null, "watching", null, null, null, null, null, null)));
    }

    @Test
    void watchingSameEpisodeTwiceIsIdempotent() {
        WatchItem series = createSeries(2001, "watching", 10);
        EpisodeWatchResponse first = watchItemService.watchEpisode(series.getId(), 1, 1);
        EpisodeWatchResponse second = watchItemService.watchEpisode(series.getId(), 1, 1);
        assertEquals(first.episodeWatch().getId(), second.episodeWatch().getId());
    }

    @Test
    void unwatchThenRewatchSameEpisodeSucceeds() {
        WatchItem series = createSeries(2002, "watching", 10);
        watchItemService.watchEpisode(series.getId(), 1, 1);
        watchItemService.unwatchEpisode(series.getId(), 1, 1);

        // Regression test: this must revive/re-insert cleanly, not collide
        // with the unique index on (watchItemId, seasonNumber, episodeNumber).
        EpisodeWatchResponse rewatched = watchItemService.watchEpisode(series.getId(), 1, 1);
        assertNotNull(rewatched.episodeWatch().getId());
    }

    @Test
    void watchingFinalEpisodeAdvancesStatusToWatchedAndUnwatchingOneDoesNotRevert() {
        WatchItem series = createSeries(2003, "watching", 2);
        watchItemService.watchEpisode(series.getId(), 1, 1);
        EpisodeWatchResponse response = watchItemService.watchEpisode(series.getId(), 1, 2);

        assertEquals("watched", response.watchItem().getStatus());
        assertNotNull(response.watchItem().getFinishedOn());

        watchItemService.unwatchEpisode(series.getId(), 1, 2);
        WatchItem afterUnwatch = watchItemService.list().stream()
                .filter(i -> i.getId().equals(series.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("watched", afterUnwatch.getStatus());
    }

    @Test
    void reAddingSoftDeletedItemRevivesTheSameRow() {
        WatchItem item = createMovie(1005, "watchlist");
        watchItemService.delete(item.getId());

        List<WatchItem> afterDelete = watchItemService.list();
        assertTrue(afterDelete.stream().noneMatch(i -> i.getId().equals(item.getId())));

        WatchItem revived = createMovie(1005, "watchlist");
        assertEquals(item.getId(), revived.getId());

        List<WatchItem> afterRevive = watchItemService.list();
        assertTrue(afterRevive.stream().anyMatch(i -> i.getId().equals(item.getId())));
    }

    @Test
    void updatingDeletedItemThrows() {
        WatchItem item = createMovie(1006, "watchlist");
        watchItemService.delete(item.getId());
        assertThrows(ResponseStatusException.class, () -> watchItemService.update(
                item.getId(), new WatchItemUpdateRequest("New Title", null, null, null, null, null, null, null)));
    }
}
