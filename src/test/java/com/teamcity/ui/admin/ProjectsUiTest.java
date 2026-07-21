package com.teamcity.ui.admin;

import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.generators.RandomData;
import com.teamcity.core.models.Project;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Feature("UI Project Management")
@Tag("ui")
@ExtendWith(AdminUiSessionExtension.class)
public class ProjectsUiTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldShowProjectsInList() {
        Project project = givenProject();

        projectsPage.openPage().shouldContainProject(project.getName());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldCreateProjectViaUi() {
        String name = "ui_project_" + RandomData.shortId();
        String id = "UIProject" + RandomData.shortId();

        createProjectPage.openPage().create(name, id);
        trackProject(id);

        projectsPage.openPage().shouldContainProject(name);
        assertThat(projectSteps.getProject(id).getName()).isEqualTo(name);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldDeleteProjectViaUi() {
        Project project = givenProject();

        projectPage.deleteProject(project.getId());

        assertThatThrownBy(() -> projectSteps.getProject(project.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        projectsPage.openPage().search(project.getName()).shouldNotContainProject(project.getName());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldSearchProjectByName() {
        Project project = givenProject();

        projectsPage.openPage()
                .search(project.getName())
                .shouldContainProject(project.getName());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectEmptyProjectName() {
        createProjectPage.openPage()
                .createExpectingError("", "EmptyName" + RandomData.shortId());

        assertThat(createProjectPage.hasValidationError()).isTrue();
        assertThat(createProjectPage.errorText()).containsIgnoringCase("empty");
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectDuplicateProjectId() {
        Project existing = givenProject();

        createProjectPage.openPage()
                .createExpectingError("Duplicate " + RandomData.shortId(), existing.getId());

        assertThat(createProjectPage.hasValidationError()).isTrue();
        assertThat(createProjectPage.errorText()).containsIgnoringCase("already used");
    }
}
