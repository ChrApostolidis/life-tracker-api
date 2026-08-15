package com.lifeTracker.life_tracker_api.watchItems;

// Returned by POST /api/watch-items/{id}/episodes/{season}/{episode} so the
// frontend sees the item's current state in the same round trip — watching
// the final episode can auto-advance status to 'watched' server-side, and
// without this the card would show stale status until a manual refresh.
public record EpisodeWatchResponse(
        EpisodeWatch episodeWatch,
        WatchItem watchItem
) {}
