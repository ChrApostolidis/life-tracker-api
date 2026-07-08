package com.lifeTracker.life_tracker_api.money;

// All-time sums for the piggy bank: balance = earnedCents - spentCents.
public record MoneyBalanceResponse(long earnedCents, long spentCents) {}
