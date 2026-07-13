package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ClientFactory;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AdminSteps {

    private final ApiClient client;
    private final ResponseValidator validator;
    private final ProjectSteps projectSteps;
    private final BuildConfigSteps buildConfigSteps;
    private final BuildRunSteps buildRunSteps;
    private final UserSteps userSteps;
    private final AuthSteps authSteps;

    public AdminSteps(ApiClient client) {
        this.client = client;
        this.validator = new ResponseValidator();
        this.projectSteps = new ProjectSteps(client, validator);
        this.buildConfigSteps = new BuildConfigSteps(client, validator);
        this.buildRunSteps = new BuildRunSteps(client, validator);
        this.userSteps = new UserSteps(client, validator);
        this.authSteps = new AuthSteps(client, validator);
    }

    public ProjectSteps projects() {
        return projectSteps;
    }

    public BuildConfigSteps buildConfigs() {
        return buildConfigSteps;
    }

    public BuildRunSteps builds() {
        return buildRunSteps;
    }

    public UserSteps users() {
        return userSteps;
    }

    public AuthSteps auth() {
        return authSteps;
    }

    @Step("Create project ready for build configs")
    public Project createProjectForBuilds(Project project) {
        return projectSteps.createProjectSmart(project);
    }

    @Step("Create project with build config")
    public ProjectWithBuildConfig createProjectWithBuildConfig(Project project, BuildConfig buildConfig) {
        Project createdProject = projectSteps.createProjectSmart(project);
        buildConfig.setProjectId(createdProject.getId());
        BuildConfig createdConfig = buildConfigSteps.createBuildConfig(buildConfig);
        return new ProjectWithBuildConfig(createdProject, createdConfig);
    }

    @Step("Create project, build config and trigger build")
    public ProjectBuildRun createProjectBuildAndRun(Project project, BuildConfig buildConfig) {
        ProjectWithBuildConfig setup = createProjectWithBuildConfig(project, buildConfig);
        Build build = buildRunSteps.runBuild(setup.getBuildConfig().getId());
        return new ProjectBuildRun(setup.getProject(), setup.getBuildConfig(), build);
    }

    @Step("Create user and obtain auth token: {user.username}")
    public CreatedUserWithToken createUserWithToken(User user) {
        User created = userSteps.createUser(user);
        String tokenName = "api-token-" + System.currentTimeMillis();

        Response response = client.post(
                Endpoint.USER_TOKENS.getPath(),
                Map.of("name", tokenName),
                created.getUsername()
        );
        validator.validateStatus(response);

        String token = extractToken(response);
        log.info("Created user {} with token {}", created.getUsername(), tokenName);
        return new CreatedUserWithToken(created, token, tokenName);
    }

    @Step("Create authenticated client for user: {username}")
    public ApiClient createClientForUser(String username, String password) {
        return ClientFactory.createBasicAuthClient(username, password);
    }

    @Step("Create bearer client for token")
    public ApiClient createClientForToken(String token) {
        return ClientFactory.createBearerClient(token);
    }

    private String extractToken(Response response) {
        String value = response.jsonPath().getString("value");
        if (value != null && !value.isBlank()) {
            return value;
        }
        String body = response.asString();
        if (body != null && !body.isBlank() && !body.trim().startsWith("{")) {
            return body.trim();
        }
        throw new IllegalStateException("Token value not found in response: " + body);
    }

    @Getter
    public static final class CreatedUserWithToken {
        private final User user;
        private final String token;
        private final String tokenName;

        public CreatedUserWithToken(User user, String token, String tokenName) {
            this.user = user;
            this.token = token;
            this.tokenName = tokenName;
        }
    }

    @Getter
    public static final class ProjectWithBuildConfig {
        private final Project project;
        private final BuildConfig buildConfig;

        public ProjectWithBuildConfig(Project project, BuildConfig buildConfig) {
            this.project = project;
            this.buildConfig = buildConfig;
        }
    }

    @Getter
    public static final class ProjectBuildRun {
        private final Project project;
        private final BuildConfig buildConfig;
        private final Build build;

        public ProjectBuildRun(Project project, BuildConfig buildConfig, Build build) {
            this.project = project;
            this.buildConfig = buildConfig;
            this.build = build;
        }
    }
}
