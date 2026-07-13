package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.dto.BuildCancelRequest;
import com.teamcity.core.models.dto.RunBuildRequest;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BuildSteps {

    public static final int DEFAULT_WAIT_TIMEOUT_SECONDS = 50;
    private static final int DEFAULT_WAIT_INTERVAL_SECONDS = 2;
    private static final int DEFAULT_MAX_WAIT_SECONDS = 300;
    private static final String DEFAULT_CANCEL_COMMENT = "Canceled by API test";

    public static final String STATE_RUNNING = "running";
    public static final String STATE_FINISHED = "finished";
    public static final String STATUS_FAILED = "FAILURE";
    public static final String STATUS_UNKNOWN = "UNKNOWN";
    public static final String STATUS_TEXT_CANCELED = "Canceled";
    public static final String STATUS_SUCCESS = "SUCCESS";

    private final ApiClient client;
    private final ResponseValidator validator;

    public BuildSteps(ApiClient client) {
        this.client = client;
        this.validator = new ResponseValidator();
    }

    // =========================================================================
    // RUN
    // =========================================================================

    @Step("Run build: {buildTypeId}")
    @Severity(SeverityLevel.BLOCKER)
    public Build runBuild(String buildTypeId) {

        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(buildTypeId)
                .build();

        Response response = client.post(
                Endpoint.BUILD_QUEUE.getPath(),
                request
        );

        return validator.validate(response, Build.class);
    }

    public Build runBuild(BuildConfig config) {
        return runBuild(config.getId());
    }

    @Step("Run build with parameters: {buildTypeId}")
    public Build runBuild(String buildTypeId,
                          Map<String, String> parameters) {

        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(buildTypeId)
                .parameters(parameters)
                .build();

        Response response = client.post(
                Endpoint.BUILD_QUEUE.getPath(),
                request
        );

        return validator.validate(response, Build.class);
    }

    @Step("Run build on branch: {buildTypeId}")
    public Build runBuildOnBranch(String buildTypeId,
                                  String branch) {

        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(buildTypeId)
                .branchName(branch)
                .build();

        Response response = client.post(
                Endpoint.BUILD_QUEUE.getPath(),
                request
        );

        return validator.validate(response, Build.class);
    }

    @Step("Run build and wait: {buildTypeId}")
    public Build runBuildAndWait(String buildTypeId) {

        Build build = runBuild(buildTypeId);

        return waitForBuildFinish(
                build.getId()
        );
    }

    public Build runBuildAndWait(BuildConfig config) {
        return runBuildAndWait(config.getId());
    }

    // =========================================================================
    // GET
    // =========================================================================

    @Step("Get build: {buildId}")
    @Severity(SeverityLevel.BLOCKER)
    public Build getBuild(Long buildId) {

        Response response = client.get(
                Endpoint.BUILD.format(String.valueOf(buildId))
        );

        return validator.validate(response, Build.class);
    }

    public Build getBuild(String buildId) {
        return getBuild(Long.valueOf(buildId));
    }

    @Step("Get builds: {buildTypeId}")
    public List<Build> getBuilds(String buildTypeId) {

        String endpoint = String.format(
                "%s?locator=buildType:%s",
                Endpoint.BUILDS.getPath(),
                buildTypeId
        );

        Response response = client.get(endpoint);

        List<Build> builds = validator.validate(
                response,
                res -> res.jsonPath().getList("build", Build.class)
        );

        return builds != null
                ? builds
                : Collections.emptyList();
    }

    @Step("Get last build: {buildTypeId}")
    public Build getLastBuild(String buildTypeId) {

        List<Build> builds = getBuilds(buildTypeId);

        return builds.isEmpty()
                ? null
                : builds.getFirst();
    }

    // =========================================================================
    // CANCEL
    // =========================================================================

    @Step("Cancel build: {buildId}")
    public void cancelBuild(Long buildId) {
        cancelBuild(String.valueOf(buildId), DEFAULT_CANCEL_COMMENT);
    }

    @Step("Cancel build: {buildId}")
    public void cancelBuild(String buildId, String comment) {

        BuildCancelRequest request = new BuildCancelRequest();
        request.setComment(comment);

        Response response = client.post(
                Endpoint.BUILD.format(buildId),
                request
        );

        validator.validateStatus(response);
    }

    @Step("Delete build: {buildId}")
    public void deleteBuild(String buildId) {

        Response response = client.delete(
                Endpoint.BUILD.format(buildId)
        );

        validator.validateStatus(response);
    }

    // =========================================================================
    // WAIT
    // =========================================================================

    @Step("Wait for build finish: {buildId}")
    @Severity(SeverityLevel.CRITICAL)
    public Build waitForBuildFinish(Long buildId) {
        return waitForBuildFinish(
                String.valueOf(buildId),
                DEFAULT_MAX_WAIT_SECONDS
        );
    }

    @Step("Wait for build finish: {buildId}")
    @Severity(SeverityLevel.CRITICAL)
    public Build waitForBuildFinish(String buildId,
                                    int maxWaitSeconds) {

        int attempts = maxWaitSeconds / DEFAULT_WAIT_INTERVAL_SECONDS;

        for (int i = 0; i < attempts; i++) {

            Build build = getBuild(buildId);

            if (BuildStatus.isFinished(build.getState())) {
                return build;
            }

            if (BuildStatus.isFailed(build.getState())) {
                return build;
            }

            sleep(DEFAULT_WAIT_INTERVAL_SECONDS);
        }

        return getBuild(buildId);
    }

    @Step("Wait for build state: {buildId}")
    public Build waitForBuildState(Long buildId,
                                   String expectedState,
                                   int maxWaitSeconds) {

        int attempts = maxWaitSeconds / DEFAULT_WAIT_INTERVAL_SECONDS;

        for (int i = 0; i < attempts; i++) {

            Build build = getBuild(buildId);

            if (expectedState.equalsIgnoreCase(build.getState())) {
                return build;
            }

            sleep(DEFAULT_WAIT_INTERVAL_SECONDS);
        }

        throw new RuntimeException(
                String.format(
                        "Build %s did not reach state %s within %d seconds",
                        buildId,
                        expectedState,
                        maxWaitSeconds
                )
        );
    }

    public Build waitForBuildState(long buildId, String state) {
        return waitForBuildState(
                buildId,
                state,
                DEFAULT_MAX_WAIT_SECONDS
        );
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    @Step("Check build exists: {buildId}")
    public boolean buildExists(String buildId) {

        try {
            getBuild(buildId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Find builds by status: {buildTypeId}")
    public List<Build> findByStatus(String buildTypeId,
                                    String status) {

        return getBuilds(buildTypeId)
                .stream()
                .filter(build ->
                        status.equalsIgnoreCase(build.getStatus()))
                .toList();
    }

    public List<Build> findSuccessful(String buildTypeId) {
        return findByStatus(buildTypeId, "SUCCESS");
    }

    public List<Build> findFailed(String buildTypeId) {
        return findByStatus(buildTypeId, "FAILURE");
    }

    // =========================================================================
    // NEGATIVE
    // =========================================================================

    @Step("Run build (negative): {buildTypeId}")
    public Response runBuildForbidden(String buildTypeId) {

        return client.post(
                Endpoint.BUILD_QUEUE.getPath(),
                RunBuildRequest.builder()
                        .buildTypeId(buildTypeId)
                        .build()
        );
    }

    @Step("Cancel build (negative): {buildId}")
    public Response cancelBuildForbidden(Long buildId) {

        BuildCancelRequest request = new BuildCancelRequest();
        request.setComment(DEFAULT_CANCEL_COMMENT);

        return client.post(
                Endpoint.BUILD.format(String.valueOf(buildId)),
                request
        );
    }

    @Step("Delete build (negative): {buildId}")
    public Response deleteBuildForbidden(Long buildId) {

        return client.delete(
                Endpoint.BUILD.format(String.valueOf(buildId))
        );
    }

    // =========================================================================
    // PRIVATE
    // =========================================================================

    private void sleep(int seconds) {

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    // =========================================================================
    // ENUM
    // =========================================================================

    public enum BuildStatus {

        QUEUED("queued"),
        RUNNING("running"),
        FINISHED("finished"),
        FAILED("failed"),
        CANCELLED("cancelled");

        private final String value;

        BuildStatus(String value) {
            this.value = value;
        }

        public static boolean isFinished(String state) {
            return FINISHED.value.equalsIgnoreCase(state)
                    || FAILED.value.equalsIgnoreCase(state)
                    || CANCELLED.value.equalsIgnoreCase(state);
        }

        public static boolean isFailed(String state) {
            return FAILED.value.equalsIgnoreCase(state)
                    || CANCELLED.value.equalsIgnoreCase(state);
        }

        public static boolean isRunning(String state) {
            return RUNNING.value.equalsIgnoreCase(state)
                    || QUEUED.value.equalsIgnoreCase(state);
        }
    }
}