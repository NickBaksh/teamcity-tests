package com.teamcity.core.client;

import com.teamcity.core.config.ConfigManager;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientFactory {

    // Для позитивных тестов — с ретраями
    public static ApiClient createAdminClient() {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword())
                .withRetry(ConfigManager.getRetryCount())
                .build();
    }

    // Для негативных тестов — БЕЗ ретраев
    public static ApiClient createNegativeTestClient() {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword())
                .withRetry(1)  // ← ТОЛЬКО 1 ПОПЫТКА
                .build();
    }

    // Для неверной аутентификации — БЕЗ ретраев
    public static ApiClient createInvalidAuthClient() {
        return RestClient.builder()
                .baseUrl(ConfigManager.getApiBaseUrl())
                .basicAuth("wrong", "wrong")
                .withRetry(1)  // ← НЕ РЕТРАИМ!
                .build();
    }
}