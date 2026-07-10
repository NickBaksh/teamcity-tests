package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RequestType;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.models.VcsRoot;
import com.teamcity.core.models.VcsRoot.VcsRootInstance;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AdminSteps {

    private final ApiClient adminClient;
    private final ProjectSteps projectSteps;
    private final BuildSteps buildSteps;
    private final UserSteps userSteps;
    private final ResponseValidator responseValidator;

    @Getter
    private final TestDataFactory dataFactory;

    // Списки для отслеживания созданных ресурсов
    private final List<String> createdProjects = new ArrayList<>();
    private final List<String> createdBuildConfigs = new ArrayList<>();
    private final List<String> createdUsers = new ArrayList<>();
    private final List<String> createdVcsRoots = new ArrayList<>();

    // Константы для тестов
    @Getter
    private final String defaultBranch;
    @Getter
    private final int defaultTimeoutSeconds;

    // =========================================================================
    // КОНСТРУКТОРЫ
    // =========================================================================

    public AdminSteps(ApiClient adminClient) {
        this.adminClient = adminClient;
        this.projectSteps = new ProjectSteps(adminClient);
        this.buildSteps = new BuildSteps(adminClient);
        this.userSteps = new UserSteps(adminClient);
        this.responseValidator = new ResponseValidator();
        this.dataFactory = new TestDataFactory();
        this.defaultBranch = TestDataFactory.DEFAULT_BRANCH;
        this.defaultTimeoutSeconds = TestDataFactory.DEFAULT_TIMEOUT_SECONDS;
    }

    public AdminSteps(ApiClient adminClient, String defaultBranch, int defaultTimeoutSeconds) {
        this.adminClient = adminClient;
        this.projectSteps = new ProjectSteps(adminClient);
        this.buildSteps = new BuildSteps(adminClient);
        this.userSteps = new UserSteps(adminClient);
        this.responseValidator = new ResponseValidator();
        this.dataFactory = new TestDataFactory();
        this.defaultBranch = defaultBranch != null ? defaultBranch : TestDataFactory.DEFAULT_BRANCH;
        this.defaultTimeoutSeconds = defaultTimeoutSeconds > 0 ? defaultTimeoutSeconds : TestDataFactory.DEFAULT_TIMEOUT_SECONDS;
    }

    // =========================================================================
    // ГЕНЕРАЦИЯ ТЕСТОВЫХ ДАННЫХ
    // =========================================================================

    @Step("Create test environment with generated data")
    public TestEnvironment createTestEnvironment() {
        TestDataFactory.TestEnvironmentData data = dataFactory.generateTestEnvironmentData();
        log.info("Creating test environment with generated data: {}", data);

        Project project = createTestProject(
                data.getProjectId(),
                data.getProjectName(),
                data.getDescription()
        );

        BuildConfig buildConfig = createTestBuildConfig(
                data.getBuildConfigId(),
                data.getBuildConfigName(),
                project.getId(),
                data.getDescription()
        );

        return new TestEnvironment(project, buildConfig, data.getBranch(), data.getTimeoutSeconds());
    }

    @Step("Create test environment with custom names")
    public TestEnvironment createTestEnvironment(String projectName, String configName) {
        TestDataFactory.TestEnvironmentData data = dataFactory.generateTestEnvironmentData(projectName, configName);
        log.info("Creating test environment: {} / {}", projectName, configName);

        Project project = createTestProject(
                data.getProjectId(),
                data.getProjectName(),
                data.getDescription()
        );

        BuildConfig buildConfig = createTestBuildConfig(
                data.getBuildConfigId(),
                data.getBuildConfigName(),
                project.getId(),
                data.getDescription()
        );

        return new TestEnvironment(project, buildConfig, data.getBranch(), data.getTimeoutSeconds());
    }

    @Step("Create test environment with specified data")
    public TestEnvironment createTestEnvironment(
            String projectId, String projectName,
            String configId, String configName,
            String description) {

        Project project = createTestProject(projectId, projectName, description);
        BuildConfig buildConfig = createTestBuildConfig(configId, configName, project.getId(), description);

        return new TestEnvironment(project, buildConfig, defaultBranch, defaultTimeoutSeconds);
    }

    // =========================================================================
    // УПРАВЛЕНИЕ ПРОЕКТАМИ
    // =========================================================================

    @Step("Create test project: {projectId}")
    public Project createTestProject(String projectId, String projectName, String description) {
        log.info("Creating test project: {} ({})", projectId, projectName);

        Project testProject = Project.builder()
                .id(projectId)
                .name(projectName)
                .description(description != null ? description : "Project for API testing")
                .parentProjectId("_Root")
                .build();

        try {
            projectSteps.deleteProjectIfExists(projectId);
            Project created = projectSteps.createProject(testProject);
            trackProject(created.getId());
            log.info("✅ Test project created: {}", created.getId());
            return created;
        } catch (Exception e) {
            log.warn("⚠️ Project setup error: {}", e.getMessage());
            throw new RuntimeException("Failed to create test project: " + projectId, e);
        }
    }

    @Step("Create test project with generated ID")
    public Project createTestProject(String projectName) {
        String projectId = dataFactory.generateProjectId();
        return createTestProject(projectId, projectName, null);
    }

    @Step("Delete project: {projectId}")
    public void deleteProject(String projectId) {
        log.info("Deleting project: {}", projectId);
        try {
            List<BuildConfig> configs = buildSteps.getBuildConfigsByProject(projectId);
            for (BuildConfig config : configs) {
                buildSteps.deleteBuildConfigIfExists(config.getId());
                log.debug("Deleted build config: {}", config.getId());
            }
            projectSteps.deleteProjectIfExists(projectId);
            log.info("✅ Project deleted: {}", projectId);
        } catch (Exception e) {
            log.warn("⚠️ Failed to delete project: {}", e.getMessage());
            throw new RuntimeException("Failed to delete project: " + projectId, e);
        }
    }

    @Step("Delete project if exists: {projectId}")
    public boolean deleteProjectIfExists(String projectId) {
        if (projectSteps.projectExists(projectId)) {
            deleteProject(projectId);
            return true;
        }
        log.debug("Project {} does not exist, skipping deletion", projectId);
        return false;
    }

    // =========================================================================
    // УПРАВЛЕНИЕ BUILD CONFIGURATIONS
    // =========================================================================

    @Step("Create test build config: {configId}")
    public BuildConfig createTestBuildConfig(String configId, String configName, String projectId, String description) {
        log.info("Creating test build config: {} ({})", configId, configName);

        BuildConfig buildConfig = BuildConfig.builder()
                .id(configId)
                .name(configName)
                .projectId(projectId)
                .description(description != null ? description : "Build config for testing")
                .build();

        try {
            buildSteps.deleteBuildConfigIfExists(configId);
            BuildConfig created = buildSteps.createBuildConfig(buildConfig);
            trackBuildConfig(created.getId());
            log.info("✅ Build config created: {}", created.getId());
            return created;
        } catch (Exception e) {
            log.warn("⚠️ Build config setup error: {}", e.getMessage());
            throw new RuntimeException("Failed to create test build config: " + configId, e);
        }
    }

    @Step("Create test build config with generated ID")
    public BuildConfig createTestBuildConfig(String configName, String projectId) {
        String configId = dataFactory.generateBuildConfigId();
        return createTestBuildConfig(configId, configName, projectId, null);
    }

    @Step("Delete build config: {configId}")
    public void deleteBuildConfig(String configId) {
        log.info("Deleting build config: {}", configId);
        buildSteps.deleteBuildConfigIfExists(configId);
        log.info("✅ Build config deleted: {}", configId);
    }

    @Step("Delete build config if exists: {configId}")
    public boolean deleteBuildConfigIfExists(String configId) {
        if (buildSteps.buildConfigExists(configId)) {
            deleteBuildConfig(configId);
            return true;
        }
        log.debug("Build config {} does not exist, skipping deletion", configId);
        return false;
    }

    // =========================================================================
    // УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ
    // =========================================================================

    @Step("Create test user: {username}")
    public User createTestUser(String username, String password, String name, String email) {
        log.info("Creating test user: {}", username);

        User user = User.builder()
                .username(username)
                .password(password)
                .name(name != null ? name : username)
                .email(email != null ? email : username + "@test.com")
                .build();

        try {
            userSteps.deleteUser(username);
            User created = userSteps.createUser(user);
            trackUser(created.getUsername());
            log.info("✅ Test user created: {}", created.getUsername());
            return created;
        } catch (Exception e) {
            log.warn("⚠️ User setup error: {}", e.getMessage());
            throw new RuntimeException("Failed to create test user: " + username, e);
        }
    }

    @Step("Create test user with generated data")
    public User createTestUser() {
        String username = dataFactory.generateUsername();
        String email = dataFactory.generateEmail(username);
        return createTestUser(username, TestDataFactory.DEFAULT_PASSWORD, "Test User", email);
    }

    @Step("Delete user: {username}")
    public void deleteUser(String username) {
        log.info("Deleting user: {}", username);
        try {
            userSteps.deleteUser(username);
            log.info("✅ User deleted: {}", username);
        } catch (Exception e) {
            log.warn("⚠️ Failed to delete user: {}", e.getMessage());
            throw new RuntimeException("Failed to delete user: " + username, e);
        }
    }

    @Step("Delete user if exists: {username}")
    public boolean deleteUserIfExists(String username) {
        try {
            userSteps.getUser(username);
            deleteUser(username);
            return true;
        } catch (Exception e) {
            log.debug("User {} does not exist, skipping deletion", username);
            return false;
        }
    }

    // =========================================================================
    // УПРАВЛЕНИЕ VCS ROOTS
    // =========================================================================

    @Step("Create VCS root")
    public VcsRoot createVcsRoot(VcsRoot vcsRoot) {
        log.info("Creating VCS root: {}", vcsRoot.getId());
        Response response = adminClient.post(Endpoint.VCS_ROOTS.getPath(), vcsRoot);
        VcsRoot created = responseValidator.validate(response, VcsRoot.class);
        trackVcsRoot(created.getId());
        return created;
    }

    @Step("Get VCS root by ID: {vcsRootId}")
    public VcsRoot getVcsRoot(String vcsRootId) {
        log.debug("Fetching VCS root: {}", vcsRootId);
        // ✅ Явно указываем Accept: application/json
        Response response = adminClient.get(
                Endpoint.VCS_ROOT.format(vcsRootId),
                RequestType.JSON
        );
        return responseValidator.validate(response, VcsRoot.class);
    }

    @Step("Get all VCS roots")
    public List<VcsRoot> getAllVcsRoots() {
        log.debug("Fetching all VCS roots");
        Response response = adminClient.get(Endpoint.VCS_ROOTS.getPath());
        return responseValidator.validate(response,
                res -> res.jsonPath().getList("vcs-root", VcsRoot.class));
    }

    @Step("Get VCS roots by project: {projectId}")
    public List<VcsRoot> getVcsRootsByProject(String projectId) {
        log.debug("Fetching VCS roots for project: {}", projectId);
        String endpoint = Endpoint.VCS_ROOTS.getPath() + "?locator=project:(id:" + projectId + ")";
        Response response = adminClient.get(endpoint, RequestType.JSON);
        return responseValidator.validate(response,
                res -> res.jsonPath().getList("vcs-root", VcsRoot.class));
    }

    @Step("Delete VCS root: {vcsRootId}")
    public void deleteVcsRoot(String vcsRootId) {
        log.info("Deleting VCS root: {}", vcsRootId);
        Response response = adminClient.delete(Endpoint.VCS_ROOT.format(vcsRootId));
        responseValidator.validateStatus(response);
    }

    @Step("Delete VCS root if exists: {vcsRootId}")
    public boolean deleteVcsRootIfExists(String vcsRootId) {
        if (vcsRootExists(vcsRootId)) {
            deleteVcsRoot(vcsRootId);
            return true;
        }
        log.debug("VCS root {} does not exist, skipping deletion", vcsRootId);
        return false;
    }

    @Step("Update VCS root name: {vcsRootId} -> {newName}")
    public void updateVcsRootName(String vcsRootId, String newName) {
        log.info("Updating VCS root name: {} -> {}", vcsRootId, newName);
        Response response = adminClient.putText(
                Endpoint.VCS_ROOT_NAME.format(vcsRootId), newName);
        responseValidator.validateStatus(response);
    }

    @Step("Update VCS root property: {vcsRootId} -> {propertyName} = {propertyValue}")
    public void updateVcsRootProperty(String vcsRootId, String propertyName, String propertyValue) {
        log.info("Updating VCS root property: {} -> {} = {}", vcsRootId, propertyName, propertyValue);
        Response response = adminClient.putText(
                Endpoint.VCS_ROOT_PROPERTY.format(vcsRootId, propertyName), propertyValue);
        responseValidator.validateStatus(response);
    }

    @Step("Update VCS root properties: {vcsRootId}")
    public void updateVcsRootProperties(String vcsRootId, Map<String, String> properties) {
        log.info("Updating VCS root properties: {}", vcsRootId);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            updateVcsRootProperty(vcsRootId, entry.getKey(), entry.getValue());
        }
    }

    @Step("Check if VCS root exists: {vcsRootId}")
    public boolean vcsRootExists(String vcsRootId) {
        try {
            getVcsRoot(vcsRootId);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    @Step("Find VCS root by name: {name}")
    public Optional<VcsRoot> findVcsRootByName(String name) {
        List<VcsRoot> roots = getAllVcsRoots();
        return roots.stream()
                .filter(r -> name.equals(r.getName()))
                .findFirst();
    }

    @Step("Find VCS roots by name prefix: {prefix}")
    public List<VcsRoot> findVcsRootsByNamePrefix(String prefix) {
        List<VcsRoot> roots = getAllVcsRoots();
        return roots.stream()
                .filter(r -> r.getName() != null && r.getName().startsWith(prefix))
                .collect(Collectors.toList());
    }

    @Step("Get VCS roots by type: {vcsName}")
    public List<VcsRoot> getVcsRootsByType(String vcsName) {
        List<VcsRoot> roots = getAllVcsRoots();
        return roots.stream()
                .filter(r -> vcsName.equals(r.getVcsName()))
                .collect(Collectors.toList());
    }

    @Step("Get VCS root web URL: {vcsRootId}")
    public String getVcsRootWebUrl(String vcsRootId) {
        return "http://localhost:8111/admin/editVcsRoot.html?id=" + vcsRootId;
    }

    @Step("Get VCS root instances: {vcsRootId}")
    public List<VcsRootInstance> getVcsRootInstances(String vcsRootId) {
        log.debug("Fetching VCS root instances for: {}", vcsRootId);
        String endpoint = Endpoint.VCS_ROOT_INSTANCES_ALL.getPath()
                + "?locator=vcsRoot:(id:" + vcsRootId + ")";
        Response response = adminClient.get(endpoint, RequestType.JSON);
        return responseValidator.validate(response,
                res -> res.jsonPath().getList("vcs-root-instance", VcsRootInstance.class));
    }

    @Step("Get VCS root instance: {instanceId}")
    public VcsRootInstance getVcsRootInstance(String instanceId) {
        log.debug("Fetching VCS root instance: {}", instanceId);
        Response response = adminClient.get(
                Endpoint.VCS_ROOT_INSTANCE.format(instanceId),
                RequestType.JSON
        );
        return responseValidator.validate(response, VcsRootInstance.class);
    }

    @Step("Get VCS root instance properties: {instanceId}")
    public Map<String, String> getVcsRootInstanceProperties(String instanceId) {
        log.debug("Fetching VCS root instance properties: {}", instanceId);
        Response response = adminClient.get(
                Endpoint.VCS_ROOT_INSTANCE_PROPERTIES.format(instanceId),
                RequestType.JSON
        );
        return responseValidator.validate(response,
                res -> res.jsonPath().getMap(""));
    }

    @Step("Get VCS root instance repository state: {instanceId}")
    public String getVcsRootInstanceRepositoryState(String instanceId) {
        log.debug("Fetching VCS root instance repository state: {}", instanceId);
        Response response = adminClient.get(
                Endpoint.VCS_ROOT_INSTANCE_REPOSITORY_STATE.format(instanceId),
                RequestType.JSON
        );
        return response.getBody().asString();
    }

    @Step("Send commit hook notification for VCS root: {vcsRootId}")
    public String sendCommitHookNotification(String vcsRootId) {
        log.info("Sending commit hook notification for: {}", vcsRootId);
        String locator = "vcsRoot:(id:" + vcsRootId + ")";

        // ✅ Используем POST с пустым телом, но передаем null как body
        // Некоторые API требуют передачи пустого объекта
        Response response = adminClient.post(
                Endpoint.VCS_ROOT_INSTANCE_COMMIT_HOOK.getPath() + "?locator=" + locator,
                ""  // ← пустая строка вместо null
        );

        responseValidator.validateStatusIn(response, Set.of(200, 202));
        return response.getBody().asString();
    }

    @Step("Get VCS root permissions: {vcsRootId}")
    public Map<String, Boolean> getVcsRootPermissions(String vcsRootId) {
        log.debug("Fetching VCS root permissions: {}", vcsRootId);
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("read", true);
        permissions.put("write", true);
        return permissions;
    }

    @Step("Cleanup test VCS roots with prefix: {prefix}")
    public int cleanupTestVcsRoots(String prefix) {
        log.info("Cleaning up test VCS roots with prefix: {}", prefix);
        List<VcsRoot> roots = findVcsRootsByNamePrefix(prefix);
        int deleted = 0;
        for (VcsRoot root : roots) {
            try {
                if (deleteVcsRootIfExists(root.getId())) {
                    deleted++;
                }
            } catch (Exception e) {
                log.warn("Failed to delete VCS root {}: {}", root.getId(), e.getMessage());
            }
        }
        log.info("Cleaned up {} VCS roots", deleted);
        return deleted;
    }

    @Step("Cleanup all test VCS roots")
    public int cleanupAllTestVcsRoots() {
        return cleanupTestVcsRoots("Test VCS Root");
    }

    // =========================================================================
    // ТРЕКИНГ РЕСУРСОВ
    // =========================================================================

    public void trackProject(String projectId) {
        if (projectId != null && !projectId.isEmpty() && !createdProjects.contains(projectId)) {
            createdProjects.add(projectId);
            log.debug("Tracking project: {}", projectId);
        }
    }

    public void trackBuildConfig(String configId) {
        if (configId != null && !configId.isEmpty() && !createdBuildConfigs.contains(configId)) {
            createdBuildConfigs.add(configId);
            log.debug("Tracking build config: {}", configId);
        }
    }

    public void trackUser(String username) {
        if (username != null && !username.isEmpty() && !createdUsers.contains(username)) {
            createdUsers.add(username);
            log.debug("Tracking user: {}", username);
        }
    }

    public void trackVcsRoot(String vcsRootId) {
        if (vcsRootId != null && !vcsRootId.isEmpty() && !createdVcsRoots.contains(vcsRootId)) {
            createdVcsRoots.add(vcsRootId);
            log.debug("Tracking VCS root: {}", vcsRootId);
        }
    }

    @Step("Cleanup all tracked resources")
    public void cleanupTrackedResources() {
        log.info("Cleaning up all tracked resources...");

        // Удаляем билд-конфиги
        for (String configId : createdBuildConfigs) {
            try {
                deleteBuildConfigIfExists(configId);
            } catch (Exception e) {
                log.warn("Failed to delete tracked build config: {}", configId);
            }
        }
        createdBuildConfigs.clear();

        // Удаляем проекты
        for (String projectId : createdProjects) {
            try {
                deleteProjectIfExists(projectId);
            } catch (Exception e) {
                log.warn("Failed to delete tracked project: {}", projectId);
            }
        }
        createdProjects.clear();

        // Удаляем пользователей
        for (String username : createdUsers) {
            try {
                deleteUserIfExists(username);
            } catch (Exception e) {
                log.warn("Failed to delete tracked user: {}", username);
            }
        }
        createdUsers.clear();

        // Удаляем VCS roots
        for (String vcsRootId : createdVcsRoots) {
            try {
                deleteVcsRootIfExists(vcsRootId);
            } catch (Exception e) {
                log.warn("Failed to delete tracked VCS root: {}", vcsRootId);
            }
        }
        createdVcsRoots.clear();

        log.info("✅ All tracked resources cleaned up");
    }

    // =========================================================================
    // ВСПОМОГАТЕЛЬНЫЙ КЛАСС
    // =========================================================================

    @Getter
    public static class TestEnvironment {
        private final Project project;
        private final BuildConfig buildConfig;
        private final String branch;
        private final int timeoutSeconds;

        public TestEnvironment(Project project, BuildConfig buildConfig, String branch, int timeoutSeconds) {
            this.project = project;
            this.buildConfig = buildConfig;
            this.branch = branch;
            this.timeoutSeconds = timeoutSeconds;
        }

        public String getProjectId() {
            return project != null ? project.getId() : null;
        }

        public String getBuildConfigId() {
            return buildConfig != null ? buildConfig.getId() : null;
        }

        public String getProjectName() {
            return project != null ? project.getName() : null;
        }

        public String getBuildConfigName() {
            return buildConfig != null ? buildConfig.getName() : null;
        }

        @Override
        public String toString() {
            return "TestEnvironment{" +
                    "projectId='" + getProjectId() + '\'' +
                    ", projectName='" + getProjectName() + '\'' +
                    ", buildConfigId='" + getBuildConfigId() + '\'' +
                    ", buildConfigName='" + getBuildConfigName() + '\'' +
                    ", branch='" + branch + '\'' +
                    ", timeoutSeconds=" + timeoutSeconds +
                    '}';
        }
    }
}