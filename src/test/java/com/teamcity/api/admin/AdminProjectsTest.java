package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.exceptions.ValidationException;
import com.teamcity.core.models.Project;
import com.teamcity.core.testdata.InvalidTestData;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Feature("Project Management")
@Tag("admin")
public class AdminProjectsTest extends BaseApiTest {

    private static final String NON_EXISTENT_ID = "non-existent-id-12345";

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldCreateProjectWithValidData() {
        Project request = dataFactory.createRandomProject();

        Project created = projectSteps.createProject(request);
        trackProject(created.getId());

        ApiAssertions.assertProjectCreated(request, created);
        assertThat(created.getParentProjectId()).isEqualTo("_Root");
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldGetProjectById() {
        Project created = givenProject();

        Project retrieved = projectSteps.getProject(created.getId());

        ApiAssertions.assertProjectsEqual(created, retrieved);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldDeleteProject() {
        Project created = projectSteps.createProject(dataFactory.createRandomProject());

        projectSteps.deleteProject(created.getId());

        assertThat(projectSteps.projectExists(created.getId())).isFalse();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetAllProjects() {
        Project first = givenProject();
        Project second = givenProject();

        List<Project> projects = projectSteps.getAllProjects();

        assertThat(projects)
                .extracting(Project::getId)
                .contains(first.getId(), second.getId());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldUpdateProjectName() {
        Project created = givenProject();
        String newName = dataFactory.generateUniqueProjectName();

        Project updated = projectSteps.updateProject(created.getId(), newName);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo(newName);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldUpdateProjectDescription() {
        Project created = givenProject();
        String newDescription = "Updated description " + System.currentTimeMillis();

        Project updated = projectSteps.updateProjectDescription(created.getId(), newDescription);

        assertThat(updated.getDescription()).isEqualTo(newDescription);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateProjectWithDescription() {
        Project request = dataFactory.createRandomProject();
        request.setDescription("Test project description");

        Project created = givenProject(request);

        assertThat(created.getDescription()).isEqualTo("Test project description");
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Create under parent via newProjectDescription.parentProject.locator; list children via locator=project:(id:...)")
    void shouldCreateProjectWithParent() {
        Project parent = givenProject();
        Project childRequest = dataFactory.createRandomProject();

        Project child = projectSteps.createProjectUnderParent(childRequest, parent.getId());
        trackProject(child.getId());

        assertThat(child.getParentProjectId()).isEqualTo(parent.getId());
        assertThat(projectSteps.getChildProjects(parent.getId()))
                .extracting(Project::getId)
                .contains(child.getId());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectEmptyProjectName() {
        Project request = InvalidTestData.projectWithName("");

        assertThatThrownBy(() -> projectSteps.createProject(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Project name cannot be empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("TeamCity returns HTTP 500 for whitespace-only project names")
    void shouldRejectWhitespaceProjectName(String whitespaceName) {
        Project request = InvalidTestData.projectWithName(whitespaceName);

        ApiAssertions.assertStatus(() -> projectSteps.createProject(request), 500);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateProjectWithDuplicateName() {
        Project request = dataFactory.createRandomProject();
        givenProject(request);

        ApiAssertions.assertDuplicate(() -> projectSteps.createProject(request));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentProject() {
        ApiAssertions.assertNotFound(() -> projectSteps.getProject(NON_EXISTENT_ID));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404WhenUpdatingNonExistentProject() {
        ApiAssertions.assertNotFound(() -> projectSteps.updateProject(NON_EXISTENT_ID, "New Name"));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404WhenDeletingNonExistentProject() {
        ApiAssertions.assertNotFound(() -> projectSteps.deleteProject(NON_EXISTENT_ID));
    }

    @ParameterizedTest
    @MethodSource("parentProjectConfigurations")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateProjectWithVariousParents(String parentId, String expectedParentId) {
        Project request = dataFactory.createRandomProject();
        request.setParentProjectId(parentId);

        Project created = givenProject(request);

        assertThat(created.getParentProjectId()).isEqualTo(expectedParentId);
    }

    static Stream<Arguments> parentProjectConfigurations() {
        return Stream.of(
                Arguments.of("_Root", "_Root"),
                Arguments.of("non-existent-id-12345", "_Root"),
                Arguments.of("", "_Root"),
                Arguments.of(null, "_Root")
        );
    }
}
