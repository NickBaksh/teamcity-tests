package com.teamcity.core.steps;

import com.teamcity.api.specs.ResponseSpecs;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.File;
import com.teamcity.core.models.Files;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArtifactSteps {
    private final RestClient client;

    public ArtifactSteps(RestClient client) {
        this.client = client;
    }

    @Step("Get artifacts list for build: {buildId}")
    public Files getArtifacts(String buildId) {
        Response response = client.get(
                Endpoint.BUILD_ARTIFACTS.getPath(),
                buildId);

        response.then().spec(ResponseSpecs.requestReturnsOK());
        return response.as(Files.class);
    }

    @Step("Get artifact metadata: build={buildId}, path={artifactPath}")
    public File getArtifactMetadata(String buildId, String artifactPath) {
        Response response = client.get(
                Endpoint.BUILD_ARTIFACT_METADATA.getPath(),
                buildId,
                artifactPath);

        response.then().spec(ResponseSpecs.requestReturnsOK());
        return response.as(File.class);
    }

    @Step("Download artifact: build={buildId}, path={artifactPath}")
    public byte[] downloadArtifact(String buildId, String artifactPath) {
        Response response = client.get(
                Endpoint.BUILD_ARTIFACT_FILE.getPath(),
                buildId,
                artifactPath);

        response.then().spec(ResponseSpecs.requestReturnsOK());
        return response.asByteArray();
    }

    @Step("Download artifacts archive: build={buildId}, path={artifactPath}")
    public byte[] downloadArtifactsArchive(String buildId, String artifactPath) {
        Response response = client.get(
                Endpoint.BUILD_ARTIFACT_ARCHIVE.getPath(),
                buildId,
                artifactPath);

        response.then().spec(ResponseSpecs.requestReturnsOK());
        return response.asByteArray();
    }
}