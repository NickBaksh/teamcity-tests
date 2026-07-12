package com.teamcity.api;

import com.teamcity.core.cleanup.CleanupExtension;
import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.*;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;


@Slf4j
@Tag("api")
@ExtendWith({TestListener.class, CleanupExtension.class})
public abstract class BaseApiTest {

    protected TestDataFactory dataFactory;

    @BeforeEach
    @Step("Initialize API test environment")
    public void setUp() {
        log.info("Setting up API test...");
        log.info("Admin login = {}", ConfigManager.getAdminLogin());
        log.info("Admin password = {}", ConfigManager.getAdminPassword());
        dataFactory = new TestDataFactory();
    }

    protected RestClient adminClient() {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(
                        ConfigManager.getAdminLogin(),
                        ConfigManager.getAdminPassword())
                .withRetry(ConfigManager.getRetryCount())
                .build();
    }

    protected RestClient userClient(User user) {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .withRetry(ConfigManager.getRetryCount())
                .build();
    }

    protected RestClient userNegativeClient(User user) {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(user.getUsername(), user.getPassword())
                .forNegativeTest()
                .build();
    }

    protected BuildSteps buildSteps(ApiClient client) {
        return new BuildSteps(client);
    }

    protected BuildConfigSteps buildConfigSteps(ApiClient client) {
        return new BuildConfigSteps(client);
    }

    protected ProjectSteps projectSteps(ApiClient client) {
        return new ProjectSteps(client);
    }

    protected UserSteps userSteps(ApiClient client) {
        return new UserSteps(client);
    }


    protected ArtifactSteps artifactSteps(ApiClient client) {
        return new ArtifactSteps(client);
    }

    protected AgentSteps agentSteps(ApiClient client) {
        return new AgentSteps((RestClient) client);
    }

    protected BuildFeatureSteps buildFeatureSteps(ApiClient client) {
        return new BuildFeatureSteps(client);
    }
}