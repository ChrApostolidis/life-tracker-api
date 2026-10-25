package com.lifeTracker.life_tracker_api.projects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectItemRepository projectItemRepository;
    private final ProjectUpdateRepository projectUpdateRepository;
    private final ZoneId zone;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectItemRepository projectItemRepository,
            ProjectUpdateRepository projectUpdateRepository,
            @Value("${app.timezone:Europe/Athens}") String timezoneId
    ) {
        this.projectRepository = projectRepository;
        this.projectItemRepository = projectItemRepository;
        this.projectUpdateRepository = projectUpdateRepository;
        this.zone = ZoneId.of(timezoneId);
    }

    // One query each for items and updates across every project, so the pool
    // never fetches boards one by one.
    public List<ProjectSummaryResponse> list() {
        List<Project> projects = projectRepository.findByDeletedAtIsNull();
        List<String> ids = projects.stream().map(Project::getId).toList();
        Map<String, List<ProjectItem>> itemsByProject = projectItemRepository.findByProjectIdInAndDeletedAtIsNull(ids)
                .stream().collect(Collectors.groupingBy(ProjectItem::getProjectId));
        Map<String, List<ProjectUpdate>> updatesByProject = projectUpdateRepository.findByProjectIdInAndDeletedAtIsNull(ids)
                .stream().collect(Collectors.groupingBy(ProjectUpdate::getProjectId));
        return projects.stream()
                .map(p -> ProjectSummaryResponse.of(p,
                        itemsByProject.getOrDefault(p.getId(), List.of()),
                        updatesByProject.getOrDefault(p.getId(), List.of())))
                .sorted(Comparator.comparing(ProjectSummaryResponse::lastActivityAt).reversed())
                .toList();
    }

    public Project get(String id) {
        return getLiveProjectOrThrow(id);
    }

    public Project create(ProjectCreateRequest request) {
        Project project = new Project();
        project.setId(UUID.randomUUID().toString());
        project.setName(request.name().trim());
        project.setPitch(blankToNull(request.pitch()));
        project.setDescription(textOrNull(request.description()));
        project.setTags(blankToNull(request.tags()));
        // A project created straight into ongoing/finished gets its date too.
        applyStatus(project, request.status());
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        return projectRepository.save(project);
    }

    public Project update(String id, ProjectPatchRequest request) {
        Project project = getLiveProjectOrThrow(id);

        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
            }
            project.setName(request.name().trim());
        }
        if (request.pitch() != null) project.setPitch(blankToNull(request.pitch()));
        if (request.description() != null) project.setDescription(textOrNull(request.description()));
        if (request.status() != null) applyStatus(project, request.status());
        if (request.repoUrl() != null) project.setRepoUrl(validUrl(request.repoUrl(), "repoUrl"));
        if (request.liveUrl() != null) project.setLiveUrl(validUrl(request.liveUrl(), "liveUrl"));
        if (request.techStack() != null) project.setTechStack(blankToNull(request.techStack()));
        if (request.tags() != null) project.setTags(blankToNull(request.tags()));
        // After applyStatus, so an explicit date in the same request wins over the auto-stamp.
        if (request.startedOn() != null) project.setStartedOn(request.startedOn());
        if (request.finishedOn() != null) project.setFinishedOn(request.finishedOn());

        project.setUpdatedAt(Instant.now());
        return projectRepository.save(project);
    }

    // Items and updates are left alone: they're unreachable through a deleted
    // project and come back with it on restore.
    public void delete(String id) {
        Project project = getLiveProjectOrThrow(id);
        project.setDeletedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
    }

    public void restore(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        project.setDeletedAt(null);
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
    }

    // The gate every item and update endpoint goes through: a missing or
    // soft-deleted project hides everything under it.
    Project getLiveProjectOrThrow(String id) {
        return projectRepository.findById(id)
                .filter(project -> project.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    // Auto-stamp only when the field is still empty — the Book rule.
    private void applyStatus(Project project, String status) {
        project.setStatus(status);
        if ("ongoing".equals(status) && project.getStartedOn() == null) {
            project.setStartedOn(today());
        }
        if ("finished".equals(status) && project.getFinishedOn() == null) {
            project.setFinishedOn(today());
        }
    }

    // http(s) only, so a stored javascript: URL can never reach an href. Blank clears.
    private String validUrl(String value, String field) {
        String url = blankToNull(value);
        if (url == null) return null;
        String lower = url.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must start with http:// or https://");
        }
        return url;
    }

    // Not trimmed: leading indentation and line breaks in free-form text are deliberate.
    private static String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String today() {
        return LocalDate.now(zone).toString();
    }
}
