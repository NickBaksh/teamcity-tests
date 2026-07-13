package com.teamcity.api;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ClientFactory;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.AdminSteps;
import com.teamcity.core.steps.AuthSteps;
import com.teamcity.core.steps.BuildConfigSteps;
import com.teamcity.core.steps.BuildRunSteps;
import com.teamcity.core.steps.ProjectSteps;
import com.teamcity.core.steps.UserSteps;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ExtendWith(TestListener.class)
public abstract class BaseApiTest {

    protected ApiClient adminClient;
    protected ApiClient userClient;
    protected TestDataFactory dataFactory;

    protected AdminSteps adminSteps;
    protected AuthSteps authSteps;
    protected ProjectSteps projectSteps;
    protected BuildConfigSteps buildConfigSteps;
    protected BuildRunSteps buildRunSteps;
    protected UserSteps userSteps;

    private final List<String> createdProjects = new ArrayList<>();
    private final List<String> createdUsers = new ArrayList<>();
    private final List<String> createdBuildConfigs = new ArrayList<>();

    @BeforeEach
    @Step("Initialize API test environment")
    public void setUp() {
        log.info("Setting up API test...");
        adminClient = ClientFactory.createAdminClient();
        userClient = ClientFactory.createUserClient();
        dataFactory = new TestDataFactory();

        adminSteps = new AdminSteps(adminClient);
        authSteps = adminSteps.auth();
        projectSteps = adminSteps.projects();
        buildConfigSteps = adminSteps.buildConfigs();
        buildRunSteps = adminSteps.builds();
        userSteps = adminSteps.users();
    }

    @AfterEach
    @Step("Cleanup test resources")
    public void cleanUp() {
        createdBuildConfigs.forEach(id -> {
            try {
                buildConfigSteps.deleteBuildConfigIfExists(id);
            } catch (Exception e) {
                log.warn("Failed to cleanup build config {}: {}", id, e.getMessage());
            }
        });
        createdBuildConfigs.clear();

        createdProjects.forEach(id -> {
            try {
                projectSteps.deleteProjectIfExists(id);
            } catch (Exception e) {
                log.warn("Failed to cleanup project {}: {}", id, e.getMessage());
            }
        });
        createdProjects.clear();

        createdUsers.forEach(username -> {
            try {
                userSteps.deleteUserIfExists(username);
            } catch (Exception e) {
                log.warn("Failed to cleanup user {}: {}", username, e.getMessage());
            }
        });
        createdUsers.clear();
    }

    @Step("Create tracked project")
    protected Project givenProject() {
        Project created = projectSteps.createProject(dataFactory.createRandomProject());
        trackProject(created.getId());
        return created;
    }

    @Step("Create tracked project from request")
    protected Project givenProject(Project request) {
        Project created = projectSteps.createProject(request);
        trackProject(created.getId());
        return created;
    }

    @Step("Create tracked build config in project: {projectId}")
    protected BuildConfig givenBuildConfig(String projectId) {
        BuildConfig created = buildConfigSteps.createBuildConfig(dataFactory.createRandomBuildConfig(projectId));
        trackBuildConfig(created.getId());
        return created;
    }

    @Step("Create tracked build config from request")
    protected BuildConfig givenBuildConfig(BuildConfig request) {
        BuildConfig created = buildConfigSteps.createBuildConfig(request);
        trackBuildConfig(created.getId());
        return created;
    }

    @Step("Create tracked user")
    protected User givenUser() {
        User created = userSteps.createUser(dataFactory.createRandomUser());
        trackUser(created.getUsername());
        return created;
    }

    @Step("Create tracked user from request")
    protected User givenUser(User request) {
        User created = userSteps.createUser(request);
        trackUser(created.getUsername());
        return created;
    }

    @Step("Track project for cleanup: {projectId}")
    protected void trackProject(String projectId) {
        if (projectId != null && !projectId.isEmpty()) {
            createdProjects.add(projectId);
        }
    }

    @Step("Track user for cleanup: {username}")
    protected void trackUser(String username) {
        if (username != null && !username.isEmpty()) {
            createdUsers.add(username);
        }
    }

    @Step("Track build config for cleanup: {configId}")
    protected void trackBuildConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            createdBuildConfigs.add(configId);
        }
    }
}
