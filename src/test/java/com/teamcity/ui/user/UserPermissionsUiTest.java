package com.teamcity.ui.user;

import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.UserUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("UI User Permissions")
@Tag("ui")
@ExtendWith(UserUiSessionExtension.class)
public class UserPermissionsUiTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldViewProjectsAsUser(User user) {
        Project project = givenProject();

        projectsPage.openPage().shouldContainProject(project.getName());
        assertThat(user.getUsername()).isNotBlank();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateProjectAsUser() {
        projectsPage.openPage();
        assertThat(projectPage.isCreateProjectAvailable()).isFalse();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldViewBuildConfigAsUser() {
        BuildConfig config = givenBuildConfig();

        buildConfigPage.openById(config.getId()).shouldHaveName(config.getName());
    }
}
