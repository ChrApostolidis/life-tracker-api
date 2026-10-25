package com.lifeTracker.life_tracker_api.projects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create projects against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectItemService projectItemService;

    @Autowired
    private ProjectUpdateService projectUpdateService;

    @Autowired
    private ProjectItemRepository projectItemRepository;

    @Autowired
    private ProjectUpdateRepository projectUpdateRepository;

    private static final String TODAY = LocalDate.now(ZoneId.of("Europe/Athens")).toString();

    private Project create(String status) {
        return projectService.create(new ProjectCreateRequest("Life Tracker", status, "Track everything", null));
    }

    private static ProjectPatchRequest patch(String status, String repoUrl, String startedOn) {
        return new ProjectPatchRequest(null, null, null, status, repoUrl, null, null, startedOn, null);
    }

    private ProjectItem addItem(String projectId, String stage) {
        return projectItemService.create(projectId, new ProjectItemCreateRequest("Item", null, stage, null, null));
    }

    private ProjectSummaryResponse summaryOf(String projectId) {
        return projectService.list().stream().filter(s -> s.project().getId().equals(projectId)).findFirst().orElseThrow();
    }

    private static void assertStatus(HttpStatus expected, Runnable call) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, call::run);
        assertEquals(expected, e.getStatusCode());
    }

    @Test
    void createAsIdeaStampsNothing() {
        Project project = create("idea");
        assertNull(project.getStartedOn());
        assertNull(project.getFinishedOn());
    }

    @Test
    void createKeepsADictatedDescriptionAsSpokenAndDropsABlankOne() {
        String spoken = "So the idea is\n  a habit heatmap on Home";
        assertEquals(spoken, projectService.create(new ProjectCreateRequest("Heatmap", "idea", null, spoken)).getDescription());
        assertNull(projectService.create(new ProjectCreateRequest("Heatmap", "idea", null, "   ")).getDescription());
    }

    @Test
    void createStraightIntoOngoingOrFinishedStampsTheDate() {
        assertEquals(TODAY, create("ongoing").getStartedOn());
        assertEquals(TODAY, create("finished").getFinishedOn());
    }

    @Test
    void movingToOngoingStampsStartedOnOnlyWhenEmpty() {
        Project project = create("idea");
        projectService.update(project.getId(), patch(null, null, "2020-01-01"));
        Project ongoing = projectService.update(project.getId(), patch("ongoing", null, null));
        assertEquals("2020-01-01", ongoing.getStartedOn());

        Project other = create("idea");
        assertEquals(TODAY, projectService.update(other.getId(), patch("ongoing", null, null)).getStartedOn());
    }

    @Test
    void explicitDateInSamePatchOverridesAutoStamp() {
        Project project = create("idea");
        Project updated = projectService.update(project.getId(), patch("ongoing", null, "2021-05-05"));
        assertEquals("2021-05-05", updated.getStartedOn());
    }

    @Test
    void urlsMustBeHttpOrHttps() {
        Project project = create("idea");
        assertStatus(HttpStatus.BAD_REQUEST, () -> projectService.update(project.getId(), patch(null, "javascript:alert(1)", null)));
        assertStatus(HttpStatus.BAD_REQUEST, () -> projectService.update(project.getId(), patch(null, "github.com/me/repo", null)));
        assertEquals("https://github.com/me/repo",
                projectService.update(project.getId(), patch(null, "https://github.com/me/repo", null)).getRepoUrl());
    }

    @Test
    void blankPatchFieldsClearAndNullLeavesThemAlone() {
        Project project = create("idea");
        projectService.update(project.getId(), new ProjectPatchRequest(
                null, null, "Notes", null, "https://a.dev", "http://b.dev", "Java, React", null, null));

        Project unchanged = projectService.update(project.getId(), patch(null, null, null));
        assertEquals("Track everything", unchanged.getPitch());
        assertEquals("https://a.dev", unchanged.getRepoUrl());

        Project cleared = projectService.update(project.getId(), new ProjectPatchRequest(
                null, "", " ", null, "", "  ", "", null, null));
        assertNull(cleared.getPitch());
        assertNull(cleared.getDescription());
        assertNull(cleared.getRepoUrl());
        assertNull(cleared.getLiveUrl());
        assertNull(cleared.getTechStack());
    }

    @Test
    void blankNameIs400() {
        Project project = create("idea");
        assertStatus(HttpStatus.BAD_REQUEST, () -> projectService.update(project.getId(),
                new ProjectPatchRequest(" ", null, null, null, null, null, null, null, null)));
    }

    @Test
    void deleteHidesFromListAndGetAndRestoreBringsBack() {
        Project project = create("idea");
        projectService.delete(project.getId());
        assertTrue(projectService.list().stream().noneMatch(s -> s.project().getId().equals(project.getId())));
        assertStatus(HttpStatus.NOT_FOUND, () -> projectService.get(project.getId()));

        projectService.restore(project.getId());
        assertEquals(project.getId(), projectService.get(project.getId()).getId());
    }

    @Test
    void summaryCountsEveryStageIncludingZeros() {
        Project project = create("ongoing");
        addItem(project.getId(), null);
        addItem(project.getId(), "todo");
        addItem(project.getId(), "done");
        ProjectItem deleted = addItem(project.getId(), "blocked");
        projectItemService.delete(deleted.getId());

        assertEquals(Map.of("todo", 2, "doing", 0, "done", 1, "blocked", 0), summaryOf(project.getId()).counts());
    }

    @Test
    void lastActivityIsTheLatestOfProjectItemsAndUpdates() {
        Project project = create("ongoing");
        assertEquals(project.getUpdatedAt(), summaryOf(project.getId()).lastActivityAt());

        ProjectItem item = addItem(project.getId(), "todo");
        Instant itemTouched = project.getUpdatedAt().plus(Duration.ofHours(1));
        item.setUpdatedAt(itemTouched);
        projectItemRepository.save(item);
        assertEquals(itemTouched, summaryOf(project.getId()).lastActivityAt());

        ProjectUpdate update = projectUpdateService.create(project.getId(), new ProjectUpdateCreateRequest("Shipped v1", null, null, null));
        Instant written = project.getUpdatedAt().plus(Duration.ofHours(2));
        update.setCreatedAt(written);
        projectUpdateRepository.save(update);
        assertEquals(written, summaryOf(project.getId()).lastActivityAt());
    }

    @Test
    void listIsSortedByLastActivityNewestFirst() {
        Project older = create("idea");
        Project newer = create("idea");
        ProjectItem item = addItem(older.getId(), "todo");
        item.setUpdatedAt(newer.getUpdatedAt().plus(Duration.ofDays(1)));
        projectItemRepository.save(item);

        List<String> ids = projectService.list().stream().map(s -> s.project().getId())
                .filter(id -> id.equals(older.getId()) || id.equals(newer.getId())).toList();
        assertEquals(List.of(older.getId(), newer.getId()), ids);
    }

    @Test
    void updateDefaultsToTodayAndTextSource() {
        Project project = create("ongoing");
        ProjectUpdate update = projectUpdateService.create(project.getId(), new ProjectUpdateCreateRequest("Did a thing", null, null, null));
        assertEquals(TODAY, update.getEntryDate());
        assertEquals("text", update.getSource());
    }

    @Test
    void updatesListNewestEntryDateFirst() {
        Project project = create("ongoing");
        projectUpdateService.create(project.getId(), new ProjectUpdateCreateRequest("old", "2026-01-01", null, null));
        projectUpdateService.create(project.getId(), new ProjectUpdateCreateRequest("new", "2026-02-01", "voice", "new"));
        List<String> bodies = projectUpdateService.list(project.getId()).stream().map(ProjectUpdate::getBody).toList();
        assertEquals(List.of("new", "old"), bodies);
    }

    @Test
    void blankUpdateBodyIs400() {
        Project project = create("ongoing");
        assertStatus(HttpStatus.BAD_REQUEST, () -> projectUpdateService.create(project.getId(),
                new ProjectUpdateCreateRequest("  ", null, null, null)));
        ProjectUpdate update = projectUpdateService.create(project.getId(), new ProjectUpdateCreateRequest("ok", null, null, null));
        assertStatus(HttpStatus.BAD_REQUEST, () -> projectUpdateService.update(update.getId(),
                new ProjectUpdatePatchRequest("", null)));
    }

    @Test
    void updatesUnderADeletedOrMissingProjectAre404() {
        Project project = create("ongoing");
        ProjectUpdate update = projectUpdateService.create(project.getId(), new ProjectUpdateCreateRequest("ok", null, null, null));
        projectService.delete(project.getId());

        assertStatus(HttpStatus.NOT_FOUND, () -> projectUpdateService.list(project.getId()));
        assertStatus(HttpStatus.NOT_FOUND, () -> projectUpdateService.update(update.getId(), new ProjectUpdatePatchRequest("x", null)));
        assertStatus(HttpStatus.NOT_FOUND, () -> projectUpdateService.list("no-such-project"));
    }
}
