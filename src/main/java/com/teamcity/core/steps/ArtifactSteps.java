package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.File;
import com.teamcity.core.models.Files;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArtifactSteps extends BaseSteps {
    public ArtifactSteps(ApiClient client) {
        super(client);
    }

    public ArtifactSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    @Step("Get artifacts list for build: {buildId}")
    public Files getArtifacts(String buildId) {
        Response response = client.get(
                Endpoint.BUILD_ARTIFACTS.format(buildId)
        );
        return validator.validate(response, Files.class);
    }

    @Step("Get artifact metadata: build={buildId}, path={artifactPath}")
    public File getArtifactMetadata(String buildId, String artifactPath) {
        Response response = client.get(
                Endpoint.BUILD_ARTIFACT_METADATA.format(
                        buildId,
                        normalizePath(artifactPath)
                )
        );
        return validator.validate(response, File.class);
    }

    @Step("Download artifact: build={buildId}, path={artifactPath}")
    public byte[] downloadArtifact(String buildId, String artifactPath) {
        Response response = client.get(
                Endpoint.BUILD_ARTIFACT_FILE.format(
                        buildId,
                        normalizePath(artifactPath)
                )
        );

        validator.validateStatus(response);
        return response.asByteArray();
    }

    @Step("Download artifacts archive: build={buildId}, path={artifactPath}")
    public byte[] downloadArtifactsArchive(String buildId, String artifactPath) {
        Response response = client.get(
                Endpoint.BUILD_ARTIFACT_ARCHIVE.format(
                        buildId,
                        normalizePath(artifactPath)
                )
        );

        validator.validateStatus(response);
        return response.asByteArray();
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}