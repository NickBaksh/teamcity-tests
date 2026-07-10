package com.teamcity.api.smoke;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.BuildSteps;
import com.teamcity.core.steps.ProjectSteps;
import com.teamcity.core.steps.UserSteps;
import io.qameta.allure.*;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;

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
    private BuildSteps buildSteps;

    @BeforeEach
    void initSteps() {
        projectSteps = new ProjectSteps(adminClient);
        userSteps = new UserSteps(adminClient);
        buildSteps = new BuildSteps(adminClient);
    }

    @Test
    @Order(1)
    @Tag("auth")
    @Tag("security")
    @DisplayName("✅ [SMOKE] Auth with valid credentials → 200")
    @Description("Verifies that admin can authenticate")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Authentication")
    void shouldAuthenticateWithValidCredentials() {
        Response response = adminClient.get("/app/rest/server");

        assertThat(response.statusCode())
                .as("Admin should be authenticated")
                .isEqualTo(200);

        log.info("✅ Authentication successful");
    }

    @Test
    @Order(2)
    @Tag("auth")
    @Tag("security")
    @Tag("negative")
    @DisplayName("❌ [SMOKE] Auth with invalid credentials → 401")
    @Description("Verifies that invalid credentials are rejected")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Authentication")
    public void shouldRejectInvalidCredentials() {
        RestClient invalidClient = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getAdminLogin(), "wrong_password")
                .build();

        assertThatThrownBy(() -> invalidClient.get("/app/rest/server"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Authentication failed")
                .hasMessageContaining("Incorrect username or password");

    }

    @Test
    @Order(3)
    @Tag("projects")
    @Tag("crud")
    @DisplayName("✅ [SMOKE] Create project → 200")
    @Description("Verifies project creation — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Project")
    void shouldCreateProject() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getId()).isNotBlank();
        softly.assertThat(created.getName()).isEqualTo(project.getName());
        softly.assertThat(created.getHref()).isNotBlank();
        softly.assertAll();

        log.info("✅ Project created: {}", created.getName());
    }

    @Test
    @Order(4)
    @Tag("projects")
    @Tag("crud")
    @DisplayName("✅ [SMOKE] Get project by ID → 200")
    @Description("Verifies project retrieval by ID — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Project")
    void shouldGetProjectById() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        trackProject(created.getId());

        Project retrieved = projectSteps.getProject(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(retrieved.getId()).isEqualTo(created.getId());
        softly.assertThat(retrieved.getName()).isEqualTo(created.getName());
        softly.assertAll();

        log.info("✅ Project retrieved: {}", retrieved.getName());
    }

    @Test
    @Order(5)
    @Tag("projects")
    @Tag("crud")
    @DisplayName("✅ [SMOKE] Delete project → 200")
    @Description("Verifies project deletion — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Project")
    void shouldDeleteProject() {
        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);

        projectSteps.deleteProject(created.getId());

        assertThat(projectSteps.projectExists(created.getId())).isFalse();

        log.info("✅ Project deleted: {}", created.getName());
    }

    @Test
    @Order(6)
    @Tag("build-configs")
    @Tag("crud")
    @DisplayName("✅ [SMOKE] Create build config → 200")
    @Description("Verifies build configuration creation — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Build Config")
    void shouldCreateBuildConfig() {
        Project project = dataFactory.createRandomProject();
        Project createdProject = projectSteps.createProject(project);
        trackProject(createdProject.getId());

        BuildConfig config = dataFactory.createRandomBuildConfig(createdProject.getId());
        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getId()).isNotBlank();
        softly.assertThat(created.getName()).isEqualTo(config.getName());
        softly.assertAll();

        log.info("✅ Build config created: {}", created.getName());
    }

    @Test
    @Order(7)
    @Tag("build-configs")
    @Tag("crud")
    @DisplayName("✅ [SMOKE] Delete build config → 200")
    @Description("Verifies build configuration deletion — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Build Config")
    void shouldDeleteBuildConfig() {
        Project project = dataFactory.createRandomProject();
        Project createdProject = projectSteps.createProject(project);
        trackProject(createdProject.getId());

        BuildConfig config = dataFactory.createRandomBuildConfig(createdProject.getId());
        BuildConfig created = buildSteps.createBuildConfig(config);

        buildSteps.deleteBuildConfig(created.getId());

        assertThat(buildSteps.buildConfigExists(created.getId())).isFalse();

        log.info("✅ Build config deleted: {}", created.getName());
    }


    @Test
    @Order(8)
    @Tag("builds")
    @DisplayName("✅ [SMOKE] Run build → 200")
    @Description("Verifies build execution — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Build")
    void shouldRunBuild() {
        Project project = dataFactory.createRandomProject();
        Project createdProject = projectSteps.createProject(project);
        trackProject(createdProject.getId());

        BuildConfig config = dataFactory.createRandomBuildConfig(createdProject.getId());
        BuildConfig createdConfig = buildSteps.createBuildConfig(config);
        trackBuildConfig(createdConfig.getId());

        Build build = buildSteps.runBuild(createdConfig.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(build.getId()).isNotNull();
        softly.assertThat(build.getBuildTypeId()).isNotBlank();
        softly.assertAll();

        log.info("✅ Build started: {}", build.getId());
    }

    @Test
    @Order(9)
    @Tag("builds")
    @DisplayName("✅ [SMOKE] Get build status → 200")
    @Description("Verifies build status retrieval")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Build")
    void shouldGetBuildStatus() {

        Project project = dataFactory.createRandomProject();
        Project createdProject = projectSteps.createProject(project);
        trackProject(createdProject.getId());
        BuildConfig config = dataFactory.createRandomBuildConfig(createdProject.getId());
        BuildConfig createdConfig = buildSteps.createBuildConfig(config);
        trackBuildConfig(createdConfig.getId());
        Build build = buildSteps.runBuild(createdConfig.getId());
        Build currentBuild = buildSteps.getBuild(String.valueOf(build.getId()));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(currentBuild)
                .as("Build should be retrievable")
                .isNotNull();

        softly.assertThat(currentBuild.getId())
                .as("Build ID should match")
                .isEqualTo(build.getId());

        softly.assertThat(currentBuild.getState())
                .as("Build should be queued, running or finished")
                .isIn("queued", "running", "finished");

        softly.assertAll();

        log.info("✅ Build status retrieved: ID={}, State={}",

                currentBuild.getId(), currentBuild.getState());

    }


    @Test
    @Order(10)
    @Tag("users")
    @Tag("auth")
    @DisplayName("✅ [SMOKE] Create user → 200")
    @Description("Verifies user creation — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("User")
    void shouldCreateUser() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getUsername()).isNotBlank();
        softly.assertThat(created.getUsername()).isEqualTo(user.getUsername());
        softly.assertAll();

        log.info("✅ User created: {}", created.getUsername());
    }

    @Test
    @Order(11)
    @Tag("users")
    @Tag("auth")
    @DisplayName("✅ [SMOKE] Get user by username → 200")
    @Description("Verifies user retrieval by username — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("User")
    void shouldGetUserByUsername() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        User retrieved = userSteps.getUser(created.getUsername());

        assertThat(retrieved.getUsername()).isEqualTo(created.getUsername());

        log.info("✅ User retrieved: {}", retrieved.getUsername());
    }

    @Test
    @Order(12)
    @Tag("users")
    @Tag("auth")
    @DisplayName("✅ [SMOKE] Delete user → 200")
    @Description("Verifies user deletion — critical path")
    @Severity(SeverityLevel.BLOCKER)
    @Story("User")
    void shouldDeleteUser() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);

        userSteps.deleteUser(created.getUsername());

        assertThatThrownBy(() -> userSteps.getUser(created.getUsername()))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

        log.info("✅ User deleted: {}", created.getUsername());
    }
}