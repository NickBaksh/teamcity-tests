package com.teamcity.core.utils;

import com.teamcity.core.generators.RandomData;
import com.teamcity.core.generators.RandomModelGenerator;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;

/**
 * Фабрика тестовых данных. Делегирует генерацию в {@link RandomData} / {@link RandomModelGenerator}.
 */
public class TestDataFactory {

    public static final String DEFAULT_PASSWORD = "TestPass123!";

    public Project createRandomProject() {
        Project project = RandomModelGenerator.generate(Project.class);
        project.setId(null);
        project.setHref(null);
        project.setWebUrl(null);
        project.setParentProjectId("_Root");
        project.setDescription("Auto-generated project: " + project.getName());
        return project;
    }

    public Project createRandomProject(String parentProjectId) {
        Project project = createRandomProject();
        project.setParentProjectId(parentProjectId);
        return project;
    }

    public User createRandomUser() {
        User user = RandomModelGenerator.generate(User.class);
        user.setId(null);
        user.setHref(null);
        user.setPassword(DEFAULT_PASSWORD);
        user.setName("Test User " + RandomData.shortId());
        return user;
    }

    public User createUserWithEmail(String email) {
        User user = createRandomUser();
        user.setEmail(email);
        return user;
    }

    public User createUserWithPassword(String password) {
        User user = createRandomUser();
        user.setPassword(password);
        return user;
    }

    public User createMinimalUser() {
        return User.builder()
                .username(generateUniqueUsername())
                .password(DEFAULT_PASSWORD)
                .build();
    }

    public BuildConfig createRandomBuildConfig(String projectId) {
        BuildConfig config = RandomModelGenerator.generate(BuildConfig.class);
        config.setId(null);
        config.setHref(null);
        config.setWebUrl(null);
        config.setPaused(null);
        config.setProjectId(projectId);
        config.setDescription("Auto-generated build config: " + config.getName());
        return config;
    }

    public BuildConfig createBuildConfigWithDescription(String projectId, String description) {
        BuildConfig config = createRandomBuildConfig(projectId);
        config.setDescription(description);
        return config;
    }

    public Project createProjectWithName(String name) {
        Project project = createRandomProject();
        project.setName(name);
        return project;
    }

    public String generateUniqueName(String prefix) {
        return RandomData.unique(prefix);
    }

    public String generateUniqueProjectName() {
        return RandomData.unique("Project");
    }

    public String generateUniqueBuildConfigName() {
        return RandomData.unique("BuildConfig");
    }

    public String generateUniqueUsername() {
        return RandomData.unique("user");
    }

    public String generateUniqueEmail() {
        return RandomData.email();
    }

    public String randomString(int length) {
        return RandomData.string(length);
    }

    public String randomPassword() {
        return RandomData.password();
    }
}
