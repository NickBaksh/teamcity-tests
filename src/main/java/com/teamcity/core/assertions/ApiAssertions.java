package com.teamcity.core.assertions;

import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.models.*;
import com.teamcity.core.models.dto.AuthorizedInfo;
import com.teamcity.core.models.dto.EnabledInfo;
import com.teamcity.core.testdata.TestDataValues;
import org.apache.http.HttpStatus;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Доменные проверки для тестов: модели, списки, HTTP-ошибки.
 * Steps делают действия — assertions остаются в тестах через этот класс.
 */
public final class ApiAssertions {

    private ApiAssertions() {
    }

    public static void assertProjectCreated(Project request, Project actual) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual).as("Created project").isNotNull();
        softly.assertThat(actual.getId()).as("Project id").isNotBlank();
        softly.assertThat(actual.getHref()).as("Project href").isNotBlank();
        softly.assertThat(actual.getWebUrl()).as("Project webUrl").isNotBlank();
        softly.assertThat(actual.getArchived()).as("Project archived").isIn(false, null);
        ModelAssertions.assertModelsMatch(softly, request, actual);
        softly.assertAll();
    }

    public static void assertProjectsEqual(Project expected, Project actual) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual).as("Project").isNotNull();
        softly.assertThat(actual.getHref()).as("Project href").isNotBlank();
        ModelAssertions.assertModelsMatch(softly, expected, actual);
        softly.assertAll();
    }

    public static void assertBuildConfigCreated(BuildConfig request, BuildConfig actual) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual).as("Created build config").isNotNull();
        softly.assertThat(actual.getId()).as("Build config id").isNotBlank();
        softly.assertThat(actual.getHref()).as("Build config href").isNotBlank();
        ModelAssertions.assertModelsMatch(softly, request, actual);
        softly.assertAll();
    }

    public static void assertBuildConfigsEqual(BuildConfig expected, BuildConfig actual) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual).as("Build config").isNotNull();
        ModelAssertions.assertModelsMatch(softly, expected, actual);
        softly.assertAll();
    }

    public static void assertUserCreated(User request, User actual) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual).as("Created user").isNotNull();
        softly.assertThat(actual.getId()).as("User id").isNotNull();
        softly.assertThat(actual.getHref()).as("User href").isNotBlank();
        ModelAssertions.assertModelsMatch(softly, request, actual);
        softly.assertAll();
    }

    public static void assertUsersEqual(User expected, User actual) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual).as("User").isNotNull();
        ModelAssertions.assertModelsMatch(softly, expected, actual);
        softly.assertAll();
    }

    public static void assertBuildTriggered(Build build) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(build).as("Build").isNotNull();
        softly.assertThat(build.getId()).as("Build id").isNotNull();
        softly.assertThat(build.getBuildTypeId()).as("Build type id").isNotBlank();
        softly.assertAll();
    }

    public static void assertBuildFinished(Build build, String expectedId, String status) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(build).isNotNull();
        softly.assertThat(build.getId()).isEqualTo(expectedId);
        softly.assertThat(build.getState())
                .isEqualTo(TestDataValues.BUILD_STATE_FINISHED);
        softly.assertThat(build.getStatus()).isEqualTo(status);
        softly.assertAll();
    }

    public static void assertBuildState(Build build, String... allowedStates) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(build).as("Build").isNotNull();
        softly.assertThat(build.getState()).as("Build state").isIn((Object[]) allowedStates);
        softly.assertAll();
    }

    public static void assertNotFound(ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action)
                .as("Expected HTTP 404")
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);
    }

    public static void assertForbidden(ThrowableAssert.ThrowingCallable action) {
        assertStatus(action, HttpStatus.SC_FORBIDDEN);
    }

    public static void assertUnauthorized(ThrowableAssert.ThrowingCallable action) {
        assertStatus(action, HttpStatus.SC_UNAUTHORIZED);
    }

    public static void assertBadRequest(ThrowableAssert.ThrowingCallable action) {
        assertStatus(action, HttpStatus.SC_BAD_REQUEST);
    }

    public static void assertStatus(ThrowableAssert.ThrowingCallable action, int expectedStatus) {
        assertThatThrownBy(action)
                .as("Expected HTTP " + expectedStatus)
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatusCode()).isEqualTo(expectedStatus));
    }

    public static void assertDuplicate(ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action)
                .as("Expected duplicate resource")
                .isInstanceOf(com.teamcity.core.exceptions.DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    public static void assertAgentsEqual(Agent expected, Agent actual) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual)
                .as("Agent")
                .isNotNull();
        ModelAssertions.assertModelsMatch(
                softly,
                expected,
                actual
        );
        softly.assertAll();
    }

    public static void assertAgentEnabled(EnabledInfo enabledInfo) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(enabledInfo)
                .as("Enabled info")
                .isNotNull();

        softly.assertThat(enabledInfo.getStatus())
                .as("Agent enabled")
                .isTrue();

        softly.assertAll();
    }

    public static void assertAgentDisabled(EnabledInfo enabledInfo) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(enabledInfo)
                .as("Enabled info")
                .isNotNull();
        softly.assertThat(enabledInfo.getStatus())
                .as("Agent disabled")
                .isFalse();

        softly.assertAll();
    }

    public static void assertAgentAuthorized(AuthorizedInfo authorizedInfo) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(authorizedInfo)
                .as("Authorized info")
                .isNotNull();
        softly.assertThat(authorizedInfo.getStatus())
                .as("Agent authorized")
                .isTrue();
        softly.assertAll();
    }

    public static void assertArtifactsExist(Files artifacts) {
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(artifacts)
                .as("Artifacts response")
                .isNotNull();
        softly.assertThat(artifacts.getCount())
                .as("Artifacts count")
                .isGreaterThan(0);

        softly.assertThat(artifacts.getFile())
                .as("Artifacts list")
                .isNotEmpty();
        softly.assertAll();
    }

    public static void assertArtifactsEmpty(Files artifacts) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(artifacts)
                .as("Artifacts response")
                .isNotNull();

        softly.assertThat(artifacts.getCount())
                .as("Artifacts count")
                .isZero();

        softly.assertThat(artifacts.getFile())
                .as("Artifacts list")
                .isEmpty();

        softly.assertAll();
    }

    public static void assertArtifactContent(byte[] content) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(content)
                .as("Downloaded artifact content")
                .isNotNull()
                .isNotEmpty();

        String artifactText = new String(content, StandardCharsets.UTF_8);

        softly.assertThat(artifactText)
                .as("Downloaded artifact text")
                .contains(TestDataValues.SUREFIRE_REPORT_MARKER);

        softly.assertAll();
    }

    public static void assertArchiveContent(byte[] archive) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(archive)
                .as("Downloaded archive")
                .isNotNull()
                .isNotEmpty();
        softly.assertAll();
    }

    public static void assertArtifactMetadata(File expected, File actual) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual)
                .as("Artifact metadata")
                .isNotNull();
        softly.assertThat(actual.getName())
                .as("Artifact name")
                .isEqualTo(expected.getName());
        softly.assertThat(actual.getSize())
                .as("Artifact size")
                .isEqualTo(expected.getSize());
        softly.assertThat(actual.getHref())
                .as("Artifact href")
                .isNotBlank();
        softly.assertAll();
    }

    public static void assertArtifact(File artifact) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(artifact)
                .as("Artifact")
                .isNotNull();
        softly.assertThat(artifact.getName())
                .as("Artifact name")
                .isNotBlank();
        softly.assertAll();
    }
}
