package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.DuplicateResourceException;
import com.teamcity.core.exceptions.ValidationException;
import com.teamcity.core.models.Project;
import com.teamcity.core.steps.ProjectSteps;
import io.qameta.allure.*;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@Feature("Project Management")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminProjectsTest extends BaseApiTest {

    private ProjectSteps projectSteps;

    @BeforeEach
    void initSteps() {
        projectSteps = new ProjectSteps(adminClient);
    }

    @Test
    @Description("Verifies that a project can be created with valid data")
    @Severity(SeverityLevel.BLOCKER)
    void shouldCreateProjectWithValidData() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created).isNotNull();
        softly.assertThat(created.getId()).isNotBlank();
        softly.assertThat(created.getName()).isEqualTo(project.getName());
        softly.assertThat(created.getParentProjectId()).isEqualTo("_Root");
        softly.assertThat(created.getHref()).isNotBlank();
        softly.assertThat(created.getWebUrl()).isNotBlank();
        softly.assertThat(created.getArchived()).isIn(false, null);
        softly.assertAll();

    }

    @Test
    @Description("Verifies that a project can be retrieved by ID")
    @Severity(SeverityLevel.BLOCKER)
    void shouldGetProjectById() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        Project retrieved = projectSteps.getProject(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(retrieved).isNotNull();
        softly.assertThat(retrieved.getId()).isEqualTo(created.getId());
        softly.assertThat(retrieved.getName()).isEqualTo(created.getName());
        softly.assertThat(retrieved.getHref()).isNotBlank();
        softly.assertAll();

    }

    @Test
    @Description("Verifies that a project can be deleted")
    @Severity(SeverityLevel.BLOCKER)
    void shouldDeleteProject() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);

        projectSteps.deleteProject(created.getId());
        assertThat(projectSteps.projectExists(created.getId())).isFalse();
    }

    @Test
    @Description("Verifies that all projects can be retrieved")
    @Severity(SeverityLevel.NORMAL)
    void shouldGetAllProjects() {
        Project project1 = dataFactory.createRandomProject();
        Project created1 = projectSteps.createProject(project1);
        trackProject(created1.getId());

        Project project2 = dataFactory.createRandomProject();
        Project created2 = projectSteps.createProject(project2);
        trackProject(created2.getId());

        List<Project> projects = projectSteps.getAllProjects();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(projects).isNotNull();
        softly.assertThat(projects).hasSizeGreaterThanOrEqualTo(2);
        softly.assertThat(projects).extracting(Project::getId)
                .contains(created1.getId(), created2.getId());
        softly.assertAll();

    }

    @Test
    @Description("Verifies that project name can be updated")
    @Severity(SeverityLevel.NORMAL)
    void shouldUpdateProjectName() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        String newName = dataFactory.generateUniqueProjectName();
        Project updated = projectSteps.updateProject(created.getId(), newName);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(updated.getId()).isEqualTo(created.getId());
        softly.assertThat(updated.getName()).isEqualTo(newName);
        softly.assertAll();

    }

    @Test
    @Description("Verifies that project description can be updated")
    @Severity(SeverityLevel.NORMAL)
    void shouldUpdateProjectDescription() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        String newDescription = "Updated description " + System.currentTimeMillis();
        Project updated = projectSteps.updateProjectDescription(created.getId(), newDescription);

        assertThat(updated.getDescription()).isEqualTo(newDescription);

    }

    @Test
    @Disabled("TC-API-003: TeamCity API does not reliably expose parent-child relationship")
    @Description("Verifies that a child project can be created under a parent")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateProjectWithParent() {

        Project parentProject = dataFactory.createRandomProject();
        Project createdParent = projectSteps.createProject(parentProject);
        trackProject(createdParent.getId());

        Project childProject = dataFactory.createRandomProject();
        childProject.setParentProjectId(createdParent.getId());
        Project createdChild = projectSteps.createProject(childProject);
        trackProject(createdChild.getId());


        Project reloadedChild = projectSteps.getProject(createdChild.getId());


        SoftAssertions softly = new SoftAssertions();
        List<Project> children = projectSteps.getChildProjects(createdParent.getId());

        softly.assertThat(children)

                .extracting(Project::getId)

                .as("Parent should contain created child project")

                .contains(createdChild.getId());
        softly.assertThat(reloadedChild.getId())
                .as("Child should have ID")
                .isNotBlank();
        softly.assertAll();


    }

    @Test
    @Description("Verifies that a project can be created with description")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateProjectWithDescription() {
        String description = "Test project description";
        Project project = dataFactory.createRandomProject();
        project.setDescription(description);

        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        assertThat(created.getDescription()).isEqualTo(description);

    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    @Description("Verifies that invalid project names are rejected." +
            "TeamCity rejects empty names with 400 and whitespace names with 500.")
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateProjectWithInvalidName(String invalidName) {
        // Создаем проект с невалидным именем
        Project project = dataFactory.createRandomProject();
        project.setName(invalidName);
        project.setParentProjectId("_Root");

        if (invalidName.isEmpty()) {
            assertThatThrownBy(() -> projectSteps.createProject(project))
                    .as("Empty project name should be rejected with ValidationException")
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Project name cannot be empty");
        } else {
            assertThatThrownBy(() -> projectSteps.createProject(project))
                    .as("Whitespace project name should be rejected with ApiException")
                    .isInstanceOf(ApiException.class)
                    .satisfies(exception -> {
                        ApiException apiEx = (ApiException) exception;
                        assertThat(apiEx.getStatusCode())
                                .as("Status code should be 500 for whitespace name")
                                .isEqualTo(500);
                    });
        }
    }

    private String escapeWhitespace(String input) {
        if (input == null) return "null";
        return input
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace(" ", "·");
    }

    @Test
    @Description("Verifies that duplicate project names are rejected")
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateProjectWithDuplicateName() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        assertThatThrownBy(() -> projectSteps.createProject(project))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

    }

    @Test
    @Description("Verifies that non-existent project returns 404")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentProject() {
        assertThatThrownBy(() -> projectSteps.getProject("non-existent-id-12345"))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

    }

    @Test
    @Description("Verifies that updating non-existent project returns 404")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404WhenUpdatingNonExistentProject() {
        assertThatThrownBy(() -> projectSteps.updateProject("non-existent-id-12345", "New Name"))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

    }

    @Test
    @Description("Verifies that deleting non-existent project returns 404")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404WhenDeletingNonExistentProject() {
        assertThatThrownBy(() -> projectSteps.deleteProject("non-existent-id-12345"))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

    }

    @ParameterizedTest
    @MethodSource("provideParentProjectConfigurations")
    @Description("Verifies project creation with different parent project configurations")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateProjectWithVariousParents(String parentId, String expectedParentId) {
        Project project = dataFactory.createRandomProject();
        project.setParentProjectId(parentId);

        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        assertThat(created.getParentProjectId())
                .as("Parent project ID should be: " + expectedParentId)
                .isEqualTo(expectedParentId);

        log.info("Project created with parent: {} (input: {})",
                expectedParentId, parentId == null ? "null" : "'" + parentId + "'");
    }

    static Stream<Arguments> provideParentProjectConfigurations() {
        return Stream.of(
                Arguments.of("_Root", "_Root"),
                Arguments.of("non-existent-id-12345", "_Root"),
                Arguments.of("", "_Root"),
                Arguments.of(null, "_Root")
        );
    }
}