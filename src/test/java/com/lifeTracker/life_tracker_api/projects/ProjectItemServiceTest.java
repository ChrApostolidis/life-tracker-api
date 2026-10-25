package com.lifeTracker.life_tracker_api.projects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create projects and items against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class ProjectItemServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectItemService projectItemService;

    @Autowired
    private ProjectItemRepository projectItemRepository;

    private String newProject() {
        return projectService.create(new ProjectCreateRequest("Board", "ongoing", null, null)).getId();
    }

    private ProjectItem add(String projectId, String title, String stage) {
        return projectItemService.create(projectId, new ProjectItemCreateRequest(title, null, stage, null, null));
    }

    private List<ProjectItem> move(ProjectItem item, String stage, int index) {
        return projectItemService.move(item.getId(), new ProjectItemMoveRequest(stage, index));
    }

    // Titles of one stage, in board order.
    private static List<String> titlesIn(List<ProjectItem> board, String stage) {
        return board.stream().filter(i -> i.getStage().equals(stage)).map(ProjectItem::getTitle).toList();
    }

    private static void assertStatus(HttpStatus expected, Runnable call) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, call::run);
        assertEquals(expected, e.getStatusCode());
    }

    @Test
    void createDefaultsToTodoAndNormal() {
        ProjectItem item = add(newProject(), "A", null);
        assertEquals("todo", item.getStage());
        assertEquals("normal", item.getPriority());
        assertNull(item.getCompletedAt());
    }

    @Test
    void createAppendsAGapAfterTheLastItemOfItsStage() {
        String projectId = newProject();
        assertEquals(1024, add(projectId, "A", "todo").getPosition());
        assertEquals(2048, add(projectId, "B", "todo").getPosition());
        assertEquals(1024, add(projectId, "C", "doing").getPosition());
    }

    @Test
    void createStraightIntoDoneStampsCompletedAt() {
        assertNotNull(add(newProject(), "A", "done").getCompletedAt());
    }

    @Test
    void moveWithinAStageTakesTheMidpoint() {
        String projectId = newProject();
        add(projectId, "A", "todo");
        add(projectId, "B", "todo");
        ProjectItem c = add(projectId, "C", "todo");

        List<ProjectItem> board = move(c, "todo", 1);
        assertEquals(List.of("A", "C", "B"), titlesIn(board, "todo"));
        assertEquals(1536, projectItemRepository.findById(c.getId()).orElseThrow().getPosition());

        board = move(c, "todo", 0);
        assertEquals(List.of("C", "A", "B"), titlesIn(board, "todo"));
        assertEquals(512, projectItemRepository.findById(c.getId()).orElseThrow().getPosition());
    }

    @Test
    void moveClampsTheIndex() {
        String projectId = newProject();
        ProjectItem a = add(projectId, "A", "todo");
        add(projectId, "B", "todo");
        assertEquals(List.of("B", "A"), titlesIn(move(a, "todo", 99), "todo"));
        assertEquals(List.of("A", "B"), titlesIn(move(a, "todo", -5), "todo"));
    }

    @Test
    void moveAcrossStagesStampsAndClearsCompletedAt() {
        String projectId = newProject();
        ProjectItem item = add(projectId, "A", "todo");
        add(projectId, "B", "done");

        List<ProjectItem> board = move(item, "done", 0);
        assertEquals(List.of("A", "B"), titlesIn(board, "done"));
        assertNotNull(item.getCompletedAt());

        move(item, "blocked", 0);
        assertEquals("blocked", item.getStage());
        assertNull(item.getCompletedAt());
    }

    @Test
    void moveReturnsTheWholeBoardInStageOrder() {
        String projectId = newProject();
        add(projectId, "blocked", "blocked");
        add(projectId, "done", "done");
        add(projectId, "doing", "doing");
        ProjectItem todo = add(projectId, "todo", "todo");

        List<String> stages = move(todo, "todo", 0).stream().map(ProjectItem::getStage).toList();
        assertEquals(List.of("todo", "doing", "done", "blocked"), stages);
    }

    @Test
    void moveRenumbersTheStageWhenNoGapIsLeft() {
        String projectId = newProject();
        ProjectItem a = add(projectId, "A", "todo");
        ProjectItem b = add(projectId, "B", "todo");
        ProjectItem c = add(projectId, "C", "doing");
        b.setPosition(a.getPosition() + 1);
        projectItemRepository.save(b);

        List<ProjectItem> board = move(c, "todo", 1);
        assertEquals(List.of("A", "C", "B"), titlesIn(board, "todo"));
        List<Integer> positions = board.stream().filter(i -> i.getStage().equals("todo")).map(ProjectItem::getPosition).toList();
        assertEquals(List.of(1024, 2048, 3072), positions);
    }

    @Test
    void blankDescriptionClearsAndClearDueDropsTheDate() {
        String projectId = newProject();
        ProjectItem item = projectItemService.create(projectId,
                new ProjectItemCreateRequest("A", "Some notes", null, "urgent", "2026-11-01"));

        ProjectItem untouched = projectItemService.update(item.getId(), new ProjectItemPatchRequest(null, null, null, null));
        assertEquals("Some notes", untouched.getDescription());
        assertNull(projectItemService.update(item.getId(), new ProjectItemPatchRequest(null, " ", null, null)).getDescription());
        assertNull(projectItemService.clearDue(item.getId()).getDueOn());
    }

    @Test
    void deletedItemsDropOutOfTheBoard() {
        String projectId = newProject();
        ProjectItem a = add(projectId, "A", "todo");
        add(projectId, "B", "todo");
        projectItemService.delete(a.getId());
        assertEquals(List.of("B"), titlesIn(projectItemService.list(projectId), "todo"));

        projectItemService.restore(a.getId());
        assertEquals(List.of("A", "B"), titlesIn(projectItemService.list(projectId), "todo"));
    }

    @Test
    void itemsUnderADeletedOrMissingProjectAre404() {
        String projectId = newProject();
        ProjectItem item = add(projectId, "A", "todo");
        projectService.delete(projectId);

        assertStatus(HttpStatus.NOT_FOUND, () -> projectItemService.list(projectId));
        assertStatus(HttpStatus.NOT_FOUND, () -> add(projectId, "B", null));
        assertStatus(HttpStatus.NOT_FOUND, () -> move(item, "done", 0));
        assertStatus(HttpStatus.NOT_FOUND, () -> projectItemService.clearDue(item.getId()));
        assertStatus(HttpStatus.NOT_FOUND, () -> add("no-such-project", "C", null));
    }
}
