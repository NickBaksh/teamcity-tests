package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.DuplicateResourceException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.exceptions.ValidationException;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.steps.BuildSteps;
import com.teamcity.core.steps.ProjectSteps;
import io.qameta.allure.*;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
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
@DisplayName("Build Configuration Management Tests")
public class AdminBuildConfigsTest extends BaseApiTest {

    private static final String NON_EXISTENT_ID = "non-existent-id-12345";
    private static final String INVALID_PROJECT_ID = "invalid-project-id";
    private static final String ROOT_PROJECT_ID = "_Root";

    private String testProjectId;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        Project project = projectSteps(adminClient())
                .createRandomProject();

        testProjectId = project.getId();
    }

    @AfterEach
    void tearDown() {

        buildConfigSteps(adminClient())
                .getAll()
                .stream()
                .filter(config -> testProjectId.equals(config.getProjectId()))
                .forEach(buildConfigSteps(adminClient())::deleteIfExists);

        projectSteps(adminClient())
                .deleteProjectIfExists(testProjectId);
    }

    // ========================================================================
    // CREATE
    // ========================================================================

    @Test
    @Order(1)
    @DisplayName("Create build config")
    void shouldCreateBuildConfigWithValidData() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig actual = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(actual.getId()).isNotBlank();
        softly.assertThat(actual.getName()).isNotBlank();
        softly.assertThat(actual.getProjectId()).isEqualTo(project.getId());
        softly.assertThat(actual.getHref()).isNotBlank();

        softly.assertAll();
    }

    @Test
    @Order(2)
    @DisplayName("Get build config by id")
    void shouldGetBuildConfigById() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig expected = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        BuildConfig actual = buildConfigSteps(adminClient())
                .get(expected);

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getProjectId()).isEqualTo(expected.getProjectId());
    }

    @Test
    @Order(3)
    @DisplayName("Delete build config")
    void shouldDeleteBuildConfig() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig created = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        buildConfigSteps(adminClient())
                .delete(created);

        assertThat(
                buildConfigSteps(adminClient())
                        .exists(created)
        ).isFalse();
    }

    @Test
    @Order(4)
    @DisplayName("Get all build configs")
    void shouldGetAllBuildConfigs() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        buildConfigSteps(adminClient()).createRandomBuildConfig(project);
        buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        List<BuildConfig> configs = buildConfigSteps(adminClient())
                .getAll();

        assertThat(configs).isNotNull();
        assertThat(configs.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(5)
    @DisplayName("Update build config name")
    void shouldUpdateBuildConfigName() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig created = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        String newName = "Updated_" + System.currentTimeMillis();

        BuildConfig updated = buildConfigSteps(adminClient())
                .updateName(created, newName);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo(newName);
    }

    @Test
    @Order(6)
    @DisplayName("Pause build config")
    void shouldPauseBuildConfig() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig created = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        buildConfigSteps(adminClient())
                .pause(created);

        BuildConfig paused = buildConfigSteps(adminClient())
                .get(created);

        assertThat(paused.getPaused()).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("Resume build config")
    void shouldResumeBuildConfig() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig created = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(
                buildConfigSteps(adminClient())
                        .get(created)
                        .getPaused()
        ).isFalse();

        buildConfigSteps(adminClient())
                .pause(created);

        softly.assertThat(
                buildConfigSteps(adminClient())
                        .get(created)
                        .getPaused()
        ).isTrue();

        softly.assertThatCode(() ->
                buildConfigSteps(adminClient())
                        .pause(created)
        ).doesNotThrowAnyException();

        softly.assertThat(
                buildConfigSteps(adminClient())
                        .get(created)
                        .getPaused()
        ).isTrue();

        buildConfigSteps(adminClient())
                .resume(created);

        softly.assertThat(
                buildConfigSteps(adminClient())
                        .get(created)
                        .getPaused()
        ).isFalse();

        softly.assertThatCode(() ->
                buildConfigSteps(adminClient())
                        .resume(created)
        ).doesNotThrowAnyException();

        softly.assertThat(
                buildConfigSteps(adminClient())
                        .get(created)
                        .getPaused()
        ).isFalse();

        softly.assertAll();
    }

    @Test
    @Order(8)
    @DisplayName("Build config with description")
    void shouldCreateBuildConfigWithDescription() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig config = dataFactory.createRandomBuildConfig(project.getId());
        config.setDescription("This is a test build config");

        BuildConfig created = buildConfigSteps(adminClient())
                .create(config);

        assertThat(created.getDescription())
                .isEqualTo("This is a test build config");
    }

    @Test
    @Order(9)
    @DisplayName("Create build config with invalid name")
    void shouldNotCreateBuildConfigWithInvalidName() {

        BuildConfig invalid = BuildConfig.builder()
                .name("")
                .projectId(testProjectId)
                .build();

        assertThatThrownBy(() ->
                buildConfigSteps(adminClient()).create(invalid)
        ).isInstanceOfAny(
                ValidationException.class,
                ApiException.class
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t"})
    @Order(10)
    @DisplayName("Create build config with blank name")
    void shouldNotCreateBuildConfigWithBlankName(String name) {

        BuildConfig invalid = BuildConfig.builder()
                .name(name)
                .projectId(testProjectId)
                .build();

        assertThatThrownBy(() ->
                buildConfigSteps(adminClient()).create(invalid)
        ).isInstanceOfAny(
                ValidationException.class,
                ApiException.class
        );
    }

    @Test
    @Order(11)
    @DisplayName("Duplicate build config")
    void shouldNotCreateBuildConfigWithDuplicateName() {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig created = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        BuildConfig duplicate = BuildConfig.builder()
                .name(created.getName())
                .projectId(project.getId())
                .build();

        assertThatThrownBy(() ->
                buildConfigSteps(adminClient()).create(duplicate)
        ).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @Order(12)
    @DisplayName("Invalid project id")
    void shouldNotCreateBuildConfigWithInvalidProjectId() {

        BuildConfig invalid = BuildConfig.builder()
                .name("Build_" + System.currentTimeMillis())
                .projectId(INVALID_PROJECT_ID)
                .build();

        assertThatThrownBy(() ->
                buildConfigSteps(adminClient()).create(invalid)
        ).isInstanceOfAny(
                ValidationException.class,
                ApiException.class
        );
    }

    @Test
    @Order(13)
    @DisplayName("Get non existing build config")
    void shouldReturn404ForNonExistentBuildConfig() {

        assertThatThrownBy(() ->
                buildConfigSteps(adminClient()).get(NON_EXISTENT_ID)
        ).isInstanceOf(ApiException.class);
    }

    @ParameterizedTest
    @MethodSource("provideProjectConfigurations")
    @Order(14)
    @DisplayName("Create build config with different projects")
    void shouldCreateBuildConfigWithVariousProjects(
            String projectId,
            boolean shouldSucceed,
            String expectedError) {

        BuildConfig config = BuildConfig.builder()
                .name("Build_" + System.currentTimeMillis())
                .projectId(projectId)
                .build();

        if (shouldSucceed) {

            BuildConfig created = buildConfigSteps(adminClient())
                    .create(config);

            assertThat(created.getProjectId())
                    .isEqualTo(projectId);

        } else {

            assertThatThrownBy(() ->
                    buildConfigSteps(adminClient()).create(config)
            ).isInstanceOfAny(
                    ValidationException.class,
                    ApiException.class
            );
        }
    }
    static Stream<Arguments> provideProjectConfigurations() {

        return Stream.of(
                Arguments.of(ROOT_PROJECT_ID, false, ""),
                Arguments.of(INVALID_PROJECT_ID, false, ""),
                Arguments.of("", false, ""),
                Arguments.of(null, false, "")
        );
    }


    @Test
    @DisplayName("Build config exists")

    void shouldVerifyBuildConfigExists() {
        assertThat(
                buildConfigSteps(adminClient())
                        .exists(NON_EXISTENT_ID)
        ).isFalse();

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        BuildConfig created = buildConfigSteps(adminClient())
                .createRandomBuildConfig(project);

        assertThat(
                buildConfigSteps(adminClient())
                        .exists(created)
        ).isTrue();
    }

    @Test
    @DisplayName("Delete non existing build config")
    void shouldHandleNonExistentDeletion() {

        buildConfigSteps(adminClient())
                .deleteIfExists(NON_EXISTENT_ID);

        buildConfigSteps(adminClient())
                .deleteIfExists(NON_EXISTENT_ID);
    }

    private void createMultipleBuildConfigs(int count) {

        Project project = projectSteps(adminClient())
                .getProject(testProjectId);

        for (int i = 0; i < count; i++) {
            buildConfigSteps(adminClient())
                    .createRandomBuildConfig(project);
        }
    }
}