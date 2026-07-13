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

    private InvalidTestData() {
    }

    public static User userWithEmptyUsername() {
        return User.builder()
                .username("")
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .email("test@test.com")
                .build();
    }

    public static User userWithUsername(String username) {
        return User.builder()
                .username(username)
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .email("test@test.com")
                .build();
    }

    public static Project projectWithName(String name) {
        return Project.builder()
                .name(name)
                .parentProjectId("_Root")
                .description("Invalid/edge project name case")
                .build();
    }

    public static BuildConfig buildConfigWithEmptyName(String projectId) {
        return BuildConfig.builder()
                .name("")
                .projectId(projectId)
                .description("Should be rejected")
                .build();
    }

    public static BuildConfig buildConfigWithName(String projectId, String name) {
        return BuildConfig.builder()
                .name(name)
                .projectId(projectId)
                .description("Edge-case build config name")
                .build();
    }

    public static BuildConfig buildConfigForMissingProject(String buildConfigName, String projectId) {
        return BuildConfig.builder()
                .name(buildConfigName)
                .projectId(projectId)
                .build();
    }
}
