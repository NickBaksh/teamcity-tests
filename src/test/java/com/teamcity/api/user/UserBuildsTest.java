package com.teamcity.api.user;

import com.teamcity.api.BaseApiTest;
import com.teamcity.api.TestListener;
import com.teamcity.api.specs.ResponseSpecs;
import com.teamcity.core.cleanup.CleanupExtension;
import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.models.dto.RunBuildRequest;
import com.teamcity.core.steps.BuildSteps;
import com.teamcity.core.steps.UserSteps;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserBuildsTest extends BaseApiTest {

    @Test
    public void userCanRunBuildOfAdminProjectTest() {
        Project project = projectSteps(adminClient()).createRandomProject();
//        Project project = adminProjectSteps.createProject(
//                dataFactory.createRandomProject());

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

//        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
//                dataFactory.createRandomBuildConfig(project.getId()));
        // создаем пользователя
        User user = userSteps(adminClient()).createRandomUser();
        System.out.println(user.getUsername());

        System.out.println(user.getPassword());
//        User user = dataFactory.createRandomUser();
//        new UserSteps(adminClient).createUser(user);

        // создаем клиента под этим пользователем
        BuildSteps userSteps = buildSteps(userClient(user));
//        ApiClient client = RestClient.builder()
//                .baseUrl(ConfigManager.getApiBaseUrl())
//                .basicAuth(user.getUsername(), user.getPassword())
//                .build();
//        //запускаем билд под пользователем
//        BuildSteps userSteps = new BuildSteps(client);
        //запускаем сборку
        Build build = userSteps.runBuild(buildConfig.getId());

        assertAll(
                () -> assertNotNull(build.getId()),
                () -> assertEquals(buildConfig.getId(), build.getBuildTypeId()));
    }

    @Test
    @DisplayName("Юзер не может запустить несуществующую сборку")
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
                () -> assertEquals("finished", build.getState()),
                () -> assertEquals("SUCCESS", build.getStatus())
        );
    }

    @Test
    @DisplayName("Юзер может получить детали сборки")
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
                () -> assertEquals("finished", buildDetails.getState()),
                () -> assertEquals("SUCCESS", buildDetails.getStatus())
        );
    }

    @Test
    @DisplayName("Юзер может отменить выполняющуюся сборку в очереди")
    public void userCanCancelRunningBuildTest() {
        Project project = projectSteps(adminClient()).createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        User user = userSteps(adminClient()).createRandomUser();

        BuildSteps buildSteps = buildSteps(userClient(user));

        Build build = buildSteps.runBuild(buildConfig.getId());

        buildSteps.waitForBuildState(build.getId(), "running", 30);

        buildSteps.cancelBuild(build.getId().toString());

        Build cancelledBuild =
                buildSteps.waitForBuildState(build.getId(), "finished", 30);

        assertAll(
                () -> assertEquals("finished", cancelledBuild.getState()),
                () -> assertEquals("UNKNOWN", cancelledBuild.getStatus()),
                () -> assertTrue(cancelledBuild.getStatusText().toLowerCase().contains("cancel"))
        );
    }

    @Test
    @DisplayName("Юзер может отменить чужую сборку")
    //проверить по документации какое ожидаемое поведение на самом деле и относительно  этого уже позитив или негати
    //Это тест на запрет отмены чужой сборки. Но TeamCity возвращает 200 OK и успешно отменяет сборку вторым пользователем.
    // Поэтому я пока убрала специальный метод cancelBuildForbidden() и хочу уточнить, должен ли этот сценарий вообще существовать?
    public void userCanCancelAnotherUserBuildTest() {

        Project project = projectSteps(adminClient()).createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        User user1 = userSteps(adminClient()).createRandomUser();
        User user2 = userSteps(adminClient()).createRandomUser();

        BuildSteps user1Steps = buildSteps(userClient(user1));
        BuildSteps user2Steps = buildSteps(userClient(user2));

        Build build = user1Steps.runBuild(buildConfig.getId());

        user1Steps.waitForBuildState(build.getId(), "running", 30);

        Response response = user2Steps.cancelBuildForbidden(build.getId());

        assertThat(response.statusCode()).isEqualTo(403);

        Build actualBuild = user1Steps.getBuild(build.getId());

        assertThat(actualBuild.getStatusText())
                .doesNotContain("Canceled");
    }
    // Проверяет ограничение прав.
    @Test
    @DisplayName("Юзер не может удалить свою завершённую сборку без прав админа")
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
                () -> assertEquals("finished", actualBuild.getState()),
                () -> assertEquals("SUCCESS", actualBuild.getStatus())
        );
    }
}