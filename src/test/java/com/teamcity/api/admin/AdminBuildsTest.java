package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
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

        Build build = givenUserBuildRunSteps()
                .runBuild(buildConfig.getId());

        ApiAssertions.assertBuildTriggered(build);
        assertThat(build.getBuildTypeId())
                .isEqualTo(buildConfig.getId());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldNotAddBuildWithNonExistentConfig() {
        // TODO: реализовать негативный тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetQueuedBuildStatus() {
        // TODO: реализовать тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetRunningBuildStatus() {
        // TODO: реализовать тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetFinishedBuildStatus() {
        // TODO: реализовать тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentBuildStatus() {
        // TODO: реализовать негативный тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetBuildDetails() {
        // TODO: реализовать тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentBuildDetails() {
        // TODO: реализовать негативный тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldCancelQueuedBuild() {
        // TODO: реализовать тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentBuildCancellation() {
        // TODO: реализовать негативный тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldDeleteFinishedBuild() {
        // TODO: реализовать тест
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentBuildDeletion() {
        // TODO: реализовать негативный тест
    }
}
