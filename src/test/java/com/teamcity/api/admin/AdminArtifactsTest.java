package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AdminArtifactsTest extends BaseApiTest {

    @Test
    @DisplayName("Получить пустой список артефактов сборки")
    public void adminCanGetEmptyArtifactsListTest() {

        Project project =
                projectSteps(adminClient())
                        .createRandomProject();

        BuildConfig buildConfig =
                buildConfigSteps(adminClient())
                        .createRandomBuildConfig(project);

        Build build =
                buildSteps(adminClient())
                        .runBuildAndWait(buildConfig);

        assertThat(build.getStatus())
                .isEqualToIgnoringCase("SUCCESS");

        Files artifacts =
                artifactSteps(adminClient())
                        .getArtifacts(build.getId().toString());

        assertThat(artifacts.getCount()).isZero();
        assertThat(artifacts.getFile()).isEmpty();
    }

    @Test
    @DisplayName("Получить список артефактов сборки")
    public void adminCanGetBuildArtifactsListTest() {

        Project project =
                projectSteps(adminClient())
                        .createRandomProject();

        BuildConfig config =
                dataFactory.createRandomBuildConfig(project.getId());

        config.setArtifactRules("artifact.txt");

        BuildConfig createdConfig =
                buildConfigSteps(adminClient()).createRandomBuildConfig(project);

        buildConfigSteps(adminClient())
                .addCommandLineStep(
                        createdConfig.getId(),
                        "echo 'hello artifact' > artifact.txt"
                );

        Build finished =
                buildSteps(adminClient())
                        .runBuildAndWait(createdConfig);

        assertThat(finished.getState())
                .isEqualToIgnoringCase("finished");

        Files artifacts =
                artifactSteps(adminClient())
                        .getArtifacts(finished.getId().toString());

        assertThat(artifacts).isNotNull();

        assertThat(artifacts.getFile())
                .isNotEmpty();

        assertThat(artifacts.getFile())
                .extracting(File::getName)
                .contains("artifact.txt");
    }

    @Test
    @DisplayName("Получить артефакты несуществующей сборки")
    public void adminCannotGetArtifactsFromNonExistingBuildTest() {

        String buildId = dataFactory.generateNotExistingBuildId();

        assertThatThrownBy(() ->
                artifactSteps(adminClient())
                        .getArtifacts(buildId)
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Скачать конкретный артефакт")
    public void adminCanDownloadArtifactTest() {
        // TODO
    }

    @Test
    @DisplayName("Скачать несуществующий артефакт")
    public void adminCanNotDownloadNonExistingArtifactTest() {
        // TODO
    }

    @Test
    @DisplayName("Скачать все артефакты архивом")
    public void adminCanDownloadAllArtifactsAsArchiveTest() {
        // TODO
    }

    @Test
    @DisplayName("Получить метаданные артефакта")
    public void adminCanGetArtifactMetadataTest() {
        // TODO
    }
}