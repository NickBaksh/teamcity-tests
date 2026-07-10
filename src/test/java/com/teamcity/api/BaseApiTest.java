package com.teamcity.api;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ExtendWith(TestListener.class)
public abstract class BaseApiTest {

    protected ApiClient adminClient;
    protected ApiClient userClient;
    protected TestDataFactory dataFactory;

    private final List<String> createdProjects = new ArrayList<>();
    private final List<String> createdUsers = new ArrayList<>();
    private final List<String> createdBuildConfigs = new ArrayList<>();

    @BeforeEach
    @Step("Initialize API test environment")
    public void setUp() {
        log.info("Setting up API test...");

        adminClient = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword())
                .withRetry(ConfigManager.getRetryCount())
                .build();

        userClient = RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getUserLogin(), ConfigManager.getUserPassword())
                .withRetry(ConfigManager.getRetryCount())
                .build();

        dataFactory = new TestDataFactory();
    }

    @AfterEach
    @Step("Cleanup test resources")
    public void cleanUp() {
        cleanupBuildConfigs();
        cleanupProjects();
        cleanupUsers();
    }

    private void cleanupBuildConfigs() {
        if (createdBuildConfigs.isEmpty()) return;

        for (String configId : createdBuildConfigs) {
            try {
                log.info("Cleaning up build config: {}", configId);
                adminClient.delete("/app/rest/buildTypes/{btLocator}", configId);
            } catch (ApiException e) {
                if (e.getStatusCode() != 404) {
                    log.warn("Failed to delete build config: {} - {}", configId, e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Unexpected error cleaning build config: {}", configId, e);
            }
        }
        createdBuildConfigs.clear();
    }

    private void cleanupProjects() {
        if (createdProjects.isEmpty()) return;

        for (String projectId : createdProjects) {
            try {
                log.info("Cleaning up project: {}", projectId);
                adminClient.delete("/app/rest/projects/{projectLocator}", projectId);
            } catch (ApiException e) {
                if (e.getStatusCode() != 404) {
                    log.warn("Failed to delete project: {} - {}", projectId, e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Unexpected error cleaning project: {}", projectId, e);
            }
        }
        createdProjects.clear();
    }

    private void cleanupUsers() {
        if (createdUsers.isEmpty()) return;

        for (String username : createdUsers) {
            try {
                log.info("Cleaning up user: {}", username);
                adminClient.delete("/app/rest/users/{userLocator}", username);
            } catch (ApiException e) {
                if (e.getStatusCode() != 404) {
                    log.warn("Failed to delete user: {} - {}", username, e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Unexpected error cleaning user: {}", username, e);
            }
        }
        createdUsers.clear();
    }

    @Step("Track project for cleanup: {projectId}")
    protected void trackProject(String projectId) {
        if (projectId != null && !projectId.isEmpty()) {
            createdProjects.add(projectId);
        }
    }

    @Step("Track user for cleanup: {username}")
    protected void trackUser(String username) {
        if (username != null && !username.isEmpty()) {
            createdUsers.add(username);
        }
    }

    @Step("Track build config for cleanup: {configId}")
    protected void trackBuildConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            createdBuildConfigs.add(configId);
        }
    }
}