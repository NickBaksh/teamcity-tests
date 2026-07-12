package com.teamcity.api.smoke;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.BuildConfigSteps;
import com.teamcity.core.steps.BuildSteps;
import com.teamcity.core.steps.ProjectSteps;
import com.teamcity.core.steps.UserSteps;
import io.qameta.allure.*;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import com.teamcity.core.exceptions.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@Epic("Smoke Tests")
@Feature("Critical Path")
@Tag("smoke")
@Tag("critical")
@Tag("positive")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SmokeTest extends BaseApiTest {

    private ProjectSteps projectSteps;
    private UserSteps userSteps;
    private BuildConfigSteps buildConfigSteps;
    private BuildSteps buildSteps;

    @BeforeEach
    void initSteps() {
        projectSteps = projectSteps(adminClient());
        userSteps = userSteps(adminClient());
        buildConfigSteps = buildConfigSteps(adminClient());
        buildSteps = buildSteps(adminClient());
    }

    @Test
    @Order(1)
    @DisplayName("✅ [SMOKE] Auth with valid credentials → 200")
    void shouldAuthenticateWithValidCredentials() {

        Response response = adminClient().get("/app/rest/server");

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @Order(2)
    @DisplayName("❌ [SMOKE] Auth with invalid credentials → 401")
    void shouldRejectInvalidCredentials() {

        RestClient invalidClient = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getAdminLogin(), "wrong_password")
                .forNegativeTest()
                .build();

        assertThatThrownBy(() -> invalidClient.get("/app/rest/server"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @Order(3)
    @DisplayName("✅ [SMOKE] Create project")
    void shouldCreateProject() {

        Project created = projectSteps.createRandomProject();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(created.getId()).isNotBlank();
        softly.assertThat(created.getName()).isNotBlank();
        softly.assertThat(created.getHref()).isNotBlank();

        softly.assertAll();
    }

    @Test
    @Order(4)
    @DisplayName("✅ [SMOKE] Get project")
    void shouldGetProjectById() {

        Project expected = projectSteps.createRandomProject();

        Project actual = projectSteps.getProject(expected.getId());

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
    }

    @Test
    @Order(5)
    @DisplayName("✅ [SMOKE] Delete project")
    void shouldDeleteProject() {

        Project project = projectSteps.createRandomProject();

        projectSteps.deleteProject(project.getId());

        assertThat(projectSteps.projectExists(project.getId()))
                .isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("✅ [SMOKE] Create build config")
    void shouldCreateBuildConfig() {

        Project project = projectSteps.createRandomProject();

        BuildConfig created =
                buildConfigSteps.createRandomBuildConfig(project);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(created.getId()).isNotBlank();
        softly.assertThat(created.getName()).isNotBlank();
        softly.assertThat(created.getProjectId())
                .isEqualTo(project.getId());

        softly.assertAll();
    }

    @Test
    @Order(7)
    @DisplayName("✅ [SMOKE] Delete build config")
    void shouldDeleteBuildConfig() {

        Project project = projectSteps.createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps.createRandomBuildConfig(project);

        buildConfigSteps.delete(buildConfig);

        assertThat(buildConfigSteps.exists(buildConfig))
                .isFalse();
    }

    @Test
    @Order(8)
    @DisplayName("✅ [SMOKE] Run build")
    void shouldRunBuild() {

        Project project = projectSteps.createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps.createRandomBuildConfig(project);

        Build build = buildSteps.runBuild(buildConfig.getId());

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(build.getId()).isNotNull();
        softly.assertThat(build.getBuildTypeId()).isNotBlank();

        softly.assertAll();
    }

    @Test
    @Order(9)
    @DisplayName("✅ [SMOKE] Get build status")
    void shouldGetBuildStatus() {

        Project project = projectSteps.createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps.createRandomBuildConfig(project);

        Build startedBuild =
                buildSteps.runBuild(buildConfig.getId());

        Build actual =
                buildSteps.getBuild(String.valueOf(startedBuild.getId()));

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(actual.getStatus()).isNotNull();
        softly.assertThat(actual.getState()).isNotNull();

        softly.assertAll();
    }


    @Test
    @Order(10)
    @DisplayName("✅ [SMOKE] Create user")
    void shouldCreateUser() {

        User created =
                userSteps.createRandomUser();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(created.getUsername()).isNotBlank();
        softly.assertThat(created.getName()).isNotBlank();

        softly.assertAll();
    }

    @Test
    @Order(11)
    @DisplayName("✅ [SMOKE] Get user")
    void shouldGetUserByUsername() {

        User expected =
                userSteps.createRandomUser();

        User actual =
                userSteps.getUser(expected.getUsername());

        assertThat(actual.getUsername())
                .isEqualTo(expected.getUsername());
    }

    @Test
    @Order(12)
    @DisplayName("✅ [SMOKE] Delete user")
    void shouldDeleteUser() {

        User user =
                userSteps.createRandomUser();

        userSteps.deleteUser(user.getUsername());

        assertThatThrownBy(() ->
                userSteps.getUser(user.getUsername()))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);
    }
}