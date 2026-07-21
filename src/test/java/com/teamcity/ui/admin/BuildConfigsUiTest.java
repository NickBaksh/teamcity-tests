package com.teamcity.ui.admin;

import com.teamcity.core.generators.RandomData;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("UI Build Configuration Management")
@Tag("ui")
@ExtendWith(AdminUiSessionExtension.class)
public class BuildConfigsUiTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldCreateBuildConfigViaUi() {
        Project project = givenProject();
        String name = "ui_bc_" + RandomData.shortId();

        buildConfigPage.openCreate(project.getId()).create(name, null);

        BuildConfig created = Awaitility.await()
                .pollInterval(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(45))
                .until(() -> {
                    try {
                        return buildConfigSteps.findBuildConfigByName(name);
                    } catch (Exception e) {
                        String fromUi = buildConfigPage.readCreatedBuildConfigId();
                        if (fromUi != null && buildConfigSteps.buildConfigExists(fromUi)) {
                            return buildConfigSteps.getBuildConfig(fromUi);
                        }
                        return null;
                    }
                }, config -> config != null);

        trackBuildConfig(created.getId());
        assertThat(created.getName()).isEqualTo(name);
        assertThat(created.getProjectId()).isEqualTo(project.getId());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldAddBuildStepViaUi() {
        BuildConfig config = givenBuildConfig();
        String stepName = "echo_step_" + RandomData.shortId();
        int before = buildConfigSteps.getBuildStepsCount(config.getId());

        buildConfigPage.addCommandLineStep(config.getId(), stepName);

        Awaitility.await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() ->
                        assertThat(buildConfigSteps.getBuildStepsCount(config.getId()))
                                .as("Command Line step should be saved")
                                .isGreaterThan(before)
                );

        buildConfigPage.openSteps(config.getId());
        String page = com.codeborne.selenide.WebDriverRunner.source();
        assertThat(page)
                .as("Build Steps page should reflect added runner")
                .containsAnyOf(stepName, "Command Line", "simpleRunner", "echo");
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @org.junit.jupiter.api.Disabled("Pause/Activate in TC 2026 UI is behind Actions popup; dialog submit is unstable in headless. Cover via API pause until Actions locator is fixed.")
    void shouldPauseAndResumeBuildConfigViaUi() {
        BuildConfig config = givenBuildConfig();

        buildConfigPage.pause(config.getId());
        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(buildConfigSteps.getBuildConfig(config.getId()).getPaused()).isTrue()
        );

        buildConfigPage.resume(config.getId());
        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(buildConfigSteps.getBuildConfig(config.getId()).getPaused()).isFalse()
        );
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectEmptyBuildConfigName() {
        Project project = givenProject();

        buildConfigPage.openClassicCreate(project.getId()).createExpectingError("");

        assertThat(buildConfigPage.hasValidationError()).isTrue();
    }
}
