package com.teamcity.api;

import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ClientFactory;
import com.teamcity.core.models.*;
import com.teamcity.core.steps.*;
import com.teamcity.core.testdata.TestDataValues;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    protected VcsRootSteps vcsRootSteps;
    protected BuildRunSteps buildRunSteps;
    protected UserSteps userSteps;
    protected ArtifactSteps artifactSteps;
    protected AgentSteps agentSteps;

    private final List<String> createdProjects = new ArrayList<>();
    private final List<String> createdUsers = new ArrayList<>();
    private final List<String> createdBuildConfigs = new ArrayList<>();
    private final List<String> createdVcsRoots = new ArrayList<>();

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
        artifactSteps = adminSteps.artifacts();
        agentSteps = adminSteps.agents();
        vcsRootSteps = adminSteps.vcs();
    }

    @AfterEach
    @Step("Cleanup test resources")
    public void cleanUp() {
        createdVcsRoots.forEach(id -> {
            try {
                vcsRootSteps.deleteVcsRootIfExists(id);
            } catch (Exception e) {
                log.warn("Failed to cleanup VCS Root {}: {}", id, e.getMessage());
            }
        });
        createdVcsRoots.clear();

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

    @Step("Create tracked VCS Root in project: {projectId}")
    protected VcsRoot givenVcsRoot(String projectId) {
        VcsRootConfig config = dataFactory.createRandomVcsRootConfig(projectId);

        VcsRoot created = vcsRootSteps.createVcsRoot(config);
        trackVcsRoot(created.getId());
        return created;
    }

    @Step("Create tracked VCS Root with empty URL in project: {projectId}")
    protected VcsRoot givenVcsRootWithEmptyUrl(String projectId) {
        VcsRootConfig config = dataFactory.createVcsRootConfigWithEmptyUrl(projectId);

        VcsRoot created = vcsRootSteps.createVcsRoot(config);
        trackVcsRoot(created.getId());
        return created;
    }

    @Step("Create tracked build config from request")
    protected BuildConfig givenBuildConfig(BuildConfig request) {
        BuildConfig created = buildConfigSteps.createBuildConfig(request);
        trackBuildConfig(created.getId());
        return created;
    }

//    @Step("Create tracked user")
//    protected User givenUser() {
//        User created = userSteps.createUser(dataFactory.createRandomUser());
//        trackUser(created.getUsername());
//        return created;
//    }

    @Step("Create tracked user")
    protected User givenUser() {
        User request = dataFactory.createRandomUser();
        User created = userSteps.createUser(request);
        // TeamCity не возвращает пароль в ответе,
    // поэтому переносим его из исходного объекта
        created.setPassword(request.getPassword());
        trackUser(created.getUsername());
        return created;
}

    @Step("Create tracked user from request")
    protected User givenUser(User request) {
        User created = userSteps.createUser(request);
        trackUser(created.getUsername());
        return created;
    }

    @Step("Create BuildConfigSteps for a new user")
    protected BuildConfigSteps givenUserBuildConfigSteps() {
        User request = dataFactory.createRandomUser();
        givenUser(request);
        return new BuildConfigSteps(
                adminSteps.createClientForUser(
                        request.getUsername(),
                        request.getPassword()));
    }

    @Step("Create BuildRunSteps for a new user")
    protected BuildRunSteps givenUserBuildRunSteps() {
        User request = dataFactory.createRandomUser();
        givenUser(request);
        return new BuildRunSteps(
                adminSteps.createClientForUser(
                        request.getUsername(),
                        request.getPassword()));
    }

    @Step("Create BuildRunSteps for admin")
    protected BuildRunSteps givenAdminBuildRunSteps() {
        ApiClient admin = adminClient;
        return new BuildRunSteps(admin);
    }

    @Step("Create negative BuildRunSteps for user")
    protected BuildRunSteps givenNegativeBuildRunSteps(User user) {
        return new BuildRunSteps(
                ClientFactory.createNegativeBasicAuthClient(
                        user.getUsername(),
                        user.getPassword()
                )
        );
    }

    @Step("Create BuildRunSteps for user: {user.username}")

    protected BuildRunSteps givenBuildRunSteps(User user) {
        return new BuildRunSteps(
                adminSteps.createClientForUser(user.getUsername(), user.getPassword()));
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

    @Step("Track vcs root for cleanup: {}")
    protected void trackVcsRoot(String vcsRootId) {
        if (vcsRootId != null && !vcsRootId.isEmpty()) {
            createdVcsRoots.add(vcsRootId);
        }
    }

    @Step("Get first available agent")
    protected Agent givenAgent() {
        return agentSteps.getAllAgents()
                .getAgent()
                .getFirst();
    }

    @Step("Create tracked build config in a new project")
    protected BuildConfig givenBuildConfig() {
        Project project = givenProject();
        return givenBuildConfig(project.getId());
    }

    @Step("Run build and wait for finish")
    protected Build givenFinishedBuild(String buildConfigId) {
        Build build = buildRunSteps.runBuild(buildConfigId);
        return buildRunSteps.waitForBuildFinish(build.getId());
    }

    @Step("Get NBank build configuration")
    protected BuildConfig givenNBankBuildConfig() {
        return buildConfigSteps.getBuildConfig(TestDataValues.NBANK_BUILD_CONFIG_ID);
    }

    @Step("Run finished NBank build")
    protected Build givenFinishedNBankBuild() {
        Build finished = givenFinishedBuild(givenNBankBuildConfig().getId());
        ApiAssertions.assertBuildFinished(
                finished,
                finished.getId(),
                TestDataValues.BUILD_STATUS_SUCCESS
        );
        return finished;
    }

    @Step("Get artifacts for build: {build.id}")
    protected Files givenArtifacts(Build build) {
        Files artifacts = artifactSteps.getArtifacts(build.getId());
        ApiAssertions.assertArtifactsExist(artifacts);
        return artifacts;
    }
}
