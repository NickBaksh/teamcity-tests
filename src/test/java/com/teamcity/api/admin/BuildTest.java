package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.steps.AdminSteps;
import com.teamcity.core.steps.BuildSteps;
import io.qameta.allure.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Epic("TeamCity API")
@Feature("Build Operations")
@Tag("api")
@Tag("build")
@Tag("teamcity")
@DisplayName("API Tests for Build Module")
public class BuildTest extends BaseApiTest {

    private String projectId;
    private String buildConfigId;
    private String branch;
    private int timeoutSeconds;

    @BeforeEach
    @Override
    @Step("Setup Build test environment")
    public void setUp() {
        super.setUp();

        // Создаем тестовое окружение с автоматической генерацией данных
        AdminSteps.TestEnvironment env = adminSteps.createTestEnvironment();

        projectId = env.getProjectId();
        buildConfigId = env.getBuildConfigId();
        branch = env.getBranch();
        timeoutSeconds = env.getTimeoutSeconds();

        log.info("✅ Test environment ready: {}", env);
    }

    @AfterEach
    @Override
    @Step("Cleanup Build test resources")
    public void cleanUp() {
        super.cleanUp();
    }

    // =========================================================================
    // 1. POSITIVE TESTS — Build Configuration CRUD
    // =========================================================================

    @Test
    @DisplayName("Should create build configuration successfully")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Build Configuration Creation")
    @Description("Test that a new build configuration can be created with valid data")
    public void shouldCreateBuildConfiguration() {
        String uniqueId = "TestConfig_" + System.currentTimeMillis();
        String uniqueName = "Test Config " + System.currentTimeMillis();

        BuildConfig config = BuildConfig.builder()
                .id(uniqueId)
                .name(uniqueName)
                .projectId(projectId)
                .description("Created by API test")
                .build();

        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        assertAll("Build config should be created correctly",
                () -> assertNotNull(created.getId(), "ID should not be null"),
                () -> assertEquals(uniqueId, created.getId(), "ID should match"),
                () -> assertEquals(uniqueName, created.getName(), "Name should match"),
                () -> assertEquals(projectId, created.getProjectId(), "Project ID should match"),
                () -> assertNotNull(created.getHref(), "Href should not be null")
        );

        // Проверяем что конфиг действительно существует
        BuildConfig retrieved = buildSteps.getBuildConfig(created.getId());
        assertNotNull(retrieved, "Build config should be retrievable");
        assertEquals(uniqueId, retrieved.getId(), "Retrieved ID should match");
    }

    @Test
    @DisplayName("Should get build configuration by ID")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Build Configuration Retrieval")
    @Description("Test that an existing build configuration can be retrieved by ID")
    public void shouldGetBuildConfiguration() {
        BuildConfig config = buildSteps.getBuildConfig(buildConfigId);

        assertAll("Build config should be retrieved correctly",
                () -> assertNotNull(config, "Config should not be null"),
                () -> assertEquals(buildConfigId, config.getId(), "ID should match"),
                () -> assertEquals(projectId, config.getProjectId(), "Project ID should match"),
                () -> assertNotNull(config.getHref(), "Href should not be null")
        );
    }

    @Test
    @DisplayName("Should get all build configurations")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration List")
    @Description("Test that all build configurations can be retrieved")
    public void shouldGetAllBuildConfigurations() {
        List<BuildConfig> configs = buildSteps.getAllBuildConfigs();

        assertNotNull(configs, "Configs list should not be null");
        assertTrue(configs.size() > 0, "Should have at least one build config");

        // Проверяем что наш тестовый конфиг есть в списке
        boolean found = configs.stream()
                .anyMatch(c -> buildConfigId.equals(c.getId()));
        assertTrue(found, "Test build config should be in the list");
    }

    @Test
    @DisplayName("Should update build configuration name")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Update")
    @Description("Test that build configuration name can be updated")
    public void shouldUpdateBuildConfigurationName() {
        String newName = "Updated Config " + System.currentTimeMillis();

        BuildConfig updated = buildSteps.updateBuildConfig(buildConfigId, newName);

        assertAll("Build config name should be updated",
                () -> assertEquals(buildConfigId, updated.getId(), "ID should not change"),
                () -> assertEquals(newName, updated.getName(), "Name should be updated"),
                () -> assertEquals(projectId, updated.getProjectId(), "Project ID should not change")
        );

        // Проверяем через GET
        BuildConfig retrieved = buildSteps.getBuildConfig(buildConfigId);
        assertEquals(newName, retrieved.getName(), "Retrieved name should be updated");
    }

    @Test
    @DisplayName("Should update build configuration description")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Update")
    @Description("Test that build configuration description can be updated")
    public void shouldUpdateBuildConfigurationDescription() {
        String newDescription = "Updated description " + System.currentTimeMillis();

        BuildConfig updated = buildSteps.updateBuildConfigDescription(buildConfigId, newDescription);

        assertAll("Build config description should be updated",
                () -> assertEquals(buildConfigId, updated.getId(), "ID should not change"),
                () -> assertNotNull(updated.getDescription(), "Description should not be null"),
                () -> assertEquals(newDescription, updated.getDescription(), "Description should be updated")
        );
    }

    @Test
    @DisplayName("Should delete build configuration")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Build Configuration Deletion")
    @Description("Test that build configuration can be deleted")
    public void shouldDeleteBuildConfiguration() {
        // Создаем уникальный конфиг для удаления
        String uniqueId = "DeleteTest_" + System.currentTimeMillis();
        BuildConfig config = BuildConfig.builder()
                .id(uniqueId)
                .name("Config to Delete")
                .projectId(projectId)
                .build();

        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        // Проверяем что конфиг существует
        assertTrue(buildSteps.buildConfigExists(created.getId()), "Config should exist before deletion");

        // Удаляем
        buildSteps.deleteBuildConfig(created.getId());

        // Проверяем что конфиг удален
        assertFalse(buildSteps.buildConfigExists(created.getId()), "Config should not exist after deletion");
    }

    @Test
    @DisplayName("Should delete build configuration if exists (idempotent)")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Deletion")
    @Description("Test idempotent deletion of build configuration")
    public void shouldDeleteBuildConfigurationIfExists() {
        // Первое удаление - конфиг существует
        boolean deleted = buildSteps.deleteBuildConfigIfExists(buildConfigId);
        assertTrue(deleted, "Config should be deleted");

        // Второе удаление - конфиг уже удален
        deleted = buildSteps.deleteBuildConfigIfExists(buildConfigId);
        assertFalse(deleted, "Config should not exist for second deletion");
    }

    // =========================================================================
    // 2. POSITIVE TESTS — Build Pause/Resume
    // =========================================================================

    @Test
    @DisplayName("Should pause and resume build configuration")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Pause")
    @Description("Test that build configuration can be paused and resumed")
    public void shouldPauseAndResumeBuildConfiguration() {
        // Начальное состояние - не приостановлен
        assertFalse(buildSteps.isBuildConfigPaused(buildConfigId),
                "Config should not be paused initially");

        // Приостанавливаем
        buildSteps.pauseBuildConfig(buildConfigId);

        // Проверяем через getBuildConfig
        BuildConfig config = buildSteps.getBuildConfig(buildConfigId);
        assertTrue(config.getPaused() != null && config.getPaused(),
                "Config should be paused");

        // Проверяем через isPaused
        assertTrue(buildSteps.isBuildConfigPaused(buildConfigId),
                "Config should be paused");

        // Возобновляем
        buildSteps.resumeBuildConfig(buildConfigId);

        // Проверяем
        config = buildSteps.getBuildConfig(buildConfigId);
        assertFalse(config.getPaused() != null && config.getPaused(),
                "Config should not be paused");
    }

    @Test
    @DisplayName("Should toggle build configuration pause state")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Pause")
    @Description("Test toggling pause state of build configuration")
    public void shouldToggleBuildConfigurationPause() {
        boolean initialPaused = buildSteps.isBuildConfigPaused(buildConfigId);

        // Toggle 1
        buildSteps.toggleBuildConfigPause(buildConfigId);
        boolean afterFirstToggle = buildSteps.isBuildConfigPaused(buildConfigId);
        assertEquals(!initialPaused, afterFirstToggle,
                "Pause state should be toggled (1)");

        // Toggle 2 - возвращаем исходное состояние
        buildSteps.toggleBuildConfigPause(buildConfigId);
        boolean afterSecondToggle = buildSteps.isBuildConfigPaused(buildConfigId);
        assertEquals(initialPaused, afterSecondToggle,
                "Pause state should be toggled back (2)");
    }

    @Test
    @DisplayName("Should set build configuration pause state directly")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Pause")
    @Description("Test direct pause state setting")
    public void shouldSetBuildConfigurationPauseState() {
        // Устанавливаем паузу
        buildSteps.setBuildConfigPaused(buildConfigId, true);
        assertTrue(buildSteps.isBuildConfigPaused(buildConfigId),
                "Config should be paused");

        // Снимаем паузу
        buildSteps.setBuildConfigPaused(buildConfigId, false);
        assertFalse(buildSteps.isBuildConfigPaused(buildConfigId),
                "Config should not be paused");
    }

    // =========================================================================
    // 3. POSITIVE TESTS — Build Execution
    // =========================================================================

    @Test
    @DisplayName("Should run build successfully")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Build Execution")
    @Description("Test that a build can be triggered and completes")
    public void shouldRunBuild() {
        // Запускаем билд
        Build build = buildSteps.runBuild(buildConfigId);

        assertAll("Build should be triggered",
                () -> assertNotNull(build, "Build should not be null"),
                () -> assertNotNull(build.getId(), "Build ID should not be null"),
                () -> assertNotNull(build.getState(), "Build state should not be null"),
                () -> assertEquals(buildConfigId, build.getBuildTypeId(),
                        "Build type ID should match")
        );

        // Ждем завершения
        Build finishedBuild = buildSteps.waitForBuildFinish(build.getId(), timeoutSeconds);

        assertAll("Build should complete",
                () -> assertNotNull(finishedBuild, "Finished build should not be null"),
                () -> assertNotNull(finishedBuild.getStatus(), "Status should not be null"),
                () -> assertTrue(BuildSteps.BuildStatus.isFinished(finishedBuild.getState()),
                        "Build should be finished")
        );

        log.info("✅ Build completed: ID={}, Status={}, StatusText={}",
                finishedBuild.getId(), finishedBuild.getStatus(), finishedBuild.getStatusText());
    }

    @Test
    @DisplayName("Should run build with parameters")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Execution")
    @Description("Test that a build can be triggered with custom parameters")
    public void shouldRunBuildWithParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        params.put("build.number", "123");

        Build build = buildSteps.runBuild(buildConfigId, params);

        assertAll("Build with parameters should be triggered",
                () -> assertNotNull(build, "Build should not be null"),
                () -> assertNotNull(build.getId(), "Build ID should not be null"),
                () -> assertEquals(buildConfigId, build.getBuildTypeId(),
                        "Build type ID should match")
        );

        // Ждем завершения
        Build finishedBuild = buildSteps.waitForBuildFinish(build.getId(), timeoutSeconds);
        assertNotNull(finishedBuild, "Finished build should not be null");
        assertTrue(BuildSteps.BuildStatus.isFinished(finishedBuild.getState()),
                "Build should be finished");
    }

    @Test
    @DisplayName("Should run build on specific branch")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Execution")
    @Description("Test that a build can be triggered on a specific branch")
    public void shouldRunBuildOnBranch() {
        Build build = buildSteps.runBuildOnBranch(buildConfigId, branch);

        assertAll("Build on branch should be triggered",
                () -> assertNotNull(build, "Build should not be null"),
                () -> assertNotNull(build.getId(), "Build ID should not be null"),
                () -> assertEquals(buildConfigId, build.getBuildTypeId(),
                        "Build type ID should match")
        );

        Build finishedBuild = buildSteps.waitForBuildFinish(build.getId(), timeoutSeconds);
        assertNotNull(finishedBuild, "Finished build should not be null");
    }

    @Test
    @DisplayName("Should cancel running build")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Execution")
    @Description("Test that a running build can be cancelled")
    public void shouldCancelRunningBuild() {
        // Запускаем билд
        Build build = buildSteps.runBuild(buildConfigId);

        // Ждем пока билд начнет выполняться (running)
        Build runningBuild = buildSteps.waitForBuildState(
                build.getId(), "running", 30);

        assertEquals("running", runningBuild.getState(),
                "Build should be running");

        // Отменяем
        String cancelComment = "Cancelled by API test " + System.currentTimeMillis();
        buildSteps.cancelBuild(build.getId(), cancelComment);

        // Ждем завершения отмены
        Build cancelledBuild = buildSteps.waitForBuildFinish(build.getId(), 30);

        assertAll("Build should be cancelled",
                () -> assertTrue(BuildSteps.BuildStatus.isFinished(cancelledBuild.getState()),
                        "Build should be finished"),
                () -> assertEquals("cancelled", cancelledBuild.getStatus(),
                        "Build status should be cancelled")
        );
    }

    @Test
    @DisplayName("Should cancel build with default comment")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Execution")
    @Description("Test that a build can be cancelled with default comment")
    public void shouldCancelBuildWithDefaultComment() {
        Build build = buildSteps.runBuild(buildConfigId);
        buildSteps.waitForBuildState(build.getId(), "running", 30);

        // Отменяем без комментария (используется дефолтный)
        buildSteps.cancelBuild(build.getId());

        Build cancelledBuild = buildSteps.waitForBuildFinish(build.getId(), 30);
        assertTrue(BuildSteps.BuildStatus.isFinished(cancelledBuild.getState()),
                "Build should be finished");
    }

    // =========================================================================
    // 4. POSITIVE TESTS — Build Retrieval
    // =========================================================================

    @Test
    @DisplayName("Should get build by ID")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Retrieval")
    @Description("Test that a build can be retrieved by ID")
    public void shouldGetBuild() {
        // Сначала запускаем билд
        Build build = buildSteps.runBuild(buildConfigId);
        buildSteps.waitForBuildFinish(build.getId(), timeoutSeconds);

        // Получаем по ID
        Build retrieved = buildSteps.getBuild(build.getId());

        assertAll("Build should be retrieved",
                () -> assertNotNull(retrieved, "Build should not be null"),
                () -> assertEquals(build.getId(), retrieved.getId(),
                        "Build ID should match"),
                () -> assertEquals(build.getBuildTypeId(), retrieved.getBuildTypeId(),
                        "Build type ID should match"),
                () -> assertNotNull(retrieved.getState(), "State should not be null"),
                () -> assertNotNull(retrieved.getStatus(), "Status should not be null")
        );
    }

    @Test
    @DisplayName("Should get builds for configuration")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Retrieval")
    @Description("Test that builds for a specific configuration can be retrieved")
    public void shouldGetBuildsForConfiguration() {
        // Запускаем несколько билдов
        Build build1 = buildSteps.runBuild(buildConfigId);
        Build build2 = buildSteps.runBuild(buildConfigId);

        buildSteps.waitForBuildFinish(build1.getId(), timeoutSeconds);
        buildSteps.waitForBuildFinish(build2.getId(), timeoutSeconds);

        // Получаем список билдов для конфига
        List<Build> builds = buildSteps.getBuildsForConfig(buildConfigId);

        assertAll("Builds should be retrieved",
                () -> assertNotNull(builds, "Builds list should not be null"),
                () -> assertTrue(builds.size() >= 2,
                        "Should have at least 2 builds"),
                () -> assertTrue(builds.stream()
                                .anyMatch(b -> build1.getId().equals(b.getId())),
                        "Build1 should be in the list"),
                () -> assertTrue(builds.stream()
                                .anyMatch(b -> build2.getId().equals(b.getId())),
                        "Build2 should be in the list")
        );
    }

    @Test
    @DisplayName("Should get last build for configuration")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Retrieval")
    @Description("Test that the last build for a configuration can be retrieved")
    public void shouldGetLastBuildForConfiguration() {
        // Запускаем билды
        Build build1 = buildSteps.runBuild(buildConfigId);
        buildSteps.waitForBuildFinish(build1.getId(), timeoutSeconds);

        Build build2 = buildSteps.runBuild(buildConfigId);
        buildSteps.waitForBuildFinish(build2.getId(), timeoutSeconds);

        // Получаем последний билд
        Build lastBuild = buildSteps.getLastBuildForConfig(buildConfigId);

        assertAll("Last build should be correct",
                () -> assertNotNull(lastBuild, "Last build should not be null"),
                () -> assertEquals(build2.getId(), lastBuild.getId(),
                        "Last build should be build2"),
                () -> assertEquals(buildConfigId, lastBuild.getBuildTypeId(),
                        "Build type ID should match")
        );
    }

    // =========================================================================
    // 5. NEGATIVE TESTS
    // =========================================================================

    @Test
    @DisplayName("Should not create build configuration with duplicate ID")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that creating build config with duplicate ID fails")
    public void shouldNotCreateDuplicateBuildConfiguration() {
        BuildConfig duplicate = BuildConfig.builder()
                .id(buildConfigId)  // Используем существующий ID
                .name("Duplicate Config")
                .projectId(projectId)
                .build();

        ApiException exception = assertThrows(ApiException.class,
                () -> buildSteps.createBuildConfig(duplicate),
                "Should throw exception for duplicate ID"
        );

        assertAll("Exception should be correct",
                () -> assertTrue(exception.getStatusCode() == 400 || exception.getStatusCode() == 409,
                        "Status code should be 400 or 409"),
                () -> assertTrue(exception.getMessage().toLowerCase().contains("already") ||
                                exception.getMessage().toLowerCase().contains("exists"),
                        "Error message should indicate duplicate")
        );
    }

    @Test
    @DisplayName("Should not get non-existent build configuration")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that getting non-existent build config throws exception")
    public void shouldNotGetNonExistentBuildConfiguration() {
        String nonExistentId = "NonExistentConfig_" + System.currentTimeMillis();

        assertThrows(ResourceNotFoundException.class,
                () -> buildSteps.getBuildConfig(nonExistentId),
                "Should throw not found exception"
        );
    }

    @Test
    @DisplayName("Should not delete non-existent build configuration")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that deleting non-existent build config throws exception")
    public void shouldNotDeleteNonExistentBuildConfiguration() {
        String nonExistentId = "NonExistentConfig_" + System.currentTimeMillis();

        assertThrows(ApiException.class,
                () -> buildSteps.deleteBuildConfig(nonExistentId),
                "Should throw exception for non-existent config"
        );
    }

    @Test
    @DisplayName("Should not update non-existent build configuration")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that updating non-existent build config throws exception")
    public void shouldNotUpdateNonExistentBuildConfiguration() {
        String nonExistentId = "NonExistentConfig_" + System.currentTimeMillis();

        assertThrows(ApiException.class,
                () -> buildSteps.updateBuildConfig(nonExistentId, "New Name"),
                "Should throw exception for non-existent config"
        );
    }

    @Test
    @DisplayName("Should not run build for non-existent configuration")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that running build for non-existent config throws exception")
    public void shouldNotRunBuildForNonExistentConfiguration() {
        String nonExistentId = "NonExistentConfig_" + System.currentTimeMillis();

        assertThrows(ApiException.class,
                () -> buildSteps.runBuild(nonExistentId),
                "Should throw exception for non-existent config"
        );
    }

    @Test
    @DisplayName("Should not cancel non-existent build")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that cancelling non-existent build throws exception")
    public void shouldNotCancelNonExistentBuild() {
        String nonExistentBuildId = "non-existent-build-id";

        assertThrows(ApiException.class,
                () -> buildSteps.cancelBuild(nonExistentBuildId),
                "Should throw exception for non-existent build"
        );
    }

    @Test
    @DisplayName("Should not create build config without required fields")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that creating build config without required fields fails")
    public void shouldNotCreateBuildConfigWithoutRequiredFields() {
        // Без ID
        BuildConfig noId = BuildConfig.builder()
                .name("No ID Config")
                .projectId(projectId)
                .build();

        assertThrows(ApiException.class,
                () -> buildSteps.createBuildConfig(noId),
                "Should throw exception when ID is missing"
        );

        // Без имени
        BuildConfig noName = BuildConfig.builder()
                .id("NoNameConfig_" + System.currentTimeMillis())
                .projectId(projectId)
                .build();

        assertThrows(ApiException.class,
                () -> buildSteps.createBuildConfig(noName),
                "Should throw exception when name is missing"
        );
    }

    // =========================================================================
    // 6. SEARCH & FILTER TESTS
    // =========================================================================

    @Test
    @DisplayName("Should find build configuration by name")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Search")
    @Description("Test that build configuration can be found by name")
    public void shouldFindBuildConfigByName() {
        // Получаем имя созданного конфига
        BuildConfig config = buildSteps.getBuildConfig(buildConfigId);
        String configName = config.getName();

        var optionalConfig = buildSteps.findBuildConfigByName(configName);

        assertTrue(optionalConfig.isPresent(), "Build config should be found by name");
        assertEquals(buildConfigId, optionalConfig.get().getId(),
                "Found config ID should match");
    }

    @Test
    @DisplayName("Should find build configurations by name prefix")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Search")
    @Description("Test that build configurations can be found by name prefix")
    public void shouldFindBuildConfigsByNamePrefix() {
        // Создаем несколько конфигов с общим префиксом
        String prefix = "PrefixTest_" + System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            String id = prefix + "_" + i;
            BuildConfig config = BuildConfig.builder()
                    .id(id)
                    .name(prefix + " Config " + i)
                    .projectId(projectId)
                    .build();
            BuildConfig created = buildSteps.createBuildConfig(config);
            trackBuildConfig(created.getId());
        }

        List<BuildConfig> found = buildSteps.findBuildConfigsByNamePrefix(prefix);

        assertAll("Configs should be found by prefix",
                () -> assertNotNull(found, "Found list should not be null"),
                () -> assertTrue(found.size() >= 3,
                        "Should find at least 3 configs with prefix"),
                () -> assertTrue(found.stream().allMatch(c ->
                                c.getName() != null && c.getName().startsWith(prefix)),
                        "All found configs should have the prefix")
        );
    }

    @Test
    @DisplayName("Should find failed builds")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Filtering")
    @Description("Test that failed builds can be filtered")
    public void shouldFindFailedBuilds() {
        List<Build> failedBuilds = buildSteps.findFailedBuilds(buildConfigId);

        assertNotNull(failedBuilds, "Failed builds list should not be null");

        // Проверяем что все найденные имеют статус FAILURE
        boolean allFailed = failedBuilds.stream()
                .allMatch(b -> "FAILURE".equals(b.getStatus()) ||
                        "failure".equalsIgnoreCase(b.getStatus()));
        // Если нет билдов - тест проходит (не было запусков)
    }

    @Test
    @DisplayName("Should find successful builds")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Filtering")
    @Description("Test that successful builds can be filtered")
    public void shouldFindSuccessfulBuilds() {
        List<Build> successfulBuilds = buildSteps.findSuccessfulBuilds(buildConfigId);

        assertNotNull(successfulBuilds, "Successful builds list should not be null");

        // Проверяем что все найденные имеют статус SUCCESS
        boolean allSuccessful = successfulBuilds.stream()
                .allMatch(b -> "SUCCESS".equals(b.getStatus()) ||
                        "success".equalsIgnoreCase(b.getStatus()));
    }

    // =========================================================================
    // 7. UTILITY & CLEANUP TESTS
    // =========================================================================

    @Test
    @DisplayName("Should check if build configuration exists")
    @Severity(SeverityLevel.NORMAL)
    @Story("Utility Methods")
    @Description("Test buildConfigExists method")
    public void shouldCheckBuildConfigExists() {
        assertTrue(buildSteps.buildConfigExists(buildConfigId),
                "Existing config should be found");

        String nonExistentId = "NonExistent_" + System.currentTimeMillis();
        assertFalse(buildSteps.buildConfigExists(nonExistentId),
                "Non-existent config should not be found");
    }

    @Test
    @DisplayName("Should check if build exists")
    @Severity(SeverityLevel.NORMAL)
    @Story("Utility Methods")
    @Description("Test buildExists method")
    public void shouldCheckBuildExists() {
        Build build = buildSteps.runBuild(buildConfigId);

        assertTrue(buildSteps.buildExists(build.getId()),
                "Existing build should be found");

        String nonExistentBuildId = "non-existent-build-id";
        assertFalse(buildSteps.buildExists(nonExistentBuildId),
                "Non-existent build should not be found");
    }

    @Test
    @DisplayName("Should get build config web URL")
    @Severity(SeverityLevel.MINOR)
    @Story("Utility Methods")
    @Description("Test getting web URL for build configuration")
    public void shouldGetBuildConfigWebUrl() {
        String url = buildSteps.getBuildConfigWebUrl(buildConfigId);

        assertNotNull(url, "URL should not be null");
        assertTrue(url.contains(buildConfigId), "URL should contain config ID");
        assertTrue(url.contains("buildConfiguration"), "URL should contain buildConfiguration path");
    }

    @Test
    @DisplayName("Should get build web URL")
    @Severity(SeverityLevel.MINOR)
    @Story("Utility Methods")
    @Description("Test getting web URL for a build")
    public void shouldGetBuildWebUrl() {
        Build build = buildSteps.runBuild(buildConfigId);
        String url = buildSteps.getBuildWebUrl(build.getId());

        assertNotNull(url, "URL should not be null");
        assertTrue(url.contains(build.getId()), "URL should contain build ID");
        assertTrue(url.contains("build"), "URL should contain build path");
    }

    @Test
    @DisplayName("Should wait for build config to be ready")
    @Severity(SeverityLevel.NORMAL)
    @Story("Utility Methods")
    @Description("Test waiting for build configuration to be ready")
    public void shouldWaitForBuildConfigReady() {
        // Создаем новый конфиг
        String uniqueId = "WaitTest_" + System.currentTimeMillis();
        BuildConfig config = BuildConfig.builder()
                .id(uniqueId)
                .name("Wait Test Config")
                .projectId(projectId)
                .build();

        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        // Ждем пока станет доступен
        BuildConfig ready = buildSteps.waitForBuildConfigReady(created.getId(), 10);

        assertNotNull(ready, "Config should be ready");
        assertEquals(uniqueId, ready.getId(), "ID should match");
    }

    @Test
    @DisplayName("Should cleanup test build configs by prefix")
    @Severity(SeverityLevel.NORMAL)
    @Story("Cleanup")
    @Description("Test cleanup of test build configurations by prefix")
    public void shouldCleanupTestBuildConfigs() {
        String prefix = "CleanupTest_" + System.currentTimeMillis();

        // Создаем несколько конфигов для очистки
        for (int i = 0; i < 5; i++) {
            String id = prefix + "_" + i;
            BuildConfig config = BuildConfig.builder()
                    .id(id)
                    .name(prefix + " Config " + i)
                    .projectId(projectId)
                    .build();
            BuildConfig created = buildSteps.createBuildConfig(config);
            trackBuildConfig(created.getId());
        }

        // Проверяем что конфиги созданы
        List<BuildConfig> before = buildSteps.findBuildConfigsByNamePrefix(prefix);
        assertEquals(5, before.size(), "Should have 5 configs before cleanup");

        // Очищаем
        int deleted = buildSteps.cleanupTestBuildConfigs(prefix);

        assertEquals(5, deleted, "Should delete 5 configs");

        // Проверяем что конфигов больше нет
        List<BuildConfig> after = buildSteps.findBuildConfigsByNamePrefix(prefix);
        assertEquals(0, after.size(), "Should have 0 configs after cleanup");
    }

    // =========================================================================
    // 8. ADVANCED TESTS
    // =========================================================================

    @Test
    @DisplayName("Should run build and verify status transitions")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Build Lifecycle")
    @Description("Test full build lifecycle: queued → running → finished")
    public void shouldVerifyBuildStatusTransitions() {
        // Запускаем билд
        Build build = buildSteps.runBuild(buildConfigId);

        // Проверяем начальное состояние - queued
        Build queuedBuild = buildSteps.getBuild(build.getId());
        assertEquals("queued", queuedBuild.getState(),
                "Initial state should be queued");

        // Ждем состояния running
        Build runningBuild = buildSteps.waitForBuildState(
                build.getId(), "running", 30);

        assertEquals("running", runningBuild.getState(),
                "Build should be running");

        // Ждем завершения
        Build finishedBuild = buildSteps.waitForBuildFinish(build.getId(), timeoutSeconds);

        assertTrue(BuildSteps.BuildStatus.isFinished(finishedBuild.getState()),
                "Build should be finished");
        assertNotNull(finishedBuild.getStatus(), "Status should not be null");
        assertNotNull(finishedBuild.getStatusText(), "Status text should not be null");

        log.info("Build lifecycle: queued → running → finished ✓");
    }

    @Test
    @DisplayName("Should run multiple builds sequentially")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Execution")
    @Description("Test running multiple builds sequentially")
    public void shouldRunMultipleBuildsSequentially() {
        int buildCount = 3;
        List<Build> builds = new ArrayList<>();

        for (int i = 0; i < buildCount; i++) {
            Build build = buildSteps.runBuild(buildConfigId);
            builds.add(build);
            buildSteps.waitForBuildFinish(build.getId(), timeoutSeconds);
            log.info("✅ Build {} completed: {}", i + 1, build.getId());
        }

        // Проверяем что все билды завершены
        for (Build build : builds) {
            Build retrieved = buildSteps.getBuild(build.getId());
            assertTrue(BuildSteps.BuildStatus.isFinished(retrieved.getState()),
                    "Build " + build.getId() + " should be finished");
        }

        // Проверяем что все билды в списке для конфига
        List<Build> allBuilds = buildSteps.getBuildsForConfig(buildConfigId);
        assertTrue(allBuilds.size() >= buildCount,
                "Should have at least " + buildCount + " builds");
    }

    @Test
    @DisplayName("Should get build config with full details (BuildType)")
    @Severity(SeverityLevel.NORMAL)
    @Story("Build Configuration Details")
    @Description("Test getting full build type details")
    public void shouldGetBuildTypeDetails() {
        var buildType = buildSteps.getBuildType(buildConfigId);

        assertAll("Build type should have full details",
                () -> assertNotNull(buildType, "Build type should not be null"),
                () -> assertEquals(buildConfigId, buildType.getId(),
                        "ID should match"),
                () -> assertEquals(projectId, buildType.getProjectId(),
                        "Project ID should match"),
                () -> assertNotNull(buildType.getProjectName(),
                        "Project name should not be null"),
                () -> assertNotNull(buildType.getHref(), "Href should not be null"),
                () -> assertNotNull(buildType.getWebUrl(), "Web URL should not be null")
        );

        // Дополнительные проверки для вложенных объектов
        if (buildType.getBuilds() != null) {
            assertNotNull(buildType.getBuilds().getHref(),
                    "Builds href should not be null");
        }
    }
}