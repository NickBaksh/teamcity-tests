package com.teamcity.core.utils;

import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;
import com.teamcity.core.models.VcsRoot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestDataFactory {

    public static final String DEFAULT_PASSWORD = "TestPass123!";
    public static final String DEFAULT_BRANCH = "main";
    public static final int DEFAULT_TIMEOUT_SECONDS = 120;
    public static final String TEST_PREFIX = "Test_";

    // =========================================================================
    // СУЩЕСТВУЮЩИЕ МЕТОДЫ (НЕ МЕНЯЕМ)
    // =========================================================================

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
                .password(DEFAULT_PASSWORD)
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

    public String generateUniqueName(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    public String generateUniqueProjectName() {
        return "Project_" + System.currentTimeMillis();
    }

    public String generateUniqueBuildConfigName() {
        return "BuildConfig_" + System.currentTimeMillis();
    }

    public String generateUniqueUsername() {
        return "user_" + System.currentTimeMillis();
    }

    public String generateUniqueEmail() {
        return "test_" + System.currentTimeMillis() + "@example.com";
    }

    public String randomString(int length) {
        return UUID.randomUUID().toString().substring(0, Math.min(length, 36));
    }

    public String randomPassword() {
        return "P@ssw0rd_" + randomString(6);
    }

    public String generateProjectId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + randomString(6);
    }

    public String generateProjectId() {
        return generateProjectId("Project");
    }

    public String generateProjectName(String prefix) {
        return prefix + " " + System.currentTimeMillis();
    }

    public String generateProjectName() {
        return generateProjectName("Test Project");
    }

    public String generateBuildConfigId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + randomString(6);
    }

    public String generateBuildConfigId() {
        return generateBuildConfigId("BuildConfig");
    }

    public String generateBuildConfigName(String prefix) {
        return prefix + " " + System.currentTimeMillis();
    }

    public String generateBuildConfigName() {
        return generateBuildConfigName("Test Build Config");
    }

    public String generateUsername(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + randomString(6);
    }

    public String generateUsername() {
        return generateUsername("TestUser");
    }

    public String generateEmail(String username) {
        return username + "@test.com";
    }

    public String generateBranch() {
        return "test-branch-" + System.currentTimeMillis();
    }

    public String generateTestPrefix() {
        return TEST_PREFIX + System.currentTimeMillis() + "_";
    }

    public TestEnvironmentData generateTestEnvironmentData() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String shortId = randomString(6);

        return new TestEnvironmentData(
                "Project_" + timestamp + "_" + shortId,
                "Test Project " + timestamp,
                "BuildConfig_" + timestamp + "_" + shortId,
                "Test Build Config " + timestamp,
                "test-branch-" + timestamp,
                DEFAULT_TIMEOUT_SECONDS,
                "Test description " + timestamp
        );
    }

    public TestEnvironmentData generateTestEnvironmentData(String projectName, String configName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String shortId = randomString(6);

        return new TestEnvironmentData(
                "Project_" + timestamp + "_" + shortId,
                projectName + " " + timestamp,
                "BuildConfig_" + timestamp + "_" + shortId,
                configName + " " + timestamp,
                "test-branch-" + timestamp,
                DEFAULT_TIMEOUT_SECONDS,
                "Test environment: " + projectName + " / " + configName
        );
    }

    // =========================================================================
    // НОВЫЕ МЕТОДЫ ДЛЯ VCS ROOTS
    // =========================================================================

    /**
     * Генерирует уникальный ID для VCS Root
     */
    public String generateVcsRootId() {
        return "VcsRoot_" + System.currentTimeMillis() + "_" + randomString(6);
    }

    /**
     * Генерирует уникальный ID для VCS Root с указанным префиксом
     */
    public String generateVcsRootId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + randomString(6);
    }

    /**
     * Генерирует уникальное имя для VCS Root
     */
    public String generateVcsRootName() {
        return "Test VCS Root " + System.currentTimeMillis();
    }

    /**
     * Генерирует уникальное имя для VCS Root с указанным префиксом
     */
    public String generateVcsRootName(String prefix) {
        return prefix + " " + System.currentTimeMillis();
    }

    /**
     * Генерирует URL для Git репозитория
     */
    public String generateGitUrl() {
        String repoName = "test-repo-" + randomString(8);
        return "https://github.com/example/" + repoName + ".git";
    }

    /**
     * Генерирует URL для GitHub репозитория
     */
    public String generateGitHubUrl() {
        String repoName = "test-repo-" + randomString(8);
        return "https://github.com/teamcity-tests/" + repoName + ".git";
    }

    /**
     * Генерирует URL для GitLab репозитория
     */
    public String generateGitLabUrl() {
        String repoName = "test-repo-" + randomString(8);
        return "https://gitlab.com/teamcity-tests/" + repoName + ".git";
    }

    /**
     * Генерирует URL для Subversion репозитория
     */
    public String generateSvnUrl() {
        return "https://svn.example.com/repos/test-project-" + System.currentTimeMillis();
    }

    /**
     * Генерирует URL для Perforce репозитория
     */
    public String generatePerforceUrl() {
        return "perforce:1666//depot/test-project-" + System.currentTimeMillis();
    }

    /**
     * Генерирует URL для TFS репозитория
     */
    public String generateTfsUrl() {
        return "https://tfs.example.com/tfs/DefaultCollection";
    }

    /**
     * Генерирует метод аутентификации
     */
    public String generateAuthMethod() {
        String[] methods = {"ANONYMOUS", "PASSWORD", "TOKEN", "SSH_KEY"};
        return methods[randomInt(0, methods.length - 1)];
    }

    /**
     * Генерирует пароль для VCS
     */
    public String generateVcsPassword() {
        return "VcsP@ssw0rd_" + randomString(8) + "!";
    }

    /**
     * Генерирует токен для VCS
     */
    public String generateVcsToken() {
        return "vcs_token_" + randomString(32);
    }

    /**
     * Генерирует workspace для Perforce
     */
    public String generateWorkspace() {
        return "workspace_" + randomString(8) + "_" + System.currentTimeMillis();
    }

    /**
     * Генерирует view для Perforce
     */
    public String generatePerforceView() {
        return "//depot/test-project-" + System.currentTimeMillis() + "/... //workspace/...";
    }

    /**
     * Генерирует путь к проекту для TFS
     */
    public String generateProjectPath() {
        return "$/TestProject/" + randomString(8);
    }

    /**
     * Генерирует теги для SVN
     */
    public String generateSvnTags() {
        return "tags/" + randomString(8);
    }

    /**
     * Генерирует свойства для Git VCS Root
     */
    public Map<String, String> generateGitVcsProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("authMethod", generateAuthMethod());
        properties.put("branch", generateVcsBranch());
        properties.put("url", generateGitUrl());
        properties.put("username", generateVcsUsername());
        properties.put("password", generateVcsPassword());
        properties.put("teamcity.git.fetchAllHeads", String.valueOf(randomBoolean()));
        properties.put("teamcity.git.useSsh", String.valueOf(randomBoolean()));
        properties.put("ignoreKnownHosts", String.valueOf(randomBoolean()));
        return properties;
    }

    /**
     * Генерирует свойства для GitHub VCS Root
     */
    public Map<String, String> generateGitHubVcsProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("authMethod", "TOKEN");
        properties.put("branch", generateVcsBranch());
        properties.put("url", generateGitHubUrl());
        properties.put("token", generateVcsToken());
        properties.put("user", generateVcsUsername());
        return properties;
    }

    /**
     * Генерирует свойства для GitLab VCS Root
     */
    public Map<String, String> generateGitLabVcsProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("authMethod", "PASSWORD");
        properties.put("branch", generateVcsBranch());
        properties.put("url", generateGitLabUrl());
        properties.put("username", generateVcsUsername());
        properties.put("password", generateVcsPassword());
        return properties;
    }

    /**
     * Генерирует свойства для Subversion VCS Root
     */
    public Map<String, String> generateSvnVcsProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("authMethod", "PASSWORD");
        properties.put("url", generateSvnUrl());
        properties.put("username", generateVcsUsername());
        properties.put("password", generateVcsPassword());
        properties.put("svn.branch", generateVcsBranch());
        properties.put("svn.tags", generateSvnTags());
        return properties;
    }

    /**
     * Генерирует свойства для Perforce VCS Root
     */
    public Map<String, String> generatePerforceVcsProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("authMethod", "PASSWORD");
        properties.put("url", generatePerforceUrl());
        properties.put("username", generateVcsUsername());
        properties.put("password", generateVcsPassword());
        properties.put("workspace", generateWorkspace());
        properties.put("view", generatePerforceView());
        return properties;
    }

    /**
     * Генерирует свойства для TFS VCS Root
     */
    public Map<String, String> generateTfsVcsProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("authMethod", "PASSWORD");
        properties.put("url", generateTfsUrl());
        properties.put("username", generateVcsUsername());
        properties.put("password", generateVcsPassword());
        properties.put("projectPath", generateProjectPath());
        properties.put("branch", generateVcsBranch());
        return properties;
    }

    /**
     * Генерирует свойства для VCS в зависимости от типа
     */
    public Map<String, String> generateVcsProperties(String vcsName) {
        if (vcsName == null) {
            return generateGitVcsProperties();
        }

        switch (vcsName.toLowerCase()) {
            case "jetbrains.git":
            case "git":
                return generateGitVcsProperties();
            case "github":
                return generateGitHubVcsProperties();
            case "gitlab":
                return generateGitLabVcsProperties();
            case "svn":
            case "subversion":
                return generateSvnVcsProperties();
            case "perforce":
                return generatePerforceVcsProperties();
            case "tfs":
                return generateTfsVcsProperties();
            default:
                return generateGitVcsProperties();
        }
    }

    /**
     * Генерирует VCS Root с автоматическими свойствами
     */
    public VcsRoot generateVcsRoot(String projectId, String vcsName) {
        String id = generateVcsRootId();
        String name = generateVcsRootName();
        Map<String, String> properties = generateVcsProperties(vcsName);

        return VcsRoot.builder()
                .id(id)
                .name(name)
                .vcsName(vcsName)
                .project(Project.builder().id(projectId).build())
                .properties(properties)
                .build();
    }

    /**
     * Генерирует VCS Root с указанным ID и именем
     */
    public VcsRoot generateVcsRoot(String projectId, String vcsName, String id, String name) {
        Map<String, String> properties = generateVcsProperties(vcsName);

        return VcsRoot.builder()
                .id(id)
                .name(name)
                .vcsName(vcsName)
                .project(Project.builder().id(projectId).build())
                .properties(properties)
                .build();
    }

    /**
     * Генерирует VCS Root с пользовательскими свойствами
     */
    public VcsRoot generateVcsRoot(String projectId, String vcsName, Map<String, String> properties) {
        String id = generateVcsRootId();
        String name = generateVcsRootName();

        return VcsRoot.builder()
                .id(id)
                .name(name)
                .vcsName(vcsName)
                .project(Project.builder().id(projectId).build())
                .properties(properties)
                .build();
    }

    // =========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ VCS
    // =========================================================================

    /**
     * Генерирует имя пользователя для VCS
     */
    public String generateVcsUsername() {
        return "vcs_user_" + randomString(6);
    }

    /**
     * Генерирует ветку для VCS
     */
    public String generateVcsBranch() {
        String[] branches = {
                "main",
                "master",
                "develop",
                "feature/test-" + randomString(6),
                "release/v" + randomNumber(2),
                "hotfix/" + randomString(6)
        };
        return branches[randomInt(0, branches.length - 1)];
    }

    /**
     * Генерирует случайное число в диапазоне
     */
    public int randomInt(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    /**
     * Генерирует случайное булево значение
     */
    public boolean randomBoolean() {
        return Math.random() > 0.5;
    }

    /**
     * Генерирует случайное число заданной длины
     */
    public String randomNumber(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(randomInt(0, 9));
        }
        return sb.toString();
    }

    /**
     * Генерирует уникальное имя с указанным префиксом и суффиксом
     */
    public String generateNameWithPrefixAndSuffix(String prefix, String suffix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + suffix;
    }

    // =========================================================================
    // ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ ТЕСТОВЫХ ДАННЫХ
    // =========================================================================

    public static class TestEnvironmentData {
        private final String projectId;
        private final String projectName;
        private final String buildConfigId;
        private final String buildConfigName;
        private final String branch;
        private final int timeoutSeconds;
        private final String description;

        public TestEnvironmentData(String projectId, String projectName,
                                   String buildConfigId, String buildConfigName,
                                   String branch, int timeoutSeconds, String description) {
            this.projectId = projectId;
            this.projectName = projectName;
            this.buildConfigId = buildConfigId;
            this.buildConfigName = buildConfigName;
            this.branch = branch;
            this.timeoutSeconds = timeoutSeconds;
            this.description = description;
        }

        public String getProjectId() {
            return projectId;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getBuildConfigId() {
            return buildConfigId;
        }

        public String getBuildConfigName() {
            return buildConfigName;
        }

        public String getBranch() {
            return branch;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "TestEnvironmentData{" +
                    "projectId='" + projectId + '\'' +
                    ", projectName='" + projectName + '\'' +
                    ", buildConfigId='" + buildConfigId + '\'' +
                    ", buildConfigName='" + buildConfigName + '\'' +
                    ", branch='" + branch + '\'' +
                    ", timeoutSeconds=" + timeoutSeconds +
                    '}';
        }
    }
}