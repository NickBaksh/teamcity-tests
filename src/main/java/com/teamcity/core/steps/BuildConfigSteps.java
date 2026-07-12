package com.teamcity.core.steps;

import com.teamcity.core.cleanup.CleanupRegistry;
import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RequestType;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.BuildType;
import com.teamcity.core.models.Project;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class BuildConfigSteps {

    private static final int DEFAULT_WAIT_INTERVAL_SECONDS = 2;

    private final ApiClient client;
    private final ResponseValidator validator;
    private final TestDataFactory dataFactory = new TestDataFactory();

    public BuildConfigSteps(ApiClient client) {
        this.client = client;
        this.validator = new ResponseValidator();
    }

    // ========================================================================
    // CREATE
    // ========================================================================

    @Step("Create build config: {config.name}")
    @Severity(SeverityLevel.BLOCKER)
    public BuildConfig create(BuildConfig config) {
        log.info("Creating build config: {}", config.getName());

        Response response = client.post(Endpoint.BUILD_TYPES.getPath(), config);
        BuildConfig created = validator.validate(response, BuildConfig.class);
        CleanupRegistry.get().register(() -> {

            try {
                delete(created.getId());
            } catch (Exception ignored) {
            }
        });

        log.info("Build config created: ID={}, Name={}", created.getId(), created.getName());
        return created;
    }

    @Step("Create random build config")
    public BuildConfig createRandomBuildConfig(Project project) {
        return create(
                dataFactory.createRandomBuildConfig(project.getId())
        );
    }

    // ========================================================================
    // READ
    // ========================================================================

    @Step("Get build config: {configId}")
    @Severity(SeverityLevel.BLOCKER)
    public BuildConfig get(String configId) {
        log.debug("Fetching build config: {}", configId);

        Response response = client.get(
                Endpoint.BUILD_TYPE.format(configId)
        );

        BuildConfig config = validator.validate(response, BuildConfig.class);

        log.debug("Build config fetched: ID={}, Name={}",
                config.getId(), config.getName());

        return config;
    }

    public BuildConfig get(BuildConfig config) {
        return get(config.getId());
    }

    @Step("Get build type: {buildTypeId}")
    @Severity(SeverityLevel.NORMAL)
    public BuildType getBuildType(String buildTypeId) {

        Response response = client.get(
                Endpoint.BUILD_TYPE.format(buildTypeId),
                RequestType.JSON
        );

        return validator.validate(response, BuildType.class);
    }

    @Step("Get all build configs")
    @Severity(SeverityLevel.NORMAL)
    public List<BuildConfig> getAll() {

        Response response = client.get(
                Endpoint.BUILD_TYPES.getPath()
        );

        List<BuildConfig> configs = validator.validate(
                response,
                res -> res.jsonPath().getList("buildType", BuildConfig.class)
        );

        return configs != null
                ? configs
                : Collections.emptyList();
    }

    @Step("Get build configs by project: {projectId}")
    @Severity(SeverityLevel.NORMAL)
    public List<BuildConfig> getByProject(String projectId) {

        String endpoint = String.format(
                "%s?locator=project:(id:%s)",
                Endpoint.BUILD_TYPES.getPath(),
                projectId
        );

        Response response = client.get(endpoint);

        List<BuildConfig> configs = validator.validate(
                response,
                res -> res.jsonPath().getList("buildType", BuildConfig.class)
        );

        return configs != null
                ? configs
                : Collections.emptyList();
    }

    public List<BuildConfig> getByProject(Project project) {
        return getByProject(project.getId());
    }

    @Step("Check build config exists: {configId}")
    public boolean exists(String configId) {
        try {
            get(configId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exists(BuildConfig config) {
        return exists(config.getId());
    }

    // ========================================================================
    // UPDATE
    // ========================================================================

    @Step("Update build config name: {configId}")
    @Severity(SeverityLevel.NORMAL)
    public BuildConfig updateName(String configId, String newName) {

        Response response = client.putText(
                Endpoint.BUILD_TYPE_NAME.format(configId),
                newName
        );

        validator.validateStatus(response);

        return get(configId);
    }

    public BuildConfig updateName(BuildConfig config, String newName) {
        return updateName(config.getId(), newName);
    }

    @Step("Update build config description: {configId}")
    @Severity(SeverityLevel.NORMAL)
    public BuildConfig updateDescription(String configId, String description) {

        Response response = client.putText(
                Endpoint.BUILD_TYPE.format(configId) + "/description",
                description
        );

        validator.validateStatus(response);

        return get(configId);
    }

    public BuildConfig updateDescription(BuildConfig config, String description) {
        return updateDescription(config.getId(), description);
    }

    // ========================================================================
    // DELETE
    // ========================================================================

    @Step("Delete build config: {configId}")
    @Severity(SeverityLevel.BLOCKER)
    public void delete(String configId) {

        Response response = client.delete(
                Endpoint.BUILD_TYPE.format(configId)
        );

        validator.validateStatus(response);

        log.info("Build config deleted: {}", configId);
    }

    public void delete(BuildConfig config) {
        delete(config.getId());
    }

    @Step("Delete build config if exists: {configId}")
    public boolean deleteIfExists(String configId) {

        if (!exists(configId)) {
            return false;
        }

        delete(configId);
        return true;
    }

    public boolean deleteIfExists(BuildConfig config) {
        return deleteIfExists(config.getId());
    }

    // ========================================================================
    // PAUSE / RESUME
    // ========================================================================

    @Step("Pause build config: {configId}")
    public void pause(String configId) {

        Response response = client.putBoolean(
                Endpoint.BUILD_TYPE_PAUSED.format(configId),
                true
        );

        validator.validateStatus(response);
    }

    public void pause(BuildConfig config) {
        pause(config.getId());
    }

    @Step("Resume build config: {configId}")
    public void resume(String configId) {

        Response response = client.putBoolean(
                Endpoint.BUILD_TYPE_PAUSED.format(configId),
                false
        );

        validator.validateStatus(response);
    }

    public void resume(BuildConfig config) {
        resume(config.getId());
    }

    @Step("Check build config paused: {configId}")
    public boolean isPaused(String configId) {

        BuildConfig config = get(configId);

        return Boolean.TRUE.equals(config.getPaused());
    }

    public boolean isPaused(BuildConfig config) {
        return isPaused(config.getId());
    }

    @Step("Toggle build config pause: {configId}")
    public void togglePause(String configId) {

        if (isPaused(configId)) {
            resume(configId);
        } else {
            pause(configId);
        }
    }

    public void togglePause(BuildConfig config) {
        togglePause(config.getId());
    }

    // ========================================================================
    // WAIT
    // ========================================================================

    @Step("Wait until build config is ready: {configId}")
    public BuildConfig waitUntilReady(String configId, int maxWaitSeconds) {

        int attempts = maxWaitSeconds / DEFAULT_WAIT_INTERVAL_SECONDS;

        for (int i = 0; i < attempts; i++) {

            try {
                return get(configId);

            } catch (Exception ignored) {

                sleep(DEFAULT_WAIT_INTERVAL_SECONDS);
            }
        }

        throw new RuntimeException(
                String.format(
                        "Build config %s is not ready after %d seconds",
                        configId,
                        maxWaitSeconds
                )
        );
    }

    public BuildConfig waitUntilReady(BuildConfig config, int maxWaitSeconds) {
        return waitUntilReady(config.getId(), maxWaitSeconds);
    }

    private void sleep(int seconds) {

        try {
            Thread.sleep(seconds * 1000L);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new RuntimeException("Waiting interrupted", e);
        }
    }

    // ========================================================================
    // SEARCH
    // ========================================================================

    @Step("Find build config by name: {name}")
    @Severity(SeverityLevel.NORMAL)
    public Optional<BuildConfig> findByName(String name) {

        return getAll().stream()
                .filter(config -> name.equals(config.getName()))
                .findFirst();
    }

    @Step("Find build configs by prefix: {prefix}")
    @Severity(SeverityLevel.NORMAL)
    public List<BuildConfig> findByPrefix(String prefix) {

        return getAll().stream()
                .filter(config ->
                        config.getName() != null &&
                                config.getName().startsWith(prefix))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // UTILS
    // ========================================================================

    public String getHref(String configId) {
        return String.format(
                "/app/rest/buildTypes/id:%s",
                configId
        );
    }

    public String getHref(BuildConfig config) {
        return getHref(config.getId());
    }

    @Step("Add command line step to build config: {buildConfigId}")
    public void addCommandLineStep(String buildConfigId, String script) {

        Map<String, Object> body = Map.of(
                "name", "Create artifact",
                "type", "simpleRunner",
                "properties",
                Map.of(
                        "property",
                        List.of(
                                Map.of(
                                        "name", "command.executable",
                                        "value", "bash"
                                ),
                                Map.of(
                                        "name", "script.content",
                                        "value", script
                                )
                        )
                )
        );

        Response response = client.post(
                Endpoint.BUILD_TYPE_STEPS.getPath()
                        .replace("{btLocator}", buildConfigId),
                body
        );

        validator.validateStatus(response);

        log.info("Command line step added to build config: {}", buildConfigId);
    }
}