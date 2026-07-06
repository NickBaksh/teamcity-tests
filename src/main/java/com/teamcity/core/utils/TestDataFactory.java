package com.teamcity.core.utils;

import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Build;

import java.util.UUID;

public class TestDataFactory {

    public Project createRandomProject() {
        String name = "Project_" + System.currentTimeMillis();
        return Project.builder()
                .name(name)
                .description("Auto-generated project: " + name)
                .parentProjectId("_Root")
                .build();
    }

    public Project createRandomProject(String parentProjectId) {
        String name = "Project_" + System.currentTimeMillis();
        return Project.builder()
                .name(name)
                .description("Auto-generated project: " + name)
                .parentProjectId(parentProjectId)
                .build();
    }

    public User createRandomUser() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "testuser_" + timestamp;
        return User.builder()
                .username(username)
                .password("TestPass123!")
                .email("test_" + timestamp + "@example.com")
                .name("Test User " + timestamp)
                .build();
    }

    public BuildConfig createRandomBuildConfig(String projectId) {
        String name = "BuildConfig_" + System.currentTimeMillis();
        return BuildConfig.builder()
                .name(name)
                .projectId(projectId)
                .description("Auto-generated build config: " + name)
                .build();
    }
}