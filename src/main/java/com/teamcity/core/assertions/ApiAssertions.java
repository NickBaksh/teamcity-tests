package com.teamcity.core.assertions;

import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert;

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
}
