package com.lifeTracker.life_tracker_api.money;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MoneyEntryService {

    private static final Set<String> EXPENSE_CATEGORIES = Set.of("food", "transport", "fun", "bills", "other");
    private static final Set<String> INCOME_CATEGORIES = Set.of("salary", "gift", "other");

    private final MoneyEntryRepository moneyEntryRepository;

    public MoneyEntryService(MoneyEntryRepository moneyEntryRepository) {
        this.moneyEntryRepository = moneyEntryRepository;
    }

    // Half-open [from, to), local 'YYYY-MM-DD' dates.
    public List<MoneyEntry> listRange(String from, String to) {
        validateDate(from);
        validateDate(to);
        return moneyEntryRepository.findEntriesInRange(from, to);
    }

    public MoneyEntry create(MoneyEntryCreateRequest request) {
        validateDate(request.occurredOn());
        validateCategory(request.type(), request.category());

        MoneyEntry entry = new MoneyEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setType(request.type());
        entry.setAmountCents(request.amountCents());
        entry.setLabel(request.label());
        entry.setCategory(request.category());
        entry.setOccurredOn(request.occurredOn());
        entry.setCreatedAt(Instant.now());
        entry.setUpdatedAt(Instant.now());
        return moneyEntryRepository.save(entry);
    }

    public MoneyEntry update(String id, MoneyEntryUpdateRequest request) {
        MoneyEntry entry = getEntryOrThrow(id);
        if (entry.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Money entry not found");
        }
        if (request.amountCents() != null) entry.setAmountCents(request.amountCents());
        if (request.label() != null) {
            if (request.label().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "label must not be blank");
            }
            entry.setLabel(request.label());
        }
        if (request.category() != null) {
            validateCategory(entry.getType(), request.category());
            entry.setCategory(request.category());
        }
        if (request.occurredOn() != null) {
            validateDate(request.occurredOn());
            entry.setOccurredOn(request.occurredOn());
        }
        entry.setUpdatedAt(Instant.now());
        return moneyEntryRepository.save(entry);
    }

    public void delete(String id) {
        MoneyEntry entry = getEntryOrThrow(id);
        entry.setDeletedAt(Instant.now());
        entry.setUpdatedAt(Instant.now());
        moneyEntryRepository.save(entry);
    }

    public void restore(String id) {
        MoneyEntry entry = getEntryOrThrow(id);
        entry.setDeletedAt(null);
        entry.setUpdatedAt(Instant.now());
        moneyEntryRepository.save(entry);
    }

    // All-time sums — the piggy bank balance is earned minus spent.
    public MoneyBalanceResponse balance() {
        return new MoneyBalanceResponse(
                moneyEntryRepository.sumAmountCentsByType("income"),
                moneyEntryRepository.sumAmountCentsByType("expense")
        );
    }

    private MoneyEntry getEntryOrThrow(String id) {
        return moneyEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Money entry not found"));
    }

    // The DTO regex only checks shape; this rejects impossible dates like 2026-13-40.
    private void validateDate(String date) {
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid date: " + date);
        }
    }

    private void validateCategory(String type, String category) {
        if (category == null) return;
        Set<String> allowed = "income".equals(type) ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
        if (!allowed.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "invalid category for " + type + ": " + category);
        }
    }
}
