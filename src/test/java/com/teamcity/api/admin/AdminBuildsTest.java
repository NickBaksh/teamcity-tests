package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.dto.RunBuildRequest;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Build Management")
@Tag("admin")
public class AdminBuildsTest extends BaseApiTest {
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

        // Запускаем сборку, она будет в очереди
        Build build = givenAdminBuildRunSteps().runBuild(config.getId());

        // Получаем статус сборки в очереди
        Build queuedBuild = givenAdminBuildRunSteps().getBuild(build.getId());

        // Проверяем, что сборка находится в очереди
        assertThat(queuedBuild.getState())
                .isEqualTo(TestDataValues.BUILD_STATE_QUEUED);
        assertThat(queuedBuild.getBuildTypeId())
                .isEqualTo(config.getId());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetRunningBuildStatus() {
        BuildConfig config = givenBuildConfig(testProjectId);

        // Запускаем сборку
        Build build = givenAdminBuildRunSteps().runBuild(config.getId());

        // Ждем, пока сборка начнет выполняться
        Build runningBuild = givenAdminBuildRunSteps().waitForBuildState(
                build.getId(),
                TestDataValues.BUILD_STATE_RUNNING,
                TestDataValues.BUILD_WAIT_TIMEOUT_SECONDS
        );

        // Проверяем, что сборка выполняется
        assertThat(runningBuild.getState())
                .isEqualTo(TestDataValues.BUILD_STATE_RUNNING);
        assertThat(runningBuild.getBuildTypeId())
                .isEqualTo(config.getId());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetFinishedBuildStatus() {
        // TODO: flaky test, иногда возвращается статус "UNKNOWN" в проверке assertBuildFinished
        BuildConfig config = givenBuildConfig(testProjectId);

        // Запускаем и ждем завершения сборки
        Build finishedBuild = givenFinishedBuild(config.getId());

        // Получаем статус завершенной сборки
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
        BuildConfig config = givenBuildConfig(testProjectId);

        // Запускаем и ждем завершения сборки
        Build finishedBuild = givenFinishedBuild(config.getId());

        // Получаем детали сборки
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
        BuildConfig config = givenBuildConfig(testProjectId);

        // Запускаем и ждем завершения сборки
        Build finishedBuild = givenFinishedBuild(config.getId());

        // Админ может удалить любую завершенную сборку
        givenAdminBuildRunSteps().deleteBuild(finishedBuild.getId());

        // Проверяем, что сборка удалена
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
}