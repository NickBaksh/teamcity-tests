package com.teamcity.ui.admin;

import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.BuildConfig;
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
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("UI Build Configuration Management")
@Tag("ui")
@ExtendWith(AdminUiSessionExtension.class)
public class BuildConfigsUiTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldCreateBuildConfigViaUi() {
        Project project = givenProject();
        String name = UiTestData.buildConfigName();

        // Sakura setup wizard does not expose an ID field; TC auto-generates it from the name.
        buildConfigPage.openCreate(project.getId()).create(name);

        BuildConfig created = Awaitility.await()
                .pollInterval(Duration.ofSeconds(UiTestData.UI_POLL_INTERVAL_LONG_SECONDS))
                .atMost(Duration.ofSeconds(UiTestData.UI_LONG_TIMEOUT_SECONDS))
                .ignoreException(ResourceNotFoundException.class)
                .until(() -> buildConfigSteps.findBuildConfigByName(name), Objects::nonNull);

        trackBuildConfig(created.getId());
        assertThat(created.getName()).isEqualTo(name);
        assertThat(created.getProjectId()).isEqualTo(project.getId());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldAddBuildStepViaUi() {
        BuildConfig config = givenBuildConfig();
        String stepName = UiTestData.buildStepName();
        int before = buildConfigSteps.getBuildStepsCount(config.getId());

        buildConfigPage.addCommandLineStep(config.getId(), stepName);

        Awaitility.await()
                .pollInterval(Duration.ofSeconds(UiTestData.UI_POLL_INTERVAL_SECONDS))
                .atMost(Duration.ofSeconds(UiTestData.UI_SHORT_TIMEOUT_SECONDS))
                .untilAsserted(() ->
                        assertThat(buildConfigSteps.getBuildStepsCount(config.getId()))
                                .as("Command Line step should be saved")
                                .isGreaterThan(before)
                );

        buildConfigPage.openSteps(config.getId()).shouldReflectAddedStep(stepName);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectEmptyBuildConfigName() {
        Project project = givenProject();

        buildConfigPage.openClassicCreate(project.getId())
                .createExpectingError("")
                .shouldShowEmptyNameError();
    }
}
