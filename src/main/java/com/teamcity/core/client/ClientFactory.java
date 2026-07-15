package com.teamcity.core.client;

import com.teamcity.core.config.ConfigManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ClientFactory {

    private ClientFactory() {
    }

    public static ApiClient createAdminClient() {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword())
                .withRetry(ConfigManager.getRetryCount())
                .retryDelay(ConfigManager.getRetryDelay())
                .build();
    }

    public static ApiClient createUserClient() {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getUserLogin(), ConfigManager.getUserPassword())
                .withRetry(ConfigManager.getRetryCount())
                .retryDelay(ConfigManager.getRetryDelay())
                .build();
    }

    public static ApiClient createBasicAuthClient(String username, String password) {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(username, password)
                .withRetry(ConfigManager.getRetryCount())
                .build();
    }

    public static ApiClient createBearerClient(String token) {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .bearerToken(token)
                .withRetry(ConfigManager.getRetryCount())
                .build();
    }

    public static ApiClient createNegativeTestClient() {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword())
                .withRetry(1)
                .build();
    }

    public static ApiClient createNegativeBasicAuthClient(String username, String password) {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(username, password)
                .withRetry(1)
                .build();
    }

    public static ApiClient createInvalidAuthClient() {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth("wrong", "wrong")
                .withRetry(1)
                .build();
    }
}