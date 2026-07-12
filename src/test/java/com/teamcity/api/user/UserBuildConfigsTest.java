package com.teamcity.api.user;

import com.teamcity.api.BaseApiTest;
import com.teamcity.api.specs.ResponseSpecs;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserBuildConfigsTest extends BaseApiTest {

    @Test
    @DisplayName("Юзер может получить Build Config по ID")
    public void userCanGetBuildConfigByIdTest() {

        Project project = projectSteps(adminClient())
                .createRandomProject();

        BuildConfig expected = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        User user = userSteps(adminClient())
                .createRandomUser();

        BuildConfig actual = buildConfigSteps(userClient(user))
                .get(expected.getId());

        assertAll(
                () -> assertEquals(expected.getId(), actual.getId()),
                () -> assertEquals(expected.getName(), actual.getName()),
                () -> assertEquals(expected.getProjectId(), actual.getProjectId())
        );
    }

    @Test
    @DisplayName("Юзер не может создать Build Config")
    public void userCanNotCreateBuildConfigTest() {

        Project project = projectSteps(adminClient())
                .createRandomProject();

        BuildConfig buildConfig =
                dataFactory.createRandomBuildConfig(project.getId());

        User user = userSteps(adminClient())
                .createRandomUser();

        Response response = userNegativeClient(user).post(
                Endpoint.BUILD_TYPES.getPath(),
                buildConfig
        );

        response.then().spec(ResponseSpecs.returnsForbidden());
    }

    @Test
    @DisplayName("Юзер не может удалить Build Config")
    public void userCanNotDeleteBuildConfigTest() {

        Project project = projectSteps(adminClient())
                .createRandomProject();

        BuildConfig buildConfig = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        User user = userSteps(adminClient())
                .createRandomUser();

        Response response = userNegativeClient(user).delete(
                Endpoint.BUILD_TYPE.format(buildConfig.getId())
        );

        response.then().spec(ResponseSpecs.returnsForbidden());
    }
}