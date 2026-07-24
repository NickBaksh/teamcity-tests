package com.teamcity.ui.user;

import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.BuildRunSteps;
import com.teamcity.core.testdata.TestDataValues;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.UserUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Feature("UI Build Management")
@Tag("ui")
@Tag("user")
@ExtendWith(UserUiSessionExtension.class)
public class UserBuildsUITest extends BaseUiTest {

    private void runBuildAndWait(User user, BuildConfig buildConfig) {
        BuildRunSteps userSteps = givenBuildRunSteps(user);

        Build build = userSteps.runBuild(buildConfig.getId());
        userSteps.waitForBuildFinish(build.getId());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void userCanRunBuildOfAdminProject(User user){
        BuildConfig buildConfig = givenBuildConfig();

        buildConfigPage
                .openById(buildConfig.getId())
                .shouldBeOpened()
                .runBuild()
                .waitForBuildFinished();

        BuildRunSteps userSteps = givenBuildRunSteps(user);
        Build build = userSteps.getLatestBuild(buildConfig.getId());

        ApiAssertions.assertBuildFinished(
                build,
                build.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void userCanViewBuildDetails(User user) {
        BuildConfig buildConfig = givenBuildConfig();

        runBuildAndWait(user, buildConfig);

        buildConfigPage
                .openById(buildConfig.getId())
                .shouldBeOpened()
                .openLatestBuild()
                .shouldBeOpened()
                .shouldHaveStatus(TestDataValues.BUILD_STATUS_SUCCESS);
    }


    @Test
    @Severity(SeverityLevel.NORMAL)
    void userCannotSeeDeleteBuildAction(User user) {
        BuildConfig buildConfig = givenBuildConfig();

        runBuildAndWait(user, buildConfig);

        buildConfigPage
                .openById(buildConfig.getId())
                .shouldBeOpened()
                .openBuildActionsMenu()
                .shouldNotHaveRemoveBuildAction();
    }

}
