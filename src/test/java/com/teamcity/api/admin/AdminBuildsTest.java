package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Agent;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.dto.RunBuildRequest;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.Duration;
import java.util.List;

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
    @Severity(SeverityLevel.NORMAL)
    void shouldGetQueuedBuildStatus() {
        BuildConfig config = givenBuildConfig(testProjectId);
        List<Agent> connected = agentSteps.getConnectedAgents();
        assertThat(connected)
                .as("Need at least one connected agent to disable for queue assertion")
                .isNotEmpty();

        connected.forEach(agent -> agentSteps.disableAgent(String.valueOf(agent.getId())));

        Build build = null;
        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(200))
                    .until(() -> connected.stream().allMatch(agent ->
                            Boolean.FALSE.equals(
                                    agentSteps.getAgent(String.valueOf(agent.getId())).getEnabled()
                            )
                    ));

            build = givenAdminBuildRunSteps().runBuild(config.getId());
            Build queuedBuild = givenAdminBuildRunSteps().getBuild(build.getId());

            assertThat(queuedBuild.getState())
                    .isEqualTo(TestDataValues.BUILD_STATE_QUEUED);
            assertThat(queuedBuild.getBuildTypeId())
                    .isEqualTo(config.getId());
        } finally {
            if (build != null) {
                try {
                    givenAdminBuildRunSteps().cancelBuild(build.getId());
                    givenAdminBuildRunSteps().waitForBuildState(
                            build.getId(),
                            TestDataValues.BUILD_STATE_FINISHED,
                            30
                    );
                } catch (Exception ignored) {
                }
            }
            connected.forEach(agent -> agentSteps.enableAgent(String.valueOf(agent.getId())));
        }
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetRunningBuildStatus() {
        BuildConfig config = givenBuildConfig(testProjectId);
        // Keep the build running long enough to observe state=running (echo finishes too fast).
        buildConfigSteps.addCommandLineStep(config.getId(), "sleep 20");

        Build build = givenAdminBuildRunSteps().runBuild(config.getId());

        Build runningBuild = givenAdminBuildRunSteps().waitForBuildState(
                build.getId(),
                TestDataValues.BUILD_STATE_RUNNING,
                TestDataValues.BUILD_WAIT_TIMEOUT_SECONDS
        );

        assertThat(runningBuild.getState())
                .isEqualTo(TestDataValues.BUILD_STATE_RUNNING);
        assertThat(runningBuild.getBuildTypeId())
                .isEqualTo(config.getId());
    }

    @Test
//    @Disabled("Flaky on single CI agent: finished status can stay UNKNOWN after agent desync")
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
