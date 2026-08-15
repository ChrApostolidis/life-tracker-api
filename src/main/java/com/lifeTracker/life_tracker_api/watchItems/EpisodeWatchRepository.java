package com.lifeTracker.life_tracker_api.watchItems;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeWatchRepository extends JpaRepository<EpisodeWatch, String> {

    List<EpisodeWatch> findByWatchItemId(String watchItemId);

    Optional<EpisodeWatch> findByWatchItemIdAndSeasonNumberAndEpisodeNumber(
            String watchItemId, Integer seasonNumber, Integer episodeNumber);

    long countByWatchItemId(String watchItemId);
}
