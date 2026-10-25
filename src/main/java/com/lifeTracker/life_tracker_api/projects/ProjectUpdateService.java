package com.lifeTracker.life_tracker_api.projects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectUpdateService {

    private final ProjectUpdateRepository projectUpdateRepository;
    private final ProjectService projectService;
    private final ZoneId zone;

    public ProjectUpdateService(
            ProjectUpdateRepository projectUpdateRepository,
            ProjectService projectService,
            @Value("${app.timezone:Europe/Athens}") String timezoneId
    ) {
        this.projectUpdateRepository = projectUpdateRepository;
        this.projectService = projectService;
        this.zone = ZoneId.of(timezoneId);
    }

    public List<ProjectUpdate> list(String projectId) {
        projectService.getLiveProjectOrThrow(projectId);
        return projectUpdateRepository.findByProjectIdAndDeletedAtIsNullOrderByEntryDateDescCreatedAtDesc(projectId);
    }

    public ProjectUpdate create(String projectId, ProjectUpdateCreateRequest request) {
        projectService.getLiveProjectOrThrow(projectId);
        ProjectUpdate update = new ProjectUpdate();
        update.setId(UUID.randomUUID().toString());
        update.setProjectId(projectId);
        update.setBody(requireBody(request.body()));
        // No entryDate means "today", decided by the server's clock and app.timezone.
        update.setEntryDate(request.entryDate() != null ? request.entryDate() : LocalDate.now(zone).toString());
        // Capture facts, create-only — same rule as JournalEntry.
        if (request.source() != null) update.setSource(request.source());
        update.setRawTranscript(request.rawTranscript() == null || request.rawTranscript().isBlank() ? null : request.rawTranscript());
        update.setCreatedAt(Instant.now());
        update.setUpdatedAt(Instant.now());
        return projectUpdateRepository.save(update);
    }

    public ProjectUpdate update(String id, ProjectUpdatePatchRequest request) {
        ProjectUpdate update = getLiveUpdateOrThrow(id);
        if (request.body() != null) update.setBody(requireBody(request.body()));
        if (request.entryDate() != null) update.setEntryDate(request.entryDate());
        update.setUpdatedAt(Instant.now());
        return projectUpdateRepository.save(update);
    }

    public void delete(String id) {
        ProjectUpdate update = getLiveUpdateOrThrow(id);
        update.setDeletedAt(Instant.now());
        update.setUpdatedAt(Instant.now());
        projectUpdateRepository.save(update);
    }

    public void restore(String id) {
        ProjectUpdate update = projectUpdateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Update not found"));
        projectService.getLiveProjectOrThrow(update.getProjectId());
        update.setDeletedAt(null);
        update.setUpdatedAt(Instant.now());
        projectUpdateRepository.save(update);
    }

    // A blank body is a 400, not a delete: an update has its own id and DELETE.
    private String requireBody(String body) {
        if (body == null || body.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body must not be blank");
        }
        return body;
    }

    private ProjectUpdate getLiveUpdateOrThrow(String id) {
        ProjectUpdate update = projectUpdateRepository.findById(id)
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Update not found"));
        projectService.getLiveProjectOrThrow(update.getProjectId());
        return update;
    }
}
