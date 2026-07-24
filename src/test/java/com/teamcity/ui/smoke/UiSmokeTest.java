package com.teamcity.ui.smoke;

import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.models.Project;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import com.teamcity.ui.testdata.UiTestData;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("UI Smoke")
@Tag("ui")
public class UiSmokeTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldLoginAsAdminViaUi() {
        loginPage.openPage()
                .loginSuccessfully(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword());

        projectsPage.openPage();
        assertThat(projectsPage.visibleProjectsCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @ExtendWith(AdminUiSessionExtension.class)
    void shouldCreateProjectViaUi() {
        String name = UiTestData.smokeProjectName();
        String id = UiTestData.smokeProjectId();

        createProjectPage.openPage().create(name, id);
        trackProject(id);

        projectsPage.openPage().shouldContainProject(name);

        Project created = projectSteps.getProject(id);
        assertThat(created.getName()).isEqualTo(name);
    }
}
