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
@Epic("Admin API")
@Feature("Project Management")
@Tag("admin")
@Tag("projects")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminProjectsTest extends BaseApiTest {

    private ProjectSteps projectSteps;

    @BeforeEach
    void initSteps() {
        projectSteps = new ProjectSteps(adminClient);
    }

    @Test
    @Order(1)
    @Tag("smoke")
    @Tag("critical")
    @Tag("crud")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Create project with valid data")
    @Description("Verifies that a project can be created with valid data")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Create project")
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

        log.info("✅ Project created: {}", created.getName());
    }

    @Test
    @Order(2)
    @Tag("smoke")
    @Tag("critical")
    @Tag("crud")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Get project by ID")
    @Description("Verifies that a project can be retrieved by ID")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Get project")
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

        log.info("✅ Project retrieved: {}", retrieved.getName());
    }

    @Test
    @Order(3)
    @Tag("smoke")
    @Tag("critical")
    @Tag("crud")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Delete project")
    @Description("Verifies that a project can be deleted")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Delete project")
    void shouldDeleteProject() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);

        projectSteps.deleteProject(created.getId());

        assertThat(projectSteps.projectExists(created.getId())).isFalse();

        log.info("✅ Project deleted: {}", created.getName());
    }

    @Test
    @Order(4)
    @Tag("positive")
    @Tag("normal")
    @Tag("crud")
    @DisplayName("✅ Get all projects")
    @Description("Verifies that all projects can be retrieved")
    @Severity(SeverityLevel.NORMAL)
    @Story("Get project")
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

        log.info("✅ Retrieved {} projects", projects.size());
    }

    @Test
    @Order(5)
    @Tag("positive")
    @Tag("normal")
    @Tag("crud")
    @DisplayName("✅ Update project name")
    @Description("Verifies that project name can be updated")
    @Severity(SeverityLevel.NORMAL)
    @Story("Update project")
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

        log.info("✅ Project updated: {} → {}", project.getName(), newName);
    }

    @Test
    @Order(6)
    @Tag("positive")
    @Tag("normal")
    @Tag("crud")
    @DisplayName("✅ Update project description")
    @Description("Verifies that project description can be updated")
    @Severity(SeverityLevel.NORMAL)
    @Story("Update project")
    void shouldUpdateProjectDescription() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        String newDescription = "Updated description " + System.currentTimeMillis();
        Project updated = projectSteps.updateProjectDescription(created.getId(), newDescription);

        assertThat(updated.getDescription()).isEqualTo(newDescription);

        log.info("✅ Project description updated: {}", newDescription);
    }

    @Test
    @Order(7)
    @Tag("positive")
    @Tag("normal")
    @Tag("parent")
    @DisplayName("✅ Create project with parent project")
    @Description("Verifies that a child project can be created under a parent")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create project")
    void shouldCreateProjectWithParent() {
        Project parent = dataFactory.createRandomProject();
        Project createdParent = projectSteps.createProject(parent);
        trackProject(createdParent.getId());

        Project child = dataFactory.createRandomProject(createdParent.getId());
        Project createdChild = projectSteps.createProject(child);
        trackProject(createdChild.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(createdChild.getParentProjectId()).isEqualTo(createdParent.getId());
        softly.assertThat(createdChild.getName()).isEqualTo(child.getName());
        softly.assertThat(createdChild.getId()).isNotEqualTo(createdParent.getId());
        softly.assertAll();

        log.info("✅ Child project created: {} under parent: {}",
                createdChild.getName(), createdParent.getName());
    }

    @Test
    @Order(8)
    @Tag("positive")
    @Tag("normal")
    @Tag("crud")
    @DisplayName("✅ Create project with description")
    @Description("Verifies that a project can be created with description")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create project")
    void shouldCreateProjectWithDescription() {
        String description = "Test project description";
        Project project = dataFactory.createRandomProject();
        project.setDescription(description);

        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        assertThat(created.getDescription()).isEqualTo(description);

        log.info("✅ Project with description created: {}", created.getName());
    }

    @ParameterizedTest
    @Order(9)
    @Tag("negative")
    @Tag("critical")
    @Tag("validation")
    @ValueSource(strings = {"", " ", "\t"})
    @DisplayName("❌ Create project with invalid name → 400")
    @Description("Verifies that invalid project names are rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create project validation")
    void shouldNotCreateProjectWithInvalidName(String invalidName) {
        Project project = Project.builder()
                .name(invalidName)
                .parentProjectId("_Root")
                .build();

        assertThatThrownBy(() -> projectSteps.createProject(project))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cannot be empty");

        log.info("✅ Project name '{}' correctly rejected", invalidName);
    }

    @Test
    @Order(10)
    @Tag("negative")
    @Tag("critical")
    @Tag("conflict")
    @DisplayName("❌ Create project with duplicate name → 409")
    @Description("Verifies that duplicate project names are rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create project validation")
    void shouldNotCreateProjectWithDuplicateName() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        assertThatThrownBy(() -> projectSteps.createProject(project))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        log.info("✅ Duplicate project name correctly rejected");
    }

    @Test
    @Order(11)
    @Tag("negative")
    @Tag("not-found")
    @DisplayName("❌ Get non-existent project → 404")
    @Description("Verifies that non-existent project returns 404")
    @Severity(SeverityLevel.NORMAL)
    @Story("Get project validation")
    void shouldReturn404ForNonExistentProject() {
        assertThatThrownBy(() -> projectSteps.getProject("non-existent-id-12345"))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

        log.info("✅ Non-existent project correctly rejected");
    }

    @Test
    @Order(12)
    @Tag("negative")
    @Tag("not-found")
    @DisplayName("❌ Update non-existent project → 404")
    @Description("Verifies that updating non-existent project returns 404")
    @Severity(SeverityLevel.NORMAL)
    @Story("Update project validation")
    void shouldReturn404WhenUpdatingNonExistentProject() {
        assertThatThrownBy(() -> projectSteps.updateProject("non-existent-id-12345", "New Name"))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

        log.info("✅ Non-existent project update correctly rejected");
    }

    @Test
    @Order(13)
    @Tag("negative")
    @Tag("not-found")
    @DisplayName("❌ Delete non-existent project → 404")
    @Description("Verifies that deleting non-existent project returns 404")
    @Severity(SeverityLevel.NORMAL)
    @Story("Delete project validation")
    void shouldReturn404WhenDeletingNonExistentProject() {
        assertThatThrownBy(() -> projectSteps.deleteProject("non-existent-id-12345"))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

        log.info("✅ Non-existent project deletion correctly rejected");
    }

    @ParameterizedTest
    @Order(14)
    @Tag("negative")
    @Tag("parameterized")
    @Tag("parent")
    @MethodSource("projectParentProvider")
    @DisplayName("🔄 Create project with various parent configurations")
    @Description("Verifies project creation with different parent project configurations")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create project validation")
    void shouldCreateProjectWithVariousParents(String parentId, boolean shouldSucceed) {
        Project project = dataFactory.createRandomProject(parentId);

        if (shouldSucceed) {
            Project created = projectSteps.createProject(project);
            trackProject(created.getId());
            assertThat(created.getParentProjectId()).isEqualTo(parentId);
            log.info("✅ Project created with parent: {}", parentId);
        } else {
            assertThatThrownBy(() -> projectSteps.createProject(project))
                    .isInstanceOf(Exception.class);
            log.info("✅ Project creation with parent '{}' correctly rejected", parentId);
        }
    }

    static Stream<Arguments> projectParentProvider() {
        return Stream.of(
                Arguments.of("_Root", true),
                Arguments.of("non-existent-parent", false),
                Arguments.of("", false),
                Arguments.of(null, false)
        );
    }
}