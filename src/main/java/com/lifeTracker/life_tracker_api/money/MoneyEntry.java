package com.lifeTracker.life_tracker_api.money;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "money_entries")
public class MoneyEntry {

    @Id
    private String id;

    // 'expense' or 'income' — amountCents is always positive, type carries the sign.
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer amountCents;

    @Column(nullable = false)
    private String label;

    private String category;

    // Local calendar day 'YYYY-MM-DD' — see V3 migration for why this is not an instant.
    @Column(nullable = false)
    private String occurredOn;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getAmountCents() { return amountCents; }
    public void setAmountCents(Integer amountCents) { this.amountCents = amountCents; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOccurredOn() { return occurredOn; }
    public void setOccurredOn(String occurredOn) { this.occurredOn = occurredOn; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
