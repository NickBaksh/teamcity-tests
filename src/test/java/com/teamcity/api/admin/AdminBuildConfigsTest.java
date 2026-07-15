package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.ValidationException;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.testdata.InvalidTestData;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Feature("Build Configuration Management")
@Tag("admin")
public class AdminBuildConfigsTest extends BaseApiTest {

    private String testProjectId;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        testProjectId = givenProject().getId();
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldCreateBuildConfigWithValidData() {
        BuildConfig request = dataFactory.createRandomBuildConfig(testProjectId);

        BuildConfig created = givenBuildConfig(request);

        ApiAssertions.assertBuildConfigCreated(request, created);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldGetBuildConfigById() {
        BuildConfig created = givenBuildConfig(testProjectId);

        BuildConfig retrieved = buildConfigSteps.getBuildConfig(created.getId());

        ApiAssertions.assertBuildConfigsEqual(created, retrieved);
        assertThat(retrieved.getProjectId()).isEqualTo(testProjectId);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldDeleteBuildConfig() {
        BuildConfig created = buildConfigSteps.createBuildConfig(
                dataFactory.createRandomBuildConfig(testProjectId));

        buildConfigSteps.deleteBuildConfig(created.getId());

        assertThat(buildConfigSteps.buildConfigExists(created.getId())).isFalse();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldGetAllBuildConfigs() {
        givenBuildConfig(testProjectId);
        givenBuildConfig(testProjectId);

        List<BuildConfig> configs = buildConfigSteps.getAllBuildConfigs();

        assertThat(configs).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldUpdateBuildConfigName() {
        BuildConfig created = givenBuildConfig(testProjectId);
        String newName = dataFactory.generateUniqueBuildConfigName();

        buildConfigSteps.updateBuildConfig(created.getId(), newName);
        BuildConfig reloaded = buildConfigSteps.getBuildConfig(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(reloaded.getId()).isEqualTo(created.getId());
        softly.assertThat(reloaded.getName()).isEqualTo(newName);
        softly.assertThat(reloaded.getProjectId()).isEqualTo(testProjectId);
        softly.assertAll();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldPauseBuildConfig() {
        BuildConfig created = givenBuildConfig(testProjectId);

        buildConfigSteps.setBuildConfigPaused(created.getId(), true);
        BuildConfig paused = buildConfigSteps.getBuildConfig(created.getId());

        assertThat(paused.getPaused()).isTrue();
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldResumeBuildConfig() {
        BuildConfig created = givenBuildConfig(testProjectId);

        buildConfigSteps.pauseBuildConfig(created.getId());
        assertThat(buildConfigSteps.getBuildConfig(created.getId()).getPaused()).isTrue();

        buildConfigSteps.resumeBuildConfig(created.getId());
        BuildConfig resumed = buildConfigSteps.getBuildConfig(created.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Boolean.TRUE.equals(resumed.getPaused())).isFalse();
        softly.assertThat(resumed.getId()).isEqualTo(created.getId());
        softly.assertThat(resumed.getName()).isEqualTo(created.getName());
        softly.assertAll();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateBuildConfigWithDescription() {
        BuildConfig request = dataFactory.createBuildConfigWithDescription(
                testProjectId, TestDataValues.BUILD_CONFIG_DESCRIPTION);

        BuildConfig created = givenBuildConfig(request);

        assertThat(created.getDescription()).isEqualTo(TestDataValues.BUILD_CONFIG_DESCRIPTION);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateBuildConfigWithEmptyName() {
        BuildConfig invalid = InvalidTestData.buildConfigWithEmptyName(testProjectId);

        assertThatThrownBy(() -> buildConfigSteps.createBuildConfig(invalid))
                .isInstanceOf(ValidationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t", "\n", "\r", "  ", " \t "})
    @Severity(SeverityLevel.NORMAL)
    @Description("Documents TeamCity behavior: whitespace names are accepted")
    void shouldCreateBuildConfigWithWhitespaceName(String whitespaceName) {
        BuildConfig request = InvalidTestData.buildConfigWithName(testProjectId, whitespaceName);

        BuildConfig created = givenBuildConfig(request);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getId()).isNotBlank();
        softly.assertThat(created.getName()).isEqualTo(whitespaceName);
        softly.assertThat(created.getProjectId()).isEqualTo(testProjectId);
        softly.assertAll();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateBuildConfigWithDuplicateName() {
        BuildConfig request = dataFactory.createRandomBuildConfig(testProjectId);
        givenBuildConfig(request);

        ApiAssertions.assertDuplicate(() -> buildConfigSteps.createBuildConfig(request));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateBuildConfigWithInvalidProjectId() {
        BuildConfig invalid = InvalidTestData.buildConfigForMissingProject(
                dataFactory.generateUniqueBuildConfigName(),
                TestDataValues.INVALID_PROJECT_ID);

        assertThatThrownBy(() -> buildConfigSteps.createBuildConfig(invalid))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(TestDataValues.MSG_PROJECT_NOT_FOUND);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturn404ForNonExistentBuildConfig() {
        ApiAssertions.assertNotFound(() -> buildConfigSteps.getBuildConfig(TestDataValues.NON_EXISTENT_ID));
    }

    @ParameterizedTest
    @MethodSource("invalidProjectLocators")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectBuildConfigForInvalidProject(String projectId) {
        BuildConfig request = dataFactory.createRandomBuildConfig(projectId);

        assertThatThrownBy(() -> buildConfigSteps.createBuildConfig(request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    void shouldVerifyBuildConfigExists() {
        assertThat(buildConfigSteps.buildConfigExists(TestDataValues.NON_EXISTENT_ID)).isFalse();

        BuildConfig created = givenBuildConfig(testProjectId);

        assertThat(buildConfigSteps.buildConfigExists(created.getId())).isTrue();
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    void shouldHandleNonExistentDeletion() {
        buildConfigSteps.deleteBuildConfigIfExists(TestDataValues.NON_EXISTENT_ID);
        buildConfigSteps.deleteBuildConfigIfExists(TestDataValues.NON_EXISTENT_ID);
    }

    static Stream<Arguments> invalidProjectLocators() {
        return Stream.of(
                Arguments.of(TestDataValues.ROOT_PROJECT_ID),
                Arguments.of(TestDataValues.INVALID_PROJECT_ID),
                Arguments.of(""),
                Arguments.of((String) null)
        );
    }
}
