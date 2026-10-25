package com.lifeTracker.life_tracker_api.projects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectItemService {

    // Board order, left to right.
    static final List<String> STAGES = List.of("todo", "doing", "done", "blocked");

    // Positions within a stage are spaced this far apart, so most moves fit between two neighbours.
    static final int POSITION_GAP = 1024;

    private final ProjectItemRepository projectItemRepository;
    private final ProjectService projectService;

    public ProjectItemService(ProjectItemRepository projectItemRepository, ProjectService projectService) {
        this.projectItemRepository = projectItemRepository;
        this.projectService = projectService;
    }

    public List<ProjectItem> list(String projectId) {
        projectService.getLiveProjectOrThrow(projectId);
        return boardOrder(projectItemRepository.findByProjectIdAndDeletedAtIsNull(projectId));
    }

    // Appended to the end of its stage.
    public ProjectItem create(String projectId, ProjectItemCreateRequest request) {
        projectService.getLiveProjectOrThrow(projectId);
        String stage = request.stage() != null ? request.stage() : "todo";
        Instant now = Instant.now();

        ProjectItem item = new ProjectItem();
        item.setId(UUID.randomUUID().toString());
        item.setProjectId(projectId);
        item.setTitle(request.title().trim());
        item.setDescription(nullIfBlank(request.description()));
        if (request.priority() != null) item.setPriority(request.priority());
        item.setDueOn(request.dueOn());
        enterStage(item, stage, now);
        item.setPosition(projectItemRepository.findTopByProjectIdAndStageAndDeletedAtIsNullOrderByPositionDesc(projectId, stage)
                .map(last -> last.getPosition() + POSITION_GAP)
                .orElse(POSITION_GAP));
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return projectItemRepository.save(item);
    }

    public ProjectItem update(String id, ProjectItemPatchRequest request) {
        ProjectItem item = getLiveItemOrThrow(id);

        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
            }
            item.setTitle(request.title().trim());
        }
        if (request.description() != null) item.setDescription(nullIfBlank(request.description()));
        if (request.priority() != null) item.setPriority(request.priority());
        if (request.dueOn() != null) item.setDueOn(request.dueOn());

        item.setUpdatedAt(Instant.now());
        return projectItemRepository.save(item);
    }

    // Its own verb because PATCH reads null as "unchanged".
    public ProjectItem clearDue(String id) {
        ProjectItem item = getLiveItemOrThrow(id);
        item.setDueOn(null);
        item.setUpdatedAt(Instant.now());
        return projectItemRepository.save(item);
    }

    // Places the item at `index` among the target stage's other live items and
    // answers with the whole board, so the client can replace its optimistic
    // guess in one step. Transactional because a renumber touches the whole stage.
    @Transactional
    public List<ProjectItem> move(String id, ProjectItemMoveRequest request) {
        ProjectItem item = getLiveItemOrThrow(id);
        List<ProjectItem> others = new ArrayList<>(projectItemRepository
                .findByProjectIdAndStageAndDeletedAtIsNullOrderByPositionAsc(item.getProjectId(), request.stage()));
        others.removeIf(other -> other.getId().equals(id));
        int index = Math.clamp(request.index(), 0, others.size());
        Instant now = Instant.now();

        enterStage(item, request.stage(), now);
        int before = index > 0 ? others.get(index - 1).getPosition() : 0;
        Integer after = index < others.size() ? others.get(index).getPosition() : null;
        Integer position = positionBetween(before, after);
        if (position != null) {
            item.setPosition(position);
        } else {
            others.add(index, item);
            renumber(others, now);
        }
        item.setUpdatedAt(now);
        projectItemRepository.save(item);
        return boardOrder(projectItemRepository.findByProjectIdAndDeletedAtIsNull(item.getProjectId()));
    }

    public void delete(String id) {
        ProjectItem item = getLiveItemOrThrow(id);
        item.setDeletedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        projectItemRepository.save(item);
    }

    public void restore(String id) {
        ProjectItem item = projectItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        projectService.getLiveProjectOrThrow(item.getProjectId());
        item.setDeletedAt(null);
        item.setUpdatedAt(Instant.now());
        projectItemRepository.save(item);
    }

    // Midpoint of the neighbours. The top of a stage has 0 as its virtual upper
    // neighbour; the bottom appends a full gap. null = no integer gap left.
    private static Integer positionBetween(int before, Integer after) {
        if (after == null) return before + POSITION_GAP;
        if (after - before < 2) return null;
        return before + (after - before) / 2;
    }

    private void renumber(List<ProjectItem> stageInOrder, Instant now) {
        for (int i = 0; i < stageInOrder.size(); i++) {
            ProjectItem item = stageInOrder.get(i);
            item.setPosition((i + 1) * POSITION_GAP);
            item.setUpdatedAt(now);
        }
        projectItemRepository.saveAll(stageInOrder);
    }

    private static void enterStage(ProjectItem item, String stage, Instant now) {
        boolean wasDone = "done".equals(item.getStage());
        boolean isDone = "done".equals(stage);
        if (isDone && !wasDone) item.setCompletedAt(now);
        if (!isDone) item.setCompletedAt(null);
        item.setStage(stage);
    }

    // The stage order isn't alphabetical, so the board is sorted here rather than in SQL.
    private static List<ProjectItem> boardOrder(List<ProjectItem> items) {
        return items.stream()
                .sorted(Comparator.comparingInt((ProjectItem item) -> STAGES.indexOf(item.getStage()))
                        .thenComparing(ProjectItem::getPosition)
                        .thenComparing(ProjectItem::getCreatedAt))
                .toList();
    }

    // Not trimmed: leading indentation and line breaks in free-form text are deliberate.
    private static String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private ProjectItem getLiveItemOrThrow(String id) {
        ProjectItem item = projectItemRepository.findById(id)
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        projectService.getLiveProjectOrThrow(item.getProjectId());
        return item;
    }
}
