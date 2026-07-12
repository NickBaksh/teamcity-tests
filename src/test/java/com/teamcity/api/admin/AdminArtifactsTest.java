package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    //FOR ME TO DELETE ALL
    @Test
    @DisplayName("Cleanup TeamCity")
    void cleanupTeamCity() {

        // ---------- BUILD CONFIGS ----------
        Response buildConfigsResponse = adminClient().get(Endpoint.BUILD_TYPES.getPath());

        List<Map<String, Object>> buildConfigs =
                buildConfigsResponse.jsonPath().getList("buildType");

        for (Map<String, Object> buildConfig : buildConfigs) {

            String id = (String) buildConfig.get("id");
            String projectId = (String) buildConfig.get("projectId");

            // не трогаем Build Config основного проекта
            if ("NBank".equals(projectId)) {
                continue;
            }

            System.out.println("DELETE BUILD CONFIG: " + id);

            adminClient().delete(
                    Endpoint.BUILD_TYPE.format(id)
            );
        }

        // ---------- PROJECTS ----------
        Response projectsResponse = adminClient().get(Endpoint.PROJECTS.getPath());

        List<Map<String, Object>> projects =
                projectsResponse.jsonPath().getList("project");

        for (Map<String, Object> project : projects) {

            String id = (String) project.get("id");

            // не удаляем Root и основной проект
            if ("_Root".equals(id) || "NBank".equals(id)) {
                continue;
            }

            System.out.println("DELETE PROJECT: " + id);

            adminClient().delete(
                    Endpoint.PROJECT.format(id)
            );
        }

        // ---------- USERS ----------
        Response usersResponse = adminClient().get(Endpoint.USERS.getPath());

        List<Map<String, Object>> users =
                usersResponse.jsonPath().getList("user");

        for (Map<String, Object> user : users) {

            String username = (String) user.get("username");
            Integer id = (Integer) user.get("id");

            // системного админа не трогаем
            if ("admin".equals(username)) {
                continue;
            }

            System.out.println("DELETE USER: " + username + " (" + id + ")");

            try {

                adminClient().delete(Endpoint.USER.format(id));

            } catch (ResourceNotFoundException e) {

                System.out.println("User already deleted: " + username);

            }
        }
    }
}