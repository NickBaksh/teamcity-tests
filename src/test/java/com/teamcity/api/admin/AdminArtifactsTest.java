package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.api.specs.ResponseSpecs;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.*;
import com.teamcity.core.steps.ArtifactSteps;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AdminArtifactsTest extends BaseApiTest {
    private ArtifactSteps artifactSteps;

    @Override

    @BeforeEach
    public void setUp() {
        super.setUp();
        artifactSteps = new ArtifactSteps(adminClient);
    }

    @Test
    @DisplayName("Получить пустой список артефактов сборки")
    public void adminCanGetEmptyArtifactsListTest() {
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject());

        BuildConfig buildConfig = adminBuildSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(project.getId()));

        trackBuildConfig(buildConfig.getId());

        Build build = adminBuildSteps.runBuildAndWait(buildConfig.getId());

        assertThat(build.getStatus())
                .isEqualToIgnoringCase("SUCCESS");

        // Получаем список артефактов
        Files artifacts = adminArtifactSteps.getArtifacts(String.valueOf(build.getId()));

        assertThat(artifacts.getCount()).isZero();
        assertThat(artifacts.getFile()).isEmpty();
        assertThat(artifacts.getCount())
                .isZero();
    }

    @Test
    @DisplayName("Получить список артефактов сборки")
    public void adminCanGetBuildArtifactsListTest() {
        // 1. Создаем проект
        Project project = adminProjectSteps.createProject(
                dataFactory.createRandomProject()
        );

        trackProject(project.getId());

        // 2. Создаем BuildConfig с artifactRules
        BuildConfig config =
                dataFactory.createRandomBuildConfig(project.getId());
        config.setArtifactRules("artifact.txt");

        BuildConfig createdConfig =
                adminBuildSteps.createBuildConfig(config);

        trackBuildConfig(createdConfig.getId());

        // 3. Добавляем шаг, который создаст artifact.txt
        adminBuildSteps.addCommandLineStep(
                createdConfig.getId(),
                "echo 'hello artifact' > artifact.txt"
        );

        // 4. Запускаем билд
        Build build =
                adminBuildSteps.runBuild(createdConfig.getId());

        // 5. Ждем завершения
        Build finished =
                adminBuildSteps.waitForBuildFinish(String.valueOf(build.getId()));

        assertThat(finished.getState())
                .as("Build should be finished")
                .isEqualToIgnoringCase("finished");

        // 6. Проверяем артефакты
        Files artifacts =
                artifactSteps.getArtifacts(
                        String.valueOf(finished.getId())
                );

        assertThat(artifacts)
                .as("Artifacts should not be null")
                .isNotNull();

        assertThat(artifacts.getFile())
                .as("Artifact list should not be empty")
                .isNotEmpty();
        assertThat(artifacts.getFile())
                .extracting(com.teamcity.core.models.File::getName)
                .contains("artifact.txt");
    }

    @Test
    @DisplayName("Получить артефакты несуществующей сборки")
    void adminCannotGetArtifactsFromNonExistingBuildTest() {
        String buildId = dataFactory.generateNotExistingBuildId();
        assertThatThrownBy(() ->
                adminArtifactSteps.getArtifacts(buildId)
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Скачать конкретный артефакт")
    public void adminCanDownloadArtifactTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Скачать несуществующий артефакт")
    public void adminCanNotDownloadNonExistingArtifactTest() {
        // Expected: 404 Not Found
    }

    @Test
    @DisplayName("Скачать все артефакты архивом")
    public void adminCanDownloadAllArtifactsAsArchiveTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Получить метаданные артефакта")
    public void adminCanGetArtifactMetadataTest() {
        // Expected: 200 OK
    }
}
