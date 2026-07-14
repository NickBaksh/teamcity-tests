package com.teamcity.api.smoke;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.AdminSteps;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("smoke")
public class SmokeTest extends BaseApiTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldAllowAdminAccess() {
        authSteps.verifyServerAccessible();
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldRejectInvalidCredentials() {
        authSteps.verifyInvalidAuthRejected();
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldManageProjectLifecycle() {
        Project request = dataFactory.createRandomProject();

        Project created = projectSteps.createProject(request);
        trackProject(created.getId());
        Project retrieved = projectSteps.getProject(created.getId());

        ApiAssertions.assertProjectCreated(request, created);
        ApiAssertions.assertProjectsEqual(created, retrieved);

        projectSteps.deleteProject(created.getId());

        assertThat(projectSteps.projectExists(created.getId())).isFalse();
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldManageBuildConfigLifecycle() {
        Project project = givenProject();
        BuildConfig request = dataFactory.createRandomBuildConfig(project.getId());

        BuildConfig created = buildConfigSteps.createBuildConfig(request);
        trackBuildConfig(created.getId());

        ApiAssertions.assertBuildConfigCreated(request, created);

        buildConfigSteps.deleteBuildConfig(created.getId());

        assertThat(buildConfigSteps.buildConfigExists(created.getId())).isFalse();
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldTriggerAndReadBuild() {
        AdminSteps.ProjectBuildRun run = adminSteps.createProjectBuildAndRun(
                dataFactory.createRandomProject(),
                dataFactory.createRandomBuildConfig(null)
        );
        trackProject(run.getProject().getId());
        trackBuildConfig(run.getBuildConfig().getId());

        Build current = buildRunSteps.getBuild(String.valueOf(run.getBuild().getId()));

        ApiAssertions.assertBuildTriggered(run.getBuild());
        ApiAssertions.assertBuildState(
                current,
                TestDataValues.BUILD_STATE_QUEUED,
                TestDataValues.BUILD_STATE_RUNNING,
                TestDataValues.BUILD_STATE_FINISHED);
        assertThat(current.getId()).isEqualTo(run.getBuild().getId());
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldManageUserLifecycle() {
        User request = dataFactory.createRandomUser();

        User created = userSteps.createUser(request);
        trackUser(created.getUsername());
        User retrieved = userSteps.getUser(created.getUsername());

        ApiAssertions.assertUserCreated(request, created);
        ApiAssertions.assertUsersEqual(created, retrieved);

        userSteps.deleteUser(created.getUsername());

        ApiAssertions.assertNotFound(() -> userSteps.getUser(created.getUsername()));
    }
}
