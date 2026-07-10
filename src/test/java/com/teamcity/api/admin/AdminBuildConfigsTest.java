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

@Slf4j
@Feature("Build Configuration Management")
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
        }
    }

    @Test
    @Description("Verifies that a build configuration can be created with valid data")
    @Severity(SeverityLevel.BLOCKER)
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

    }

    @Test
    @Description("Verifies that a build configuration can be retrieved by ID")
    @Severity(SeverityLevel.BLOCKER)
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

    }

    @Test
    @Description("Verifies that a build configuration can be deleted")
    @Severity(SeverityLevel.BLOCKER)
    void shouldDeleteBuildConfig() {
        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);

        buildSteps.deleteBuildConfig(created.getId());

        assertThat(buildSteps.buildConfigExists(created.getId()))
                .as("Build config should not exist after deletion")
                .isFalse();

    }


    @Test
    @Description("Verifies that all build configurations can be retrieved")
    @Severity(SeverityLevel.CRITICAL)
    void shouldGetAllBuildConfigs() {

        createMultipleBuildConfigs(2);

        List<BuildConfig> configs = buildSteps.getAllBuildConfigs();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(configs).as("Build configs list should not be null").isNotNull();
        softly.assertThat(configs).as("Should have at least 2 configs").hasSizeGreaterThanOrEqualTo(2);
        softly.assertAll();
    }

    @Test
    @Description("Verifies that build configuration name can be updated")
    @Severity(SeverityLevel.CRITICAL)
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

    }


    @Test
    @Description("Verifies that a build configuration can be paused")
    @Severity(SeverityLevel.CRITICAL)
    void shouldPauseBuildConfig() {

        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());

        buildSteps.setBuildConfigPaused(created.getId(), true);

        BuildConfig paused = buildSteps.getBuildConfig(created.getId());
        assertThat(paused.getPaused())
                .as("Build config should be paused")
                .isTrue();

    }

    @Test
    @Disabled("TC-API-001: TeamCity pause endpoint does not persist paused state")
    @Description("Verifies that build config can be paused and resumed. " +
            "Tests idempotency: pausing already paused config, resuming already resumed config.")
    @Severity(SeverityLevel.BLOCKER)
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
    }

    @Test
    @Description("Verifies that a build configuration can be created with description")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateBuildConfigWithDescription() {

        String description = "This is a test build config";
        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        config.setDescription(description);


        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());


        assertThat(created.getDescription())
                .as("Description should match")
                .isEqualTo(description);

    }

    @Test
    @Description("Verifies that build config with empty name is rejected with 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
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
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t", "\n", "\r", "  ", " \t "})
    @Description("Verifies that TeamCity accepts whitespace names. " +
            "This is system behavior, not a bug. Includes description validation.")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateBuildConfigWithWhitespaceName(String whitespaceName) {
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

    }

    private String escapeWhitespace(String input) {
        if (input == null) return "null";
        return input
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace(" ", "·");  // · для визуализации пробела
    }

    @Test
    @Description("Verifies that duplicate build config names are rejected")
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateBuildConfigWithDuplicateName() {

        BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
        BuildConfig created = buildSteps.createBuildConfig(config);
        trackBuildConfig(created.getId());


        assertThatThrownBy(() -> buildSteps.createBuildConfig(config))
                .as("Should throw DuplicateResourceException for duplicate name")
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

    }

    @Test
    @Description("Verifies that invalid project ID is rejected")
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateBuildConfigWithInvalidProjectId() {

        BuildConfig invalidConfig = BuildConfig.builder()
                .name(dataFactory.generateUniqueBuildConfigName())
                .projectId(INVALID_PROJECT_ID)
                .build();


        assertThatThrownBy(() -> buildSteps.createBuildConfig(invalidConfig))
                .as("Should throw ValidationException for invalid project")
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Cannot find project");

    }

    @Test
    @Description("Verifies that non-existent build config returns 404")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturn404ForNonExistentBuildConfig() {

        assertThatThrownBy(() -> buildSteps.getBuildConfig(NON_EXISTENT_ID))
                .as("Should throw ApiException with status 404")
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

    }

    @ParameterizedTest
    @MethodSource("provideProjectConfigurations")
    @Description("Verifies build config creation with different project configurations")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateBuildConfigWithVariousProjects(String projectId, boolean shouldSucceed, String expectedError) {
        BuildConfig config = dataFactory.createRandomBuildConfig(projectId);

        if (shouldSucceed) {

            BuildConfig created = buildSteps.createBuildConfig(config);
            trackBuildConfig(created.getId());

            assertThat(created.getProjectId())
                    .as("Project ID should match")
                    .isEqualTo(projectId);
            log.info("Build config created with project: {}", projectId);
        } else {

            assertThatThrownBy(() -> buildSteps.createBuildConfig(config))
                    .as("Should fail with appropriate exception for project: " + projectId)
                    .isInstanceOfAny(ValidationException.class, ApiException.class);
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
    @Description("Verifies that buildConfigExists method works correctly")
    @Severity(SeverityLevel.MINOR)
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

    }

    @Test
    @Description("Verifies that deleting non-existent build config is safe")
    @Severity(SeverityLevel.MINOR)
    void shouldHandleNonExistentDeletion() {
        buildSteps.deleteBuildConfigIfExists(NON_EXISTENT_ID);
        buildSteps.deleteBuildConfigIfExists(NON_EXISTENT_ID); // Повторный вызов тоже безопасен

        log.info("Non-existent build config deletion handled correctly");
    }

    private void createMultipleBuildConfigs(int count) {
        for (int i = 0; i < count; i++) {
            BuildConfig config = dataFactory.createRandomBuildConfig(testProjectId);
            BuildConfig created = buildSteps.createBuildConfig(config);
            trackBuildConfig(created.getId());
        }
    }
}