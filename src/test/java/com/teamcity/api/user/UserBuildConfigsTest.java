package com.teamcity.api.user;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.BuildConfig;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Build Configuration Management")
@Tag("user")
public class UserBuildConfigsTest extends BaseApiTest {
    private String testProjectId;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        testProjectId = givenProject().getId();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetBuildConfigById() {
        BuildConfig expected = givenBuildConfig(testProjectId);

        BuildConfig actual = givenUserBuildConfigSteps().getBuildConfig(expected.getId());

        ApiAssertions.assertBuildConfigsEqual(expected, actual);
        assertThat(actual.getProjectId()).isEqualTo(testProjectId);
    }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        void shouldNotCreateBuildConfig() {
            BuildConfig request = dataFactory.createRandomBuildConfig(testProjectId);

            ApiAssertions.assertForbidden(
                    () -> givenUserBuildConfigSteps().createBuildConfig(request)
            );
        }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotDeleteBuildConfig() {
        BuildConfig config = givenBuildConfig(testProjectId);

        ApiAssertions.assertForbidden(
                () -> givenUserBuildConfigSteps().deleteBuildConfig(config.getId())
        );
    }
}