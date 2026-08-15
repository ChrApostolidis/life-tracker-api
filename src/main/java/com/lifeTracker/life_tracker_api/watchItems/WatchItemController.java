package com.lifeTracker.life_tracker_api.watchItems;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://lifetracker.christosapostolidis.com"})
public class WatchItemController {

    private final WatchItemService watchItemService;

    public WatchItemController(WatchItemService watchItemService) {
        this.watchItemService = watchItemService;
    }

    @GetMapping("/watch-items")
    public ResponseEntity<List<WatchItem>> listWatchItems() {
        return ResponseEntity.ok(watchItemService.list());
    }

    @PostMapping("/watch-items")
    public ResponseEntity<WatchItem> createWatchItem(@Valid @RequestBody WatchItemCreateRequest request) {
        return ResponseEntity.ok(watchItemService.create(request));
    }

    @PatchMapping("/watch-items/{id}")
    public ResponseEntity<WatchItem> updateWatchItem(@PathVariable String id, @Valid @RequestBody WatchItemUpdateRequest request) {
        return ResponseEntity.ok(watchItemService.update(id, request));
    }

    @DeleteMapping("/watch-items/{id}")
    public ResponseEntity<Void> deleteWatchItem(@PathVariable String id) {
        watchItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/watch-items/{id}/restore")
    public ResponseEntity<Void> restoreWatchItem(@PathVariable String id) {
        watchItemService.restore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/episode-watches")
    public ResponseEntity<List<EpisodeWatch>> listEpisodeWatches() {
        return ResponseEntity.ok(watchItemService.listAllEpisodeWatches());
    }

    @PostMapping("/watch-items/{id}/episodes/{season}/{episode}")
    public ResponseEntity<EpisodeWatchResponse> watchEpisode(
            @PathVariable String id,
            @PathVariable Integer season,
            @PathVariable Integer episode
    ) {
        return ResponseEntity.ok(watchItemService.watchEpisode(id, season, episode));
    }

    @DeleteMapping("/watch-items/{id}/episodes/{season}/{episode}")
    public ResponseEntity<Void> unwatchEpisode(
            @PathVariable String id,
            @PathVariable Integer season,
            @PathVariable Integer episode
    ) {
        watchItemService.unwatchEpisode(id, season, episode);
        return ResponseEntity.noContent().build();
    }
}
