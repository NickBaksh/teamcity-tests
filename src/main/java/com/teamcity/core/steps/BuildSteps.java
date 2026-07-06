package com.teamcity.core.steps;

import com.teamcity.core.client.RestClient;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.dto.BuildCancelRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class BuildSteps {
    private final RestClient client;

    public BuildSteps(RestClient client) {
        this.client = client;
    }

    @Step("Create build config: {config.name}")
    public BuildConfig createBuildConfig(BuildConfig config) {
        Response response = client.post("/app/rest/buildTypes", config);
        assertEquals(200, response.statusCode(), "Failed to create build config: " + response.getBody().asString());

        BuildConfig created = response.as(BuildConfig.class);
        assertNotNull(created.getId(), "Build config ID is null");
        assertEquals(config.getName(), created.getName(), "Build config name mismatch");
        assertEquals(config.getProjectId(), created.getProjectId(), "Project ID mismatch");

        log.info("Build config created: ID={}", created.getId());
        return created;
    }

    @Step("Get build config by ID: {configId}")
    public BuildConfig getBuildConfig(String configId) {
        Response response = client.get("/app/rest/buildTypes/{btLocator}", configId);
        assertEquals(200, response.statusCode(), "Failed to get build config");

        return response.as(BuildConfig.class);
    }

    @Step("Delete build config: {configId}")
    public void deleteBuildConfig(String configId) {
        Response response = client.delete("/app/rest/buildTypes/{btLocator}", configId);
        assertEquals(204, response.statusCode(), "Failed to delete build config");
        log.info("Build config deleted: ID={}", configId);
    }

    @Step("Check if build config exists: {configId}")
    public boolean buildConfigExists(String configId) {
        Response response = client.get("/app/rest/buildTypes/{btLocator}", configId);
        return response.statusCode() == 200;
    }

    @Step("Run build for config: {buildTypeId}")
    public Build runBuild(String buildTypeId) {
        Build build = Build.builder()
                .buildTypeId(buildTypeId)
                .build();

        Response response = client.post("/app/rest/buildQueue", build);
        assertEquals(200, response.statusCode(), "Failed to run build: " + response.getBody().asString());

        Build created = response.as(Build.class);
        assertNotNull(created.getId(), "Build ID should not be null");

        log.info("Build started: ID={}", created.getId());
        return created;
    }

    @Step("Get build by ID: {buildId}")
    public Build getBuild(String buildId) {
        Response response = client.get("/app/rest/builds/{buildLocator}", buildId);
        assertEquals(200, response.statusCode(), "Failed to get build");

        return response.as(Build.class);
    }

    @Step("Cancel build: {buildId}")
    public void cancelBuild(String buildId, String comment) {
        BuildCancelRequest request = new BuildCancelRequest();
        request.setComment(comment);

        Response response = client.post("/app/rest/builds/{buildLocator}", request, buildId);
        assertEquals(200, response.statusCode(), "Failed to cancel build");
        log.info("Build canceled: ID={}", buildId);
    }
}