package com.teamcity.core.testdata;

import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.utils.TestDataFactory;

/**
 * Явные payload'ы для негативных и граничных кейсов.
 * Happy-path остаётся в {@link TestDataFactory}.
 */
public final class InvalidTestData {

    private static final String EDGE_EMAIL = "test@test.com";
    private static final String EDGE_PROJECT_DESCRIPTION = "Invalid/edge project name case";
    private static final String REJECTED_BUILD_CONFIG_DESCRIPTION = "Should be rejected";
    private static final String EDGE_BUILD_CONFIG_DESCRIPTION = "Edge-case build config name";

    private InvalidTestData() {
    }

    public static User userWithEmptyUsername() {
        return User.builder()
                .username("")
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .email(EDGE_EMAIL)
                .build();
    }

    public static User userWithUsername(String username) {
        return User.builder()
                .username(username)
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .email(EDGE_EMAIL)
                .build();
    }

    public static Project projectWithName(String name) {
        return Project.builder()
                .name(name)
                .parentProjectId(TestDataValues.ROOT_PROJECT_ID)
                .description(EDGE_PROJECT_DESCRIPTION)
                .build();
    }

    public static BuildConfig buildConfigWithEmptyName(String projectId) {
        return BuildConfig.builder()
                .name("")
                .projectId(projectId)
                .description(REJECTED_BUILD_CONFIG_DESCRIPTION)
                .build();
    }

    public static BuildConfig buildConfigWithName(String projectId, String name) {
        return BuildConfig.builder()
                .name(name)
                .projectId(projectId)
                .description(EDGE_BUILD_CONFIG_DESCRIPTION)
                .build();
    }

    public static BuildConfig buildConfigForMissingProject(String buildConfigName, String projectId) {
        return BuildConfig.builder()
                .name(buildConfigName)
                .projectId(projectId)
                .build();
    }
}
