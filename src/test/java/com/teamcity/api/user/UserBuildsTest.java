package com.teamcity.api.user;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.User;
import com.teamcity.core.models.dto.RunBuildRequest;
import com.teamcity.core.steps.BuildRunSteps;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Build Management")
@Tag("user")
public class UserBuildsTest extends BaseApiTest {
    private String testProjectId;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        testProjectId = givenProject().getId();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldRunBuildOfAdminProject() {
        BuildConfig buildConfig = givenBuildConfig(testProjectId);

        Build build = givenUserBuildRunSteps()
                .runBuild(buildConfig.getId());

        ApiAssertions.assertBuildTriggered(build);
        assertThat(build.getBuildTypeId())
                .isEqualTo(buildConfig.getId());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotRunNonExistingBuild() {

        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(TestDataValues.NON_EXISTENT_ID)
                .build();

        ApiAssertions.assertNotFound(
                () -> givenUserBuildRunSteps().runBuild(request)
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetBuildStatus() {

        BuildConfig config = givenBuildConfig(testProjectId);

        BuildRunSteps userSteps = givenUserBuildRunSteps();

        Build build = userSteps.runBuild(config.getId());

        Build finished = userSteps.waitForBuildFinish(build.getId());

        ApiAssertions.assertBuildFinished(
                finished,
                build.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetBuildDetails() {

        BuildConfig config = givenBuildConfig(testProjectId);

        BuildRunSteps userSteps = givenUserBuildRunSteps();

        Build build = userSteps.runBuild(config.getId());

        Build finished = userSteps.waitForBuildFinish(build.getId());

        Build details = userSteps.getBuild(finished.getId());

        ApiAssertions.assertBuildFinished(
                details,
                finished.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS
        );

        assertThat(details.getBuildTypeId())
                .isEqualTo(config.getId());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldCancelRunningBuild() {

        BuildConfig config = givenBuildConfig(testProjectId);

        BuildRunSteps userSteps = givenUserBuildRunSteps();

        Build build = userSteps.runBuild(config.getId());

        userSteps.waitForBuildState(
                build.getId(),
                TestDataValues.BUILD_STATE_RUNNING,
                TestDataValues.BUILD_WAIT_TIMEOUT_SECONDS);

        userSteps.cancelBuild(build.getId());

        Build cancelled = userSteps.waitForBuildState(
                build.getId(),
                TestDataValues.BUILD_STATE_FINISHED,
                TestDataValues.BUILD_WAIT_TIMEOUT_SECONDS);

        ApiAssertions.assertBuildFinished(
                cancelled,
                build.getId(),
                TestDataValues.BUILD_STATUS_UNKNOWN
        );

        assertThat(cancelled.getStatusText())
                .containsIgnoringCase(TestDataValues.BUILD_STATUS_CANCEL);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldCancelAnotherUserBuild() {

        BuildConfig config = givenBuildConfig(testProjectId);

        User firstUser = givenUser();
        User secondUser = givenUser();

        BuildRunSteps firstUserSteps = givenBuildRunSteps(firstUser);
        BuildRunSteps secondUserSteps = givenBuildRunSteps(secondUser);

        Build build = firstUserSteps.runBuild(config.getId());

        firstUserSteps.waitForBuildState(
                build.getId(),
                TestDataValues.BUILD_STATE_RUNNING,
                TestDataValues.BUILD_WAIT_TIMEOUT_SECONDS);

        secondUserSteps.cancelBuild(build.getId());

        Build cancelled = firstUserSteps.waitForBuildState(
                build.getId(),
                TestDataValues.BUILD_STATE_FINISHED,
                TestDataValues.BUILD_WAIT_TIMEOUT_SECONDS);

        ApiAssertions.assertBuildFinished(
                cancelled,
                build.getId(),
                TestDataValues.BUILD_STATUS_UNKNOWN
        );

        assertThat(cancelled.getStatusText())
                .containsIgnoringCase(TestDataValues.BUILD_STATUS_CANCEL);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotDeleteOwnFinishedBuild() {

        BuildConfig config = givenBuildConfig(testProjectId);

        User user = givenUser();

        BuildRunSteps userSteps = givenBuildRunSteps(user);
        BuildRunSteps negativeSteps = givenNegativeBuildRunSteps(user);

        Build build = userSteps.runBuild(config.getId());

        Build finished = userSteps.waitForBuildFinish(build.getId());

        ApiAssertions.assertForbidden(
                () -> negativeSteps.deleteBuild(finished.getId())
        );

        Build actual = userSteps.getBuild(finished.getId());

        ApiAssertions.assertBuildFinished(
                actual,
                finished.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS
        );
    }
}
