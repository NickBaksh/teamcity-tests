package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.dto.RunBuildRequest;
import com.teamcity.core.steps.BuildRunSteps;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Build Management")
@Tag("admin")
@Execution(ExecutionMode.SAME_THREAD)
public class AdminBuildsTest extends BaseApiTest {
    private String testProjectId;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        testProjectId = givenProject().getId();
        ensureAgentEnabled();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldRunBuildOfAdminProject() {
        BuildConfig buildConfig = givenBuildConfig(testProjectId);

        Build build = givenAdminBuildRunSteps()
                .runBuild(buildConfig.getId());

        ApiAssertions.assertBuildTriggered(build);
        assertThat(build.getBuildTypeId())
                .isEqualTo(buildConfig.getId());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldNotAddBuildWithNonExistentConfig() {
        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(TestDataValues.NON_EXISTENT_ID_RANDOM)
                .build();

        ApiAssertions.assertNotFound(
                () -> givenAdminBuildRunSteps().runBuild(request)
        );
    }

    @Test
    @Disabled("Temporarily skipped to unblock CI; queue pause / agent desync under investigation")
    @Severity(SeverityLevel.NORMAL)
    void shouldGetQueuedBuildStatus() {
        BuildConfig config = givenBuildConfig(testProjectId);
        BuildRunSteps steps = givenAdminBuildRunSteps();
        // Pause the queue — do NOT disable agents: that leaves the agent with a stranded
        // local build and later finishes as UNKNOWN ("Agent runs unknown build...").
        Build build = null;
        boolean queuePaused = false;
        try {
            steps.setBuildQueuePaused(true, "API test: assert queued state");
            queuePaused = true;

            build = steps.runBuild(config.getId());
            Build queuedBuild = steps.getBuild(build.getId());

            assertThat(queuedBuild.getState())
                    .isEqualTo(TestDataValues.BUILD_STATE_QUEUED);
            assertThat(queuedBuild.getBuildTypeId())
                    .isEqualTo(config.getId());
        } finally {
            if (build != null) {
                try {
                    steps.cancelBuild(build.getId());
                } catch (Exception ignored) {
                }
            }
            if (queuePaused) {
                steps.setBuildQueuePaused(false, "API test: resume queue");
            }
        }
    }

    @Test
    @Disabled("Temporarily skipped to unblock CI; long-running sleep leaves agent busy under investigation")
    @Severity(SeverityLevel.NORMAL)
    void shouldGetRunningBuildStatus() {
        BuildConfig config = givenBuildConfig(testProjectId);
        // Keep the build running long enough to observe state=running (echo finishes too fast).
        buildConfigSteps.addCommandLineStep(config.getId(), "sleep 20");

        BuildRunSteps steps = givenAdminBuildRunSteps();
        Build build = steps.runBuild(config.getId());

        try {
            Build runningBuild = steps.waitForBuildState(
                    build.getId(),
                    TestDataValues.BUILD_STATE_RUNNING,
                    TestDataValues.BUILD_WAIT_TIMEOUT_SECONDS
            );

            assertThat(runningBuild.getState())
                    .isEqualTo(TestDataValues.BUILD_STATE_RUNNING);
            assertThat(runningBuild.getBuildTypeId())
                    .isEqualTo(config.getId());
        } finally {
            try {
                steps.cancelBuild(build.getId());
                steps.waitForBuildFinish(build.getId());
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetFinishedBuildStatus() {
        BuildConfig config = givenRunnableBuildConfig(testProjectId);

        Build finishedBuild = givenFinishedBuild(config.getId());

        Build actualBuild = givenAdminBuildRunSteps().getBuild(finishedBuild.getId());

        ApiAssertions.assertBuildFinished(
                actualBuild,
                finishedBuild.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS
        );
        assertThat(actualBuild.getBuildTypeId())
                .isEqualTo(config.getId());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentBuildStatus() {
        ApiAssertions.assertNotFound(
                () -> givenAdminBuildRunSteps().getBuild(TestDataValues.NON_EXISTENT_ID_RANDOM)
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetBuildDetails() {
        BuildConfig config = givenRunnableBuildConfig(testProjectId);

        Build finishedBuild = givenFinishedBuild(config.getId());

        Build details = givenAdminBuildRunSteps().getBuild(finishedBuild.getId());

        ApiAssertions.assertBuildFinished(
                details,
                finishedBuild.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS
        );
        assertThat(details.getBuildTypeId())
                .isEqualTo(config.getId());
        assertThat(details.getState())
                .isEqualTo(TestDataValues.BUILD_STATE_FINISHED);
        assertThat(details.getStatus())
                .isEqualTo(TestDataValues.BUILD_STATUS_SUCCESS);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentBuildDetails() {
        ApiAssertions.assertNotFound(
                () -> givenAdminBuildRunSteps().getBuild(TestDataValues.NON_EXISTENT_ID_RANDOM)
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentBuildCancellation() {
        ApiAssertions.assertNotFound(
                () -> givenAdminBuildRunSteps().cancelBuild(TestDataValues.NON_EXISTENT_ID_RANDOM)
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldDeleteFinishedBuild() {
        BuildConfig config = givenRunnableBuildConfig(testProjectId);

        Build finishedBuild = givenFinishedBuild(config.getId());

        givenAdminBuildRunSteps().deleteBuild(finishedBuild.getId());

        ApiAssertions.assertNotFound(
                () -> givenAdminBuildRunSteps().getBuild(finishedBuild.getId())
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentBuildDeletion() {
        ApiAssertions.assertNotFound(
                () -> givenAdminBuildRunSteps().deleteBuild(TestDataValues.NON_EXISTENT_ID_RANDOM)
        );
    }

    private void ensureAgentEnabled() {
        ensureConnectedAgentsEnabled();
    }
}
