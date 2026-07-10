package com.teamcity.api;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.steps.AdminSteps;
import com.teamcity.core.steps.BuildSteps;
import com.teamcity.core.steps.ProjectSteps;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовый класс для всех API тестов TeamCity.
 * <p>
 * Предоставляет:
 * <ul>
 *   <li>Инициализацию API клиентов (admin и user)</li>
 *   <li>Автоматическую очистку созданных ресурсов</li>
 *   <li>Методы для трекинга ресурсов</li>
 *   <li>Доступ к Step-классам через AdminSteps, ProjectSteps, BuildSteps</li>
 * </ul>
 *
 * <p><b>Пример использования:</b>
 * <pre>
 * public class MyTest extends BaseApiTest {
 *
 *     {@literal @}Test
 *     public void test() {
 *         // Создаем ресурсы
 *         Project project = projectSteps.createProject(...);
 *         trackProject(project.getId());
 *
 *         // Или через AdminSteps
 *         AdminSteps.TestEnvironment env = createTestEnvironment(...);
 *
 *         // Ресурсы будут автоматически очищены в @AfterEach
 *     }
 * }
 * </pre>
 */
@Slf4j
@Tag("api")
@ExtendWith(TestListener.class)
public abstract class BaseApiTest {

    // =========================================================================
    // API КЛИЕНТЫ
    // =========================================================================

    /**
     * Клиент с правами администратора.
     * Используется для создания/удаления ресурсов.
     */
    protected ApiClient adminClient;

    /**
     * Клиент с правами обычного пользователя.
     * Используется для тестирования пользовательских сценариев.
     */
    protected ApiClient userClient;

    /**
     * Фабрика для генерации тестовых данных.
     */
    protected TestDataFactory dataFactory;

    // =========================================================================
    // STEP-КЛАССЫ (доступны для всех тестов)
    // =========================================================================

    /**
     * Шаги для работы с проектами.
     */
    protected ProjectSteps projectSteps;

    /**
     * Шаги для работы с билд-конфигами и билдами.
     */
    protected BuildSteps buildSteps;

    /**
     * Шаги для административных операций.
     * Содержит комплексные методы для создания тестовых окружений.
     */
    protected AdminSteps adminSteps;

    // =========================================================================
    // ТРЕКИНГ РЕСУРСОВ (для автоматической очистки)
    // =========================================================================

    /**
     * Список ID созданных проектов для автоматической очистки.
     */
    private final List<String> createdProjects = new ArrayList<>();

    /**
     * Список имен созданных пользователей для автоматической очистки.
     */
    private final List<String> createdUsers = new ArrayList<>();

    /**
     * Список ID созданных билд-конфигов для автоматической очистки.
     */
    private final List<String> createdBuildConfigs = new ArrayList<>();

    // =========================================================================
    // SETUP / TEARDOWN
    // =========================================================================

    @BeforeEach
    @Step("Initialize API test environment")
    public void setUp() {
        log.info("Setting up API test...");
        System.setProperty("swagger.coverage.results.directory", "target/swagger-coverage");
        // Создаем папку если ее нет
        new File("target/swagger-coverage").mkdirs();

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

        // Инициализация Step-классов
        projectSteps = new ProjectSteps(adminClient);
        buildSteps = new BuildSteps(adminClient);
        adminSteps = new AdminSteps(adminClient);
    }

    @AfterEach
    @Step("Cleanup test resources")
    public void cleanUp() {
        // Очистка ресурсов, затрэканных через старые методы
        cleanupBuildConfigs();
        cleanupProjects();
        cleanupUsers();

        // Очистка ресурсов, затрэканных через AdminSteps
        if (adminSteps != null) {
            adminSteps.cleanupTrackedResources();
        }
    }

    // =========================================================================
    // МЕТОДЫ ОЧИСТКИ (private)
    // =========================================================================

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

    // =========================================================================
    // МЕТОДЫ ТРЕКИНГА (protected)
    // =========================================================================

    /**
     * Добавляет проект в список для автоматической очистки.
     * Вызывайте этот метод после создания проекта в тесте.
     *
     * @param projectId ID созданного проекта
     */
    @Step("Track project for cleanup: {projectId}")
    protected void trackProject(String projectId) {
        if (projectId != null && !projectId.isEmpty()) {
            createdProjects.add(projectId);
        }
    }

    /**
     * Добавляет пользователя в список для автоматической очистки.
     * Вызывайте этот метод после создания пользователя в тесте.
     *
     * @param username Имя созданного пользователя
     */
    @Step("Track user for cleanup: {username}")
    protected void trackUser(String username) {
        if (username != null && !username.isEmpty()) {
            createdUsers.add(username);
        }
    }

    /**
     * Добавляет билд-конфиг в список для автоматической очистки.
     * Вызывайте этот метод после создания билд-конфига в тесте.
     *
     * @param configId ID созданного билд-конфига
     */
    @Step("Track build config for cleanup: {configId}")
    protected void trackBuildConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            createdBuildConfigs.add(configId);
        }
    }

    // =========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ ТЕСТОВЫХ ДАННЫХ
    // =========================================================================

    /**
     * Создает полное тестовое окружение (проект + билд-конфиг).
     * Использует AdminSteps для создания и автоматически трекает ресурсы.
     *
     * <p><b>Пример использования:</b>
     * <pre>
     * AdminSteps.TestEnvironment env = createTestEnvironment(
     *     "TestProject", "Test Project",
     *     "TestConfig", "Test Config",
     *     "Description"
     * );
     * String projectId = env.getProjectId();
     * String configId = env.getBuildConfigId();
     * </pre>
     *
     * @param projectId   ID проекта
     * @param projectName Название проекта
     * @param configId    ID билд-конфига
     * @param configName  Название билд-конфига
     * @param description Описание
     * @return Объект с созданными проектом и билд-конфигом
     */
    @Step("Create test environment")
    protected AdminSteps.TestEnvironment createTestEnvironment(
            String projectId, String projectName,
            String configId, String configName,
            String description) {

        AdminSteps.TestEnvironment env = adminSteps.createTestEnvironment(
                projectId, projectName,
                configId, configName,
                description
        );

        // Трекинг для гарантированной очистки
        trackProject(env.getProjectId());
        trackBuildConfig(env.getBuildConfigId());

        return env;
    }

    /**
     * Создает тестовое окружение с автоматической генерацией ID.
     * Использует AdminSteps для создания и автоматически трекает ресурсы.
     *
     * <p><b>Пример использования:</b>
     * <pre>
     * AdminSteps.TestEnvironment env = createTestEnvironment();
     * String projectId = env.getProjectId();
     * String configId = env.getBuildConfigId();
     * </pre>
     *
     * @return Объект с созданными проектом и билд-конфигом
     */
    @Step("Create test environment with generated IDs")
    protected AdminSteps.TestEnvironment createTestEnvironment() {
        AdminSteps.TestEnvironment env = adminSteps.createTestEnvironment();

        // Трекинг для гарантированной очистки
        trackProject(env.getProjectId());
        trackBuildConfig(env.getBuildConfigId());

        return env;
    }
}