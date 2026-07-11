package com.lifeTracker.life_tracker_api.money;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://lifetracker.christosapostolidis.com"})
public class MoneyEntryController {

    private final MoneyEntryService moneyEntryService;

    public MoneyEntryController(MoneyEntryService moneyEntryService) {
        this.moneyEntryService = moneyEntryService;
    }

    @GetMapping("/money")
    public ResponseEntity<List<MoneyEntry>> listEntries(@RequestParam String from, @RequestParam String to) {
        return ResponseEntity.ok(moneyEntryService.listRange(from, to));
    }

    @PostMapping("/money")
    public ResponseEntity<MoneyEntry> createEntry(@Valid @RequestBody MoneyEntryCreateRequest request) {
        return ResponseEntity.ok(moneyEntryService.create(request));
    }

    @PatchMapping("/money/{id}")
    public ResponseEntity<MoneyEntry> updateEntry(@PathVariable String id, @Valid @RequestBody MoneyEntryUpdateRequest request) {
        return ResponseEntity.ok(moneyEntryService.update(id, request));
    }

    @DeleteMapping("/money/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String id) {
        moneyEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/money/{id}/restore")
    public ResponseEntity<Void> restoreEntry(@PathVariable String id) {
        moneyEntryService.restore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/money/balance")
    public ResponseEntity<MoneyBalanceResponse> balance() {
        return ResponseEntity.ok(moneyEntryService.balance());
    }
}
