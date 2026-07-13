package com.teamcity.api.user;

import com.teamcity.api.BaseApiTest;
import com.teamcity.api.specs.ResponseSpecs;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.BuildSteps;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserBuildsTest extends BaseApiTest {

    @Test
    public void userCanRunBuildOfAdminProjectTest() {
        Project project = projectSteps(adminClient()).createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        User user = userSteps(adminClient()).createRandomUser();

        BuildSteps userSteps = buildSteps(userClient(user));

        Build build = userSteps.runBuild(buildConfig.getId());

        assertAll(
                () -> assertNotNull(build.getId()),
                () -> assertEquals(buildConfig.getId(), build.getBuildTypeId()));
    }

    @Test
    public void userCanNotRunNotExistBuildTest() {

        User user = userSteps(adminClient()).createRandomUser();

        Response response = buildSteps(userNegativeClient(user))
                .runBuildForbidden(dataFactory.generateNotExistingBuildConfigId());

        response.then().spec(ResponseSpecs.returnsNotFound());
    }

    @Test
    public void userCanGetBuildStatusTest() {
        Project project = projectSteps(adminClient()).createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        userSteps(adminClient()).createRandomUser();

        Build build = buildSteps(adminClient())
                .runBuildAndWait(buildConfig.getId());

        assertAll(
                () -> assertNotNull(build.getState()),
                () -> assertNotNull(build.getStatus()),
                () -> assertEquals(BuildSteps.STATE_FINISHED, build.getState()),
                () -> assertEquals(BuildSteps.STATUS_SUCCESS, build.getStatus())
        );
    }

    @Test
    public void userCanCancelRunningBuildTest() {
        Project project = projectSteps(adminClient()).createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        User user = userSteps(adminClient()).createRandomUser();

        BuildSteps buildSteps = buildSteps(userClient(user));

        Build build = buildSteps.runBuild(buildConfig.getId());

        buildSteps.waitForBuildState(build.getId(), BuildSteps.STATE_RUNNING);

        buildSteps.cancelBuild(build.getId());

        Build cancelledBuild =
                buildSteps.waitForBuildState(build.getId(), BuildSteps.STATE_FINISHED);

        assertAll(
                () -> assertEquals(BuildSteps.STATUS_UNKNOWN, cancelledBuild.getStatus()),
                () -> assertEquals(BuildSteps.STATUS_TEXT_CANCELED, cancelledBuild.getStatusText())
        );
    }

    @Test
    public void userCanCancelAnotherUsersBuildTest() {

        Project project = projectSteps(adminClient()).createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        User user1 = userSteps(adminClient()).createRandomUser();
        User user2 = userSteps(adminClient()).createRandomUser();

        BuildSteps user1Steps = buildSteps(userClient(user1));
        BuildSteps user2Steps = buildSteps(userClient(user2));

        Build build = user1Steps.runBuild(buildConfig.getId());

        user1Steps.waitForBuildState(build.getId(), BuildSteps.STATE_RUNNING);

        user2Steps.cancelBuild(build.getId());

        Build cancelledBuild =
                user1Steps.waitForBuildState(build.getId(), BuildSteps.STATE_FINISHED);

        assertAll(
                () -> assertEquals(BuildSteps.STATE_FINISHED, cancelledBuild.getState()),
                () -> assertEquals(BuildSteps.STATUS_UNKNOWN, cancelledBuild.getStatus()),
                () -> assertEquals(BuildSteps.STATUS_TEXT_CANCELED, cancelledBuild.getStatusText())
        );
    }
    // Проверяет ограничение прав.
    @Test
    public void userCanNotDeleteOwnFinishedBuildTest() {

        Project project = projectSteps(adminClient()).createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        User user = userSteps(adminClient()).createRandomUser();

        BuildSteps buildSteps = buildSteps(userClient(user));

        Build build = buildSteps.runBuild(buildConfig.getId());

        Build finishedBuild = buildSteps.waitForBuildFinish(build.getId());

        Response response = buildSteps(userNegativeClient(user))
                .deleteBuildForbidden(finishedBuild.getId());

        response.then().spec(ResponseSpecs.returnsForbidden());

        Build actualBuild = buildSteps.getBuild(finishedBuild.getId());

        assertAll(
                () -> assertNotNull(actualBuild),
                () -> assertEquals(finishedBuild.getId(), actualBuild.getId()),
                () -> assertEquals(BuildSteps.STATE_FINISHED, actualBuild.getState()),
                () -> assertEquals(BuildSteps.STATUS_SUCCESS, actualBuild.getStatus())
        );
    }

    @Test
    public void userCanGetOwnBuildDetailsTest() {
        Project project = projectSteps(adminClient()).createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        User user = userSteps(adminClient()).createRandomUser();

        BuildSteps buildSteps = buildSteps(userClient(user));

        Build build = buildSteps.runBuild(buildConfig.getId());

        Build finishedBuild = buildSteps.waitForBuildFinish(build.getId());

        Build buildDetails = buildSteps.getBuild(finishedBuild.getId());

        assertAll(
                () -> assertEquals(finishedBuild.getId(), buildDetails.getId()),
                () -> assertEquals(buildConfig.getId(), buildDetails.getBuildTypeId()),
                () -> assertEquals(BuildSteps.STATE_FINISHED, buildDetails.getState()),
                () -> assertEquals(BuildSteps.STATUS_SUCCESS, buildDetails.getStatus())
        );
    }
}