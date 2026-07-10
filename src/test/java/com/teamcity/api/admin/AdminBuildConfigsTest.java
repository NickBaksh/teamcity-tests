package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.DuplicateResourceException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.exceptions.ValidationException;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.steps.BuildSteps;
import com.teamcity.core.steps.ProjectSteps;
import io.qameta.allure.*;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты для управления Build Configurations в TeamCity API.
 * <p>
 * Покрывает CRUD операции, валидацию, негативные сценарии и edge cases.
 * Все тесты изолированы и используют cleanup для удаления созданных ресурсов.
 * <p>
 * Иерархия тестов:
 * <ul>
 *   <li>P0 (BLOCKER) — Smoke тесты: создание, получение, удаление</li>
 *   <li>P1 (CRITICAL) — Основной функционал: обновление, пауза, валидация</li>
 *   <li>P2 (NORMAL) — Параметризованные тесты</li>
 *   <li>P3 (MINOR) — Edge cases</li>
 * </ul>
 *
 * @see <a href="https://www.jetbrains.com/help/teamcity/rest-api.html">TeamCity REST API</a>
 */
@Slf4j
@Epic("Admin API")
@Feature("Build Configuration Management")
@Tag("admin")
@Tag("build-configs")
@Tag("api-tests")
@DisplayName("Build Configuration Management Tests")
public class AdminBuildConfigsTest extends BaseApiTest {

    private static final String NON_EXISTENT_ID = "non-existent-id-12345";
    private static final String INVALID_PROJECT_ID = "invalid-project-id";
    private static final String ROOT_PROJECT_ID = "_Root";


    private ProjectSteps projectSteps;
    private BuildSteps buildSteps;
    private String testProjectId;


    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        projectSteps = new ProjectSteps(adminClient);
        buildSteps = new BuildSteps(adminClient);

        Project project = dataFactory.createRandomProject();
        Project created = projectSteps.createProject(project);
        testProjectId = created.getId();
        trackProject(testProjectId);

        log.debug("Test setup completed. Project ID: {}", testProjectId);
    }

    private void cleanupResources() {
        try {
            List<BuildConfig> configs = buildSteps.getAllBuildConfigs();
            configs.stream()
                    .filter(config -> testProjectId.equals(config.getProjectId()))
                    .forEach(config -> {
                        try {
                            buildSteps.deleteBuildConfigIfExists(config.getId());
                            log.debug("Cleaned up build config: {}", config.getId());
                        } catch (Exception e) {
                            log.warn("Failed to clean up build config {}: {}", config.getId(), e.getMessage());
                        }
                    });

            try {
                projectSteps.deleteProject(testProjectId);
                log.debug("Cleaned up project: {}", testProjectId);
            } catch (ResourceNotFoundException e) {
                log.debug("Project {} already deleted", testProjectId);
            } catch (Exception e) {
                log.warn("Failed to clean up project {}: {}", testProjectId, e.getMessage());
            }

        } catch (Exception e) {
            log.warn("Error during cleanup: {}", e.getMessage());
        }
    }

    @Test
    @Order(1)
    @Tag("smoke")
    @Tag("critical")
    @Tag("crud")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Create build config with valid data")
    @Description("Verifies that a build configuration can be created with valid data")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Create build config")
    void shouldCreateBuildConfigWithValidData() {

        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);

        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created).as("Created build config should not be null").isNotNull();
        softly.assertThat(created.getId()).as("ID should not be empty").isNotBlank();
        softly.assertThat(created.getName()).as("Name should match").isEqualTo(config.getName());
        softly.assertThat(created.getProjectId()).as("Project ID should match").isEqualTo(testProjectId);
        softly.assertThat(created.getHref()).as("Href should not be empty").isNotBlank();
        softly.assertAll();

        log.info("✅ Build config created: ID={}, Name={}", created.getId(), created.getName());
    }

    @Test
    @Order(2)
    @Tag("smoke")
    @Tag("critical")
    @Tag("crud")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Get build config by ID")
    @Description("Verifies that a build configuration can be retrieved by ID")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Get build config")
    void shouldGetBuildConfigById() {

        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        BuildConfig retrieved = buildSteps.getBuildConfig(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(retrieved).as("Retrieved build config should not be null").isNotNull();
        softly.assertThat(retrieved.getId()).as("ID should match").isEqualTo(created.getId());
        softly.assertThat(retrieved.getName()).as("Name should match").isEqualTo(created.getName());
        softly.assertThat(retrieved.getProjectId()).as("Project ID should match").isEqualTo(testProjectId);
        softly.assertAll();

        log.info("✅ Build config retrieved: ID={}, Name={}", retrieved.getId(), retrieved.getName());
    }

    @Test
    @Order(3)
    @Tag("smoke")
    @Tag("critical")
    @Tag("crud")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Delete build config")
    @Description("Verifies that a build configuration can be deleted")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Delete build config")
    void shouldDeleteBuildConfig() {
        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);

        buildSteps.deleteBuildConfig(created.getId());

        assertThat(buildSteps.buildConfigExists(created.getId()))
                .as("Build config should not exist after deletion")
                .isFalse();

        log.info("✅ Build config deleted: ID={}", created.getId());
    }


    @Test
    @Order(4)
    @Tag("positive")
    @Tag("normal")
    @Tag("crud")
    @DisplayName("✅ Get all build configs")
    @Description("Verifies that all build configurations can be retrieved")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Get build config")
    void shouldGetAllBuildConfigs() {

        createMultipleBuildConfigs(2);

        List<BuildConfig> configs = buildSteps.getAllBuildConfigs();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(configs).as("Build configs list should not be null").isNotNull();
        softly.assertThat(configs).as("Should have at least 2 configs").hasSizeGreaterThanOrEqualTo(2);
        softly.assertAll();

        log.info("✅ Retrieved {} build configs", configs.size());
    }

    @Test
    @Order(5)
    @Tag("positive")
    @Tag("normal")
    @Tag("crud")
    @DisplayName("✅ Update build config name")
    @Description("Verifies that build configuration name can be updated")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Update build config")

    void shouldUpdateBuildConfigName() {

        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());
        String newName = dataFactory.generateUniqueBuildConfigName();
        buildSteps.updateBuildConfig(created.getId(), newName);

        BuildConfig reloaded = buildSteps.getBuildConfig(created.getId());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(reloaded)
                .as("Reloaded config should not be null")
                .isNotNull();

        softly.assertThat(reloaded.getId())
                .as("ID should remain the same")
                .isEqualTo(created.getId());

        softly.assertThat(reloaded.getName())
                .as("Name should be updated")
                .isEqualTo(newName);

        softly.assertThat(reloaded.getProjectId())
                .as("Project ID should remain the same")
                .isEqualTo(testProjectId);
        softly.assertAll();

        log.info("✅ Build config updated: {} → {}", config.getName(), newName);

    }


    @Test
    @Order(6)
    @Tag("positive")
    @Tag("normal")
    @Tag("crud")
    @DisplayName("✅ Pause build config")
    @Description("Verifies that a build configuration can be paused")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Pause build config")
    void shouldPauseBuildConfig() {

        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        buildSteps.setBuildConfigPaused(created.getId(), true);

        BuildConfig paused = buildSteps.getBuildConfig(created.getId());
        assertThat(paused.getPaused())
                .as("Build config should be paused")
                .isTrue();

        log.info("✅ Build config paused: {}", created.getName());
    }

    @Order(7)
    @Test
    @Tag("known-issue")
    @Tag("build-configs")
    @Tag("pause-resume")
    @DisplayName("✅ Resume build config → 200")
    @Disabled("TC-API-001: TeamCity pause endpoint does not persist paused state")
    @Description("Verifies that build config can be paused and resumed. " +
            "Tests idempotency: pausing already paused config, resuming already resumed config.")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Build Config")
    void shouldResumeBuildConfig() {

        Project project = dataFactory.createRandomProject();
        Project createdProject = projectSteps.createProject(project);
        trackProject(createdProject.getId());

        BuildConfig config = dataFactory.createRandomBuildConfig(createdProject.getId());
        BuildConfig createdConfig = buildSteps.createBuildConfig(config);
        trackBuildConfig(createdConfig.getId());

        SoftAssertions softly = new SoftAssertions();

        BuildConfig initialConfig = buildSteps.getBuildConfig(createdConfig.getId());
        softly.assertThat(Boolean.TRUE.equals(initialConfig.getPaused()));
        buildSteps.pauseBuildConfig(createdConfig.getId());
        BuildConfig pausedConfig = buildSteps.waitUntilPaused(createdConfig.getId());


        softly.assertThat(Boolean.TRUE.equals(pausedConfig.getPaused()))
                .as("Build config should be paused after pause() call")
                .isTrue();

        softly.assertThatCode(() -> buildSteps.pauseBuildConfig(createdConfig.getId()))
                .as("Pausing already paused config should not throw exception")
                .doesNotThrowAnyException();

        BuildConfig doublePausedConfig = buildSteps.getBuildConfig(createdConfig.getId());
        softly.assertThat(doublePausedConfig.getPaused())
                .as("Double pause should keep config paused")
                .isTrue();

        buildSteps.resumeBuildConfig(createdConfig.getId());
        BuildConfig resumedConfig = buildSteps.waitUntilResumed(createdConfig.getId());
        softly.assertThat(Boolean.TRUE.equals(resumedConfig.getPaused()))
                .as("Build config should be resumed after resume() call")
                .isFalse();

        softly.assertThatCode(() -> buildSteps.resumeBuildConfig(createdConfig.getId()))
                .as("Resuming already resumed config should not throw exception")
                .doesNotThrowAnyException();

        BuildConfig doubleResumedConfig = buildSteps.getBuildConfig(createdConfig.getId());
        softly.assertThat(doubleResumedConfig.getPaused())
                .as("Double resume should keep config resumed")
                .isFalse();

        softly.assertThat(resumedConfig.getId())
                .as("Build config ID should not change")
                .isEqualTo(createdConfig.getId());

        softly.assertThat(resumedConfig.getName())
                .as("Build config name should not change")
                .isEqualTo(createdConfig.getName());

        softly.assertAll();

        log.info("✅ Build config pause/resume verified: {}", createdConfig.getName());
    }

    @Test
    @Order(8)
    @Tag("positive")
    @Tag("normal")
    @Tag("crud")
    @DisplayName("✅ Create build config with description")
    @Description("Verifies that a build configuration can be created with description")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create build config")
    void shouldCreateBuildConfigWithDescription() {

        String description = "This is a test build config";
        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        config.setDescription(description);


        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());


        assertThat(created.getDescription())
                .as("Description should match")
                .isEqualTo(description);

        log.info("✅ Build config with description created: {}", created.getName());
    }

    @Test
    @Order(9)
    @Tag("negative")
    @Tag("critical")
    @Tag("validation")
    @DisplayName("❌ Create build config with empty name → 400")
    @Description("Verifies that build config with empty name is rejected with 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create build config validation")
    void shouldNotCreateBuildConfigWithEmptyName() {
        BuildConfig invalidConfig = BuildConfig.builder()
                .name("")
                .projectId(testProjectId)
                .description("Should be rejected")
                .build();

        assertThatThrownBy(() -> buildSteps.createBuildConfig(invalidConfig))
                .as("Build config with empty name should be rejected")
                .isInstanceOfAny(ValidationException.class, ApiException.class)
                .satisfies(exception -> {
                    if (exception instanceof ApiException) {
                        assertThat(((ApiException) exception).getStatusCode())
                                .as("Status code should be 400 for empty name")
                                .isEqualTo(400);
                    }
                });

        log.info("✅ Empty build config name correctly rejected");
    }

    // ============================================================
    // ТЕСТ 2: Пробельные имена → 200 (поведение системы)
    // ============================================================

    @ParameterizedTest
    @Order(10)
    @Tag("positive")
    @Tag("whitespace")
    @Tag("system-behavior")
    @ValueSource(strings = {" ", "\t", "\n", "\r", "  ", " \t "})
    @DisplayName("⚠️ Create build config with whitespace name → 200 (system behavior)")
    @Description("Verifies that TeamCity accepts whitespace names. " +
            "This is system behavior, not a bug. Includes description validation.")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create build config with whitespace")
    void shouldCreateBuildConfigWithWhitespaceName(String whitespaceName) {
        // Arrange
        String escapedName = escapeWhitespace(whitespaceName);
        String description = "Auto-generated build config with whitespace name: " + escapedName;

        BuildConfig config = BuildConfig.builder()
                .name(whitespaceName)
                .projectId(testProjectId)
                .description(description)
                .build();

        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created)
                .as("Build config should be created despite whitespace name")
                .isNotNull();

        softly.assertThat(created.getId())
                .as("Should have valid ID")
                .isNotBlank();

        softly.assertThat(created.getName())
                .as("Name should be preserved as provided")
                .isEqualTo(whitespaceName);

        softly.assertThat(created.getDescription())
                .as("Description should be preserved")
                .isEqualTo(description);

        softly.assertThat(created.getProjectId())
                .as("Project ID should match")
                .isEqualTo(testProjectId);

        softly.assertAll();

        log.info("⚠️ Build config created with whitespace name: '{}'", escapedName);
    }

    /**
     * Экранирует пробельные символы для логирования
     */
    private String escapeWhitespace(String input) {
        if (input == null) return "null";
        return input
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace(" ", "·");  // · для визуализации пробела
    }

    @Test
    @Order(11)
    @Tag("negative")
    @Tag("critical")
    @Tag("conflict")
    @DisplayName("❌ Create build config with duplicate name → 409")
    @Description("Verifies that duplicate build config names are rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create build config validation")
    void shouldNotCreateBuildConfigWithDuplicateName() {

        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());


        assertThatThrownBy(() -> buildSteps.createBuildConfig(config))
                .as("Should throw DuplicateResourceException for duplicate name")
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        log.info("✅ Duplicate build config name correctly rejected");
    }

    @Test
    @Order(12)
    @Tag("negative")
    @Tag("validation")
    @Tag("not-found")
    @DisplayName("❌ Create build config with invalid project ID → 400")
    @Description("Verifies that invalid project ID is rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create build config validation")
    void shouldNotCreateBuildConfigWithInvalidProjectId() {

        BuildConfig invalidConfig = BuildConfig.builder()
                .name(dataFactory.generateUniqueBuildConfigName())
                .projectId(INVALID_PROJECT_ID)
                .build();


        assertThatThrownBy(() -> buildSteps.createBuildConfig(invalidConfig))
                .as("Should throw ValidationException for invalid project")
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Cannot find project");

        log.info("✅ Invalid project ID correctly rejected");
    }

    @Test
    @Order(13)
    @Tag("negative")
    @Tag("not-found")
    @DisplayName("❌ Get non-existent build config → 404")
    @Description("Verifies that non-existent build config returns 404")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Get build config validation")
    void shouldReturn404ForNonExistentBuildConfig() {

        assertThatThrownBy(() -> buildSteps.getBuildConfig(NON_EXISTENT_ID))
                .as("Should throw ApiException with status 404")
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

        log.info("✅ Non-existent build config correctly rejected");
    }

    @ParameterizedTest
    @Order(14)
    @Tag("negative")
    @Tag("parameterized")
    @Tag("validation")
    @MethodSource("provideProjectConfigurations")
    @DisplayName("🔄 Create build config with various project configurations")
    @Description("Verifies build config creation with different project configurations")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create build config validation")
    void shouldCreateBuildConfigWithVariousProjects(String projectId, boolean shouldSucceed, String expectedError) {
        BuildConfig config = dataFactory.createRandomBuildConfig(projectId);

        if (shouldSucceed) {

            BuildConfig created = buildSteps.createBuildConfig(config);
            trackBuildConfig(created.getId());

            assertThat(created.getProjectId())
                    .as("Project ID should match")
                    .isEqualTo(projectId);
            log.info("✅ Build config created with project: {}", projectId);
        } else {

            assertThatThrownBy(() -> buildSteps.createBuildConfig(config))
                    .as("Should fail with appropriate exception for project: " + projectId)
                    .isInstanceOfAny(ValidationException.class, ApiException.class);
            log.info("✅ Build config creation with project '{}' correctly rejected", projectId);
        }
    }

    static Stream<Arguments> provideProjectConfigurations() {
        return Stream.of(
                Arguments.of(ROOT_PROJECT_ID, false, "Root project cannot contain build configurations"),
                Arguments.of(INVALID_PROJECT_ID, false, "Cannot find project"),
                Arguments.of("", false, "Cannot find project"),
                Arguments.of(null, false, "Cannot find project")
        );
    }

    @Test
    @Order(15)
    @Tag("edge")
    @Tag("positive")
    @DisplayName("🔍 Verify build config exists")
    @Description("Verifies that buildConfigExists method works correctly")
    @Severity(SeverityLevel.MINOR)
    @Story("Build config validation")
    void shouldVerifyBuildConfigExists() {
        assertThat(buildSteps.buildConfigExists(NON_EXISTENT_ID))
                .as("Non-existent config should return false")
                .isFalse();

        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        assertThat(buildSteps.buildConfigExists(created.getId()))
                .as("Existing config should return true")
                .isTrue();

        log.info("✅ Build config existence verification successful");
    }

    @Test
    @Order(16)
    @Tag("edge")
    @Tag("negative")
    @DisplayName("🔍 Delete non-existent build config (idempotent)")
    @Description("Verifies that deleting non-existent build config is safe")
    @Severity(SeverityLevel.MINOR)
    @Story("Build config validation")
    void shouldHandleNonExistentDeletion() {
        // Should not throw exception
        buildSteps.deleteBuildConfigIfExists(NON_EXISTENT_ID);
        buildSteps.deleteBuildConfigIfExists(NON_EXISTENT_ID); // Повторный вызов тоже безопасен

        log.info("✅ Non-existent build config deletion handled correctly");
    }

    private void createMultipleBuildConfigs(int count) {
        for (int i = 0; i < count; i++) {
            BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
            BuildConfig created = buildSteps.createBuildConfig(config);
            trackBuildConfig(created.getId());
        }
        log.debug("Created {} build configs", count);
    }
}