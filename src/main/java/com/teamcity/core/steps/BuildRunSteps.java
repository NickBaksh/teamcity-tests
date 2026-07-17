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

    @Step("Cancel build: {buildId}")
    public void cancelBuild(String buildId, String comment) {
        BuildCancelRequest request = new BuildCancelRequest();
        request.setComment(comment);
        Response response = client.post(Endpoint.BUILD.format(buildId), request);
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
        return Awaitility.await()
                .atMost(Duration.ofSeconds(timeout))
                .pollInterval(Duration.ofMillis(ConfigManager.getBuildPollInterval()))
                .until(() -> getBuild(buildId), build -> {
                    String state = build.getState();
                    return TestDataValues.BUILD_STATE_FINISHED.equalsIgnoreCase(state) || TestDataValues.BUILD_STATUS_FAILED.equalsIgnoreCase(state);
                });
    }
}
