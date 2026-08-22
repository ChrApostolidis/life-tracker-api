# Life Tracker API

The REST backend for [Life Tracker](https://github.com/ChrApostolidis/life-tracker), a single-user, capture-first life-tracking app (tasks, notes, money, books, habits, plus a small RPG layer on top). This repo is the persistence and business-logic layer; there's no UI here.

---

## Contents

- [Repositories](#repositories)
- [Why this exists](#why-this-exists)
- [Stack](#stack)
- [Data model](#data-model)
- [API contract (summary)](#api-contract-summary)

---

## Repositories

Life Tracker is split across two repos. This is the backend; the UI lives in the other one.

| Repo | What it is |
|---|---|
| [life-tracker](https://github.com/ChrApostolidis/life-tracker) | Next.js frontend, every screen and the RPG engine |
| [life-tracker-api](https://github.com/ChrApostolidis/life-tracker-api) | This repo. Spring Boot + SQLite, the REST API and all persistence |

---

## Why this exists

The frontend needed somewhere to actually keep data that wasn't `localStorage`: something that survives redeploys, can be backed up as a single file, and doesn't need a database server running in the background for a single-user app. SQLite plus a small Spring Boot API fit that: one `.db` file, zero ops, easy to back up, easy to reason about.

The API contract deliberately mirrors the frontend's data model field-for-field (camelCase JSON, same nullability) so the two repos never drift into "the backend calls it one thing and the frontend calls it another." Every feature follows the same package-by-feature shape (see below), so adding a new domain is close to mechanical.

---

## Stack

- Java 21 · [Spring Boot 4.1](https://spring.io/projects/spring-boot) (`webmvc`, Spring Data JPA, Bean Validation)
- [SQLite](https://www.sqlite.org/) via the `xerial` JDBC driver + `hibernate-community-dialects`
- [Flyway](https://flywaydb.org/) owns the schema; every change is a versioned migration, never an entity-first change
- Maven (wrapper included, no local Maven install needed)

---

## Data model

Seven feature domains, each its own package (`tasks/`, `notes/`, `money/`, `books/`, `habits/`, `dayNotes/`, `watchItems/`) with the same internal shape: `Entity`, `CreateRequest`/`UpdateRequest` DTOs, `Repository`, `Service` (owns business rules + soft delete), thin `Controller`.

| Domain | What it stores |
|---|---|
| **Tasks** | Scheduled or inbox items; recurring tasks (daily/weekly/monthly) expand into occurrences entirely server-side |
| **Notes** | A standalone note stream; also backs per-task and per-book note threads via nullable foreign keys on one shared table |
| **Money** | Expense/income entries, integer cents, local-date-keyed so late-night entries don't shift across UTC midnight |
| **Books** | A personal library with a wishlist → owned → reading → finished pipeline; auto-stamps start/finish dates on status change |
| **Habits** | Binary daily checks. One of two deliberate exceptions to soft-delete-everywhere: unchecking a day **hard-deletes** that check row (a check is a toggle, not a document; see `HabitService` for why) |
| **Day journal** | One free-form entry per calendar day with an optional 1 to 5 star rating, addressed by date rather than id |
| **Watchlist** | Movies and series in one table, with per-episode tracking for series; finishing the last episode auto-completes the show |

Soft delete (`deletedAt` + a `/restore` endpoint) is the rule everywhere except habit checks and episode watches. Both are toggles, and a tombstoned row would collide with their unique index on re-toggle. Full field-by-field contract, including the recurrence-expansion algorithm, is documented in `CLAUDE.md`.

---

## API contract (summary)

Base `http://localhost:8080`, JSON camelCase, `PATCH` semantics are `null = leave unchanged` everywhere (never used to clear a field). Full endpoint table, request/response shapes, and validation rules live in `CLAUDE.md`. This is just the shape:

```
/api/tasks            GET, POST, PATCH/{id}, DELETE/{id}, /{id}/restore, /{id}/complete, /{id}/uncomplete
/api/tasks/overdue     GET
/api/inbox             GET
/api/notes             GET (+ ?bookId=), POST, PATCH/{id}, DELETE/{id}, /{id}/restore, /{id}/promote
/api/money             GET ?from&to, POST, PATCH/{id}, DELETE/{id}, /{id}/restore
/api/money/balance     GET
/api/books             GET, POST, PATCH/{id}, DELETE/{id}, /{id}/restore
/api/habits            GET, POST, PATCH/{id}, /{id}/archive, /{id}/unarchive
/api/habit-checks      GET ?from&to
/api/habits/{id}/checks/{date}   POST (check), DELETE (uncheck)
/api/day-notes         GET ?from&to, GET/{date}, POST/{date}, DELETE/{date}, /{date}/restore
/api/watch-items       GET, POST, PATCH/{id}, DELETE/{id}, /{id}/restore
/api/episode-watches   GET
/api/watch-items/{id}/episodes/{season}/{episode}   POST (watch), DELETE (unwatch)
```
