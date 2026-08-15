package com.lifeTracker.life_tracker_api.watchItems;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchItemRepository extends JpaRepository<WatchItem, String> {

    List<WatchItem> findByDeletedAtIsNullOrderByCreatedAtDesc();

    // No deletedAt filter on purpose — backs the revive-on-re-add path in
    // WatchItemService, which needs to find a soft-deleted row for the same
    // (tmdbId, mediaType) pair rather than colliding with the unique index.
    Optional<WatchItem> findByTmdbIdAndMediaType(Integer tmdbId, String mediaType);
}
