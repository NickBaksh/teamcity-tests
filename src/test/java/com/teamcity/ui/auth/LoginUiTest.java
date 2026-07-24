package com.teamcity.ui.auth;

import com.teamcity.core.config.ConfigManager;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.testdata.UiTestData;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("UI Authentication")
@Tag("ui")
public class LoginUiTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldLoginWithValidCredentials() {
        loginPage.openPage()
                .loginSuccessfully(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword());

        projectsPage.openPage();
        assertThat(projectsPage.visibleProjectsCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectInvalidCredentials() {
        loginPage.openPage()
                .login(ConfigManager.getAdminLogin(), UiTestData.invalidPassword())
                .shouldStayOnLoginAfterFailure();
    }
}
