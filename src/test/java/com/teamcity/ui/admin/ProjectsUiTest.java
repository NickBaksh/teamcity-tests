package com.teamcity.ui.admin;

import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.Project;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import com.teamcity.ui.testdata.UiTestData;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

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
        String name = UiTestData.projectName();
        String id = UiTestData.projectId();

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

        Awaitility.await()
                .pollInterval(Duration.ofSeconds(UiTestData.UI_POLL_INTERVAL_SECONDS))
                .atMost(Duration.ofSeconds(UiTestData.UI_DEFAULT_TIMEOUT_SECONDS))
                .untilAsserted(() ->
                        assertThatThrownBy(() -> projectSteps.getProject(project.getId()))
                                .isInstanceOf(ResourceNotFoundException.class)
                );
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
                .createExpectingError("", UiTestData.emptyNameProjectId())
                .shouldShowEmptyNameError();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectDuplicateProjectId() {
        Project existing = givenProject();

        createProjectPage.openPage()
                .createExpectingError(UiTestData.duplicateProjectName(), existing.getId())
                .shouldShowDuplicateIdError();
    }
}
