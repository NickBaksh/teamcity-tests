package com.teamcity.api.user;

import com.teamcity.api.BaseApiTest;
import com.teamcity.api.specs.ResponseSpecs;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserBuildsTest extends BaseApiTest {

    @Test
    @DisplayName("Юзер может запустить сборку админа")
    public void userCanRunBuildOfAdminProjectTest() {
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));
        // создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        // создаем клиента под этим пользователем
        ApiClient client = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .build();
        //запускаем билд под пользователем
        BuildSteps userSteps = new BuildSteps(client);
        Build build = userSteps.runBuild(buildConfig.getId());

        assertAll(
                () -> assertNotNull(build.getId()),
                () -> assertEquals(buildConfig.getId(), build.getBuildTypeId())
        );
    }

    @Test
    @DisplayName("Юзер не может запустить несуществующую сборку")
    public void userCanNotRunNotExistBuildTest() {
        // создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        // создаем негативного клиента
        ApiClient client = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .forNegativeTest()
                .build();

        Response response = client.post(
                Endpoint.BUILD_QUEUE.getPath(),
                RunBuildRequest.builder()
                        .buildTypeId(dataFactory.generateNotExistingBuildConfigId())
                        .build()
        );

        response.then().spec(ResponseSpecs.returnsNotFound());
    }

    @Test
    @DisplayName("Юзер может получить статус сборки")
    public void userCanGetBuildStatusTest() {
        // Создаем проект и билд-конфиг
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));

        // Создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        // Создаем клиента под пользователем
        ApiClient client = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .build();

        BuildSteps userSteps = new BuildSteps(client);
        // Запускаем сборку
        Build build = userSteps.runBuild(buildConfig.getId());
        Build finishedBuild =
                userSteps.waitForBuildFinish(String.valueOf(build.getId()));

        assertAll(
                () -> assertNotNull(finishedBuild.getState()),
                () -> assertNotNull(finishedBuild.getStatus()),
                () -> assertEquals("finished", finishedBuild.getState()),
                () -> assertEquals("SUCCESS", finishedBuild.getStatus())
        );
    }

    @Test
    @DisplayName("Юзер может получить детали сборки")
    public void userCanGetOwnBuildDetailsTest() {
        // Создаем проект и билд-конфиг
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));

        // Создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        // Клиент пользователя
        ApiClient client = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .build();

        BuildSteps userSteps = new BuildSteps(client);

        // Запускаем сборку
        Build build = userSteps.runBuild(buildConfig.getId());
        Build finishedBuild =
                userSteps.waitForBuildFinish(String.valueOf(build.getId()));

        // Получаем детали сборки
        Build buildDetails = userSteps.getBuild(String.valueOf(finishedBuild.getId()));

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
        // Создаем проект и билд-конфиг
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));

        // Создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        ApiClient client = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .build();

        BuildSteps userSteps = new BuildSteps(client);

        // Запускаем сборку
        Build build = userSteps.runBuild(buildConfig.getId());

        // Ждем, пока сборка перейдет в состояние running
        userSteps.waitForBuildState(
                String.valueOf(build.getId()),
                "running",
                30
        );

        // Отменяем сборку
        userSteps.cancelBuild(String.valueOf(build.getId()));

        // Ждем завершения отмены
        Build cancelledBuild = userSteps.waitForBuildState(
                String.valueOf(build.getId()),
                "finished",
                30
        );

        // Проверяем результат
        assertAll(
                () -> assertEquals("finished", cancelledBuild.getState()),
                () -> assertEquals("UNKNOWN", cancelledBuild.getStatus()),
                () -> assertTrue(cancelledBuild.getStatusText().toLowerCase().contains("cancel"))
        );
    }

    @Test
    @DisplayName("Юзер не может отменить чужую сборку")
    //Это тест на запрет отмены чужой сборки. Но TeamCity возвращает 200 OK и успешно отменяет сборку вторым пользователем.
    // Поэтому я пока убрала специальный метод cancelBuildForbidden() и хочу уточнить, должен ли этот сценарий вообще существовать?
    public void userCanNotCancelAnotherUserBuildTest() {

        // Создаем проект и билд-конфигурацию
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));

        // Создаем первого пользователя
        User user1 = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user1);

        // Создаем второго пользователя
        User user2 = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user2);

        // Клиент первого пользователя
        ApiClient client1 = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user1.getUsername(), user1.getPassword())
                .build();

        // Клиент второго пользователя
        ApiClient client2 = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user2.getUsername(), user2.getPassword())
                .forNegativeTest()
                .build();

        BuildSteps firstUserSteps = new BuildSteps(client1);
        BuildSteps secondUserSteps = new BuildSteps(client2);

        // Первый пользователь запускает сборку
        Build build = firstUserSteps.runBuild(buildConfig.getId());

        firstUserSteps.waitForBuildState(
                String.valueOf(build.getId()),
                "running",
                30
        );

        // Второй пользователь пытается отменить чужую сборку
        Response response = secondUserSteps.cancelBuildForbidden(
                String.valueOf(build.getId())
        );
        assertThat(response.statusCode()).isEqualTo(403);

        // Проверяем, что сборка не была отменена
        Build actualBuild = firstUserSteps.getBuild(
                String.valueOf(build.getId())
        );

        assertThat(actualBuild.getStatusText())
                .doesNotContain("Canceled");
    }
    // Проверяет ограничение прав.
    @Test
    @DisplayName("Юзер не может удалить свою завершённую сборку без прав админа")
    public void userCanNotDeleteOwnFinishedBuildTest() {
        // Создаем проект и Build Config
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));

        // Создаем пользователя
        User user = dataFactory.createRandomUser();
        new UserSteps(adminClient).createUser(user);

        // Клиент пользователя для запуска сборки
        ApiClient userClient = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .build();

        BuildSteps userSteps = new BuildSteps(userClient);

        // Запускаем сборку
        Build build = userSteps.runBuild(buildConfig.getId());

        // Ждем завершения
        Build finishedBuild = userSteps.waitForBuildFinish(
                String.valueOf(build.getId())
        );

        // Негативный клиент того же пользователя
        ApiClient negativeClient = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .forNegativeTest()
                .build();

        BuildSteps negativeSteps = new BuildSteps(negativeClient);

        // Пытаемся удалить сборку
        Response response = negativeSteps.deleteBuildForbidden(
                String.valueOf(finishedBuild.getId())
        );

        response.then().spec(ResponseSpecs.returnsForbidden());

        // Проверяем, что сборка не исчезла
        Build actualBuild = userSteps.getBuild(
                String.valueOf(finishedBuild.getId())
        );

        assertAll(
                () -> assertNotNull(actualBuild),
                () -> assertEquals(finishedBuild.getId(), actualBuild.getId()),
                () -> assertEquals("finished", actualBuild.getState()),
                () -> assertEquals("SUCCESS", actualBuild.getStatus())
        );
    }

    // Не существует ручки для получения лога. Тест несуществуюшего API
//    @Test
//    @DisplayName("Получить лог своей сборки")
//    public void userCanGetOwnBuildLogTest() {
//        // Expected: 200 OK
//    }
}