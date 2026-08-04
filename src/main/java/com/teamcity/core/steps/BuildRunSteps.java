package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.dto.BuildCancelRequest;
import com.teamcity.core.models.dto.RunBuildRequest;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class BuildRunSteps extends BaseSteps {

    public BuildRunSteps(ApiClient client) {
        super(client);
    }

    public BuildRunSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    @Step("Run build: {buildTypeId}")
    public Build runBuild(String buildTypeId) {
        return runBuild(
                RunBuildRequest.builder()
                        .buildTypeId(buildTypeId)
                        .build()
        );
    }

    @Step("Run build with parameters: {buildTypeId}")
    public Build runBuild(String buildTypeId, Map<String, String> parameters) {
        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(buildTypeId)
                .parameters(parameters)
                .build();
        Response response = client.post(Endpoint.BUILD_QUEUE.getPath(), request);
        return validator.validate(response, Build.class);
    }

    @Step("Run build")
    public Build runBuild(RunBuildRequest request) {
        Response response = client.post(
                Endpoint.BUILD_QUEUE.getPath(),
                request
        );
        Build created = validator.validate(response, Build.class);

        log.info("Build triggered: id={}, state={}",
                created.getId(),
                created.getState());
        return created;
    }

    @Step("Get build: {buildId}")
    public Build getBuild(String buildId) {
        Response response = client.get(Endpoint.BUILD.format(buildId));
        return validator.validate(response, Build.class);
    }

    @Step("Get builds for config: {buildTypeId}")
    public List<Build> getBuildsForConfig(String buildTypeId) {
        String endpoint = Endpoint.BUILDS.getPath() + "?locator=buildType:" + buildTypeId;
        Response response = client.get(endpoint);
        List<Build> builds = validator.validate(
                response,
                res -> res.jsonPath().getList("build", Build.class)
        );
        return builds != null ? builds : Collections.emptyList();
    }

    @Step("Get latest build for config: {buildTypeId}")
    public Build getLatestBuild(String buildTypeId) {
        return getBuildsForConfig(buildTypeId)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No builds found for config: " + buildTypeId));
    }

    @Step("Cancel build: {buildId}")
    public void cancelBuild(String buildId, String comment) {
        BuildCancelRequest request = new BuildCancelRequest();
        request.setComment(comment);
        Response response = client.post(Endpoint.BUILD.format(buildId), request);
        // Queued builds may reject cancel-on-build; drop them from the queue instead.
        if (response.getStatusCode() >= 400) {
            Response queueDelete = client.delete(Endpoint.BUILD_QUEUE_ITEM.format("id:" + buildId));
            if (queueDelete.getStatusCode() < 400) {
                log.info("Build {} removed from queue after cancel failed with {}",
                        buildId, response.getStatusCode());
                return;
            }
        }
        validator.validateStatus(response);
    }

    @Step("Cancel build: {buildId}")
    public void cancelBuild(String buildId) {
        cancelBuild(buildId, "Cancelled by API test");
    }

    @Step("Delete build: {buildId}")
    public void deleteBuild(String buildId) {
        Response response = client.delete(
                Endpoint.BUILD.format(buildId)
        );
        validator.validateStatus(response);
    }

    @Step("Wait for build state: {expectedState}")

    public Build waitForBuildState(String buildId, String expectedState, int timeoutSeconds) {
        return Awaitility.await()
                .atMost(Duration.ofSeconds(timeoutSeconds))
                .until(
                        () -> getBuild(buildId),
                        build -> expectedState.equalsIgnoreCase(build.getState())
                );
    }

    @Step("Wait for build finish: {buildId}")
    public Build waitForBuildFinish(String buildId) {
        int timeout = ConfigManager.getBuildTimeout();
        Awaitility.await()
                .atMost(Duration.ofSeconds(timeout))
                .pollInterval(Duration.ofMillis(ConfigManager.getBuildPollInterval()))
                .ignoreExceptions()
                .until(() -> isBuildFinished(getBuild(buildId)));

        try {
            return Awaitility.await()
                    .atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .ignoreExceptions()
                    .until(() -> getBuild(buildId), this::hasResolvedBuildStatus);
        } catch (ConditionTimeoutException ex) {
            log.warn("Build {} finished but status stayed UNKNOWN after settle wait", buildId);
            return getBuild(buildId);
        }
    }

    private boolean isBuildFinished(Build build) {
        return build != null
                && TestDataValues.BUILD_STATE_FINISHED.equalsIgnoreCase(build.getState());
    }

    private boolean hasResolvedBuildStatus(Build build) {
        String status = build.getStatus();
        return status != null
                && !status.isBlank()
                && !TestDataValues.BUILD_STATUS_UNKNOWN.equalsIgnoreCase(status);
    }
}
