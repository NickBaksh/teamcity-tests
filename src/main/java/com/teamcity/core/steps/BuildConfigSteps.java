package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RequestType;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.BuildConfig;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
public class BuildConfigSteps extends BaseSteps {

    public BuildConfigSteps(ApiClient client) {
        super(client);
    }

    public BuildConfigSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    @Step("Create build config: {config.name}")
    public BuildConfig createBuildConfig(BuildConfig config) {
        Response response = client.post(Endpoint.BUILD_TYPES.getPath(), config);
        BuildConfig created = validator.validate(response, BuildConfig.class);
        log.info("Build config created: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    @Step("Get build config: {configId}")
    public BuildConfig getBuildConfig(String configId) {
        Response response = client.get(Endpoint.BUILD_TYPE.format(configId), RequestType.JSON);
        return validator.validate(response, BuildConfig.class);
    }

    @Step("Get all build configs")
    public List<BuildConfig> getAllBuildConfigs() {
        Response response = client.get(Endpoint.BUILD_TYPES.getPath());
        List<BuildConfig> configs = validator.validate(
                response,
                res -> res.jsonPath().getList("buildType", BuildConfig.class)
        );
        return configs != null ? configs : Collections.emptyList();
    }

    @Step("Update build config name: {configId} -> {newName}")
    public BuildConfig updateBuildConfig(String configId, String newName) {
        Response response = client.putText(Endpoint.BUILD_TYPE_NAME.format(configId), newName);
        validator.validateStatus(response);
        return getBuildConfig(configId);
    }

    @Step("Delete build config: {configId}")
    public void deleteBuildConfig(String configId) {
        Response response = client.delete(Endpoint.BUILD_TYPE.format(configId));
        validator.validateStatus(response);
        log.info("Build config deleted: {}", configId);
    }

    @Step("Delete build config if exists: {configId}")
    public boolean deleteBuildConfigIfExists(String configId) {
        if (!buildConfigExists(configId)) {
            return false;
        }
        deleteBuildConfig(configId);
        return true;
    }

    @Step("Check build config exists: {configId}")
    public boolean buildConfigExists(String configId) {
        try {
            getBuildConfig(configId);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (RuntimeException e) {
            log.debug("buildConfigExists({}) -> false: {}", configId, e.getMessage());
            return false;
        }
    }

    @Step("Pause build config: {configId}")
    public void pauseBuildConfig(String configId) {
        setBuildConfigPaused(configId, true);
    }

    @Step("Resume build config: {configId}")
    public void resumeBuildConfig(String configId) {
        setBuildConfigPaused(configId, false);
    }

    @Step("Set build config paused: {configId}={paused}")
    public void setBuildConfigPaused(String configId, boolean paused) {
        Response response = client.putBoolean(Endpoint.BUILD_TYPE_PAUSED.getPath(), paused, configId);
        validator.validateStatus(response);
    }

    @Step("Wait until build config paused: {buildConfigId}")
    public BuildConfig waitUntilPaused(String buildConfigId) {
        return Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> getBuildConfig(buildConfigId), config -> Boolean.TRUE.equals(config.getPaused()));
    }

    @Step("Wait until build config resumed: {buildConfigId}")
    public BuildConfig waitUntilResumed(String buildConfigId) {
        return Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> getBuildConfig(buildConfigId), config -> !Boolean.TRUE.equals(config.getPaused()));
    }
}
