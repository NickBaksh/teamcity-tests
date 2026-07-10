package com.teamcity.api.user;

import com.teamcity.api.BaseApiTest;
import com.teamcity.api.specs.ResponseSpecs;
import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.BuildSteps;
import com.teamcity.core.steps.UserSteps;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserBuildConfigsTest extends BaseApiTest {
    @Test
    @DisplayName("Юзер может получить Build Config по ID")
    public void userCanGetBuildConfigByIdTest(){
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig expected = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));

        // создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        // создаем клиента под этим пользователем
        ApiClient client = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .withRetry(ConfigManager.getRetryCount())
                .build();

        BuildSteps userSteps = new BuildSteps(client);
        BuildConfig actual = userSteps.getBuildConfig(expected.getId());

        assertAll(
                () -> assertEquals(expected.getId(), actual.getId()),
                () -> assertEquals(expected.getName(), actual.getName()),
                () -> assertEquals(expected.getProjectId(), actual.getProjectId())
        );
    }

    @Test
    @DisplayName("Юзер не может создать Build Config")
    public void userCanNotCreateBuildConfigTest(){
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = dataFactory.createRandomBuildConfig(
                project.getId());
        // создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        // создаем негативного клиента под этим пользователем
        ApiClient client = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .forNegativeTest()
                .build();

        Response response = client.post(
                Endpoint.BUILD_TYPES.getPath(),
                buildConfig);
        response.then().spec(ResponseSpecs.returnsForbidden());
    }

    @Test
    @DisplayName("Юзер не может удалить Build Config")
    public void userCanNotDeleteBuildConfigTest(){
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));

        // создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        // создаем негативного клиента под этим пользователем
        ApiClient client = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .forNegativeTest()
                .build();

        Response response = client.delete(
                Endpoint.BUILD_TYPE.format(buildConfig.getId())
        );
        response.then().spec(ResponseSpecs.returnsForbidden());
    }
}