package com.teamcity.ui.admin;

import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.steps.BuildRunSteps;
import com.teamcity.core.testdata.TestDataValues;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.teamcity.core.testdata.TestDataValues.NON_EXISTENT_ID_RANDOM;

@Feature("UI Artifact Management")
@Tag("ui")
@Tag("admin")
@ExtendWith(AdminUiSessionExtension.class)
public class AdminRunBuildUiTest extends BaseUiTest {
    private String buildConfigId;

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void adminCanRunBuildOfProject() {
        BuildConfig buildConfig = givenBuildConfig();

        buildConfigPage
                .openById(buildConfig.getId())
                .shouldBeOpened()
                .runBuild()
                .waitForBuildFinished();

        BuildRunSteps userSteps = givenAdminBuildRunSteps();
        Build build = userSteps.getLatestBuild(buildConfig.getId());

        ApiAssertions.assertBuildFinished(
                build,
                build.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void adminCanRunBuildOfAdminProject() {
        BuildConfig buildConfig = givenBuildConfig();

        buildConfigPage
                .openById(buildConfig.getId())
                .shouldBeOpened()
                .runBuild()
                .waitForBuildFinished()
                .openLatestBuild()
                .shouldBeOpened()
                .shouldHaveStatus(TestDataValues.BUILD_STATUS_SUCCESS);

        BuildRunSteps userSteps = givenAdminBuildRunSteps();
        Build build = userSteps.getLatestBuild(buildConfig.getId());

        ApiAssertions.assertBuildFinished(
                build,
                build.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCannotRunBuildWithoutSteps() {

        BuildConfig buildConfig = givenBuildConfig();
        buildConfigId = buildConfig.getId();

        buildConfigPage
                .openById(buildConfigId)
                .shouldBeOpened()
                .runBuild()
                .hasValidationError();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void adminOpenNonExistentBuild() {
        buildConfigPage
                .openById(NON_EXISTENT_ID_RANDOM)
                .shouldBeOpened()
                .shouldContainNotFound();
    }
}
