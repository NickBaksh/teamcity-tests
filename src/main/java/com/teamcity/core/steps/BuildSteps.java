package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RequestType;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.BuildType;
import com.teamcity.core.models.dto.BuildCancelRequest;
import com.teamcity.core.models.dto.RunBuildRequest;
import io.qameta.allure.Step;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Шаги для работы с Build Configurations и Builds в TeamCity API.
 * <p>
 * Предоставляет методы для CRUD операций с билд-конфигами,
 * управления паузой, запуском билдов и их мониторингом.
 * <p>
 * Все методы интегрированы с Allure для отчетности и используют
 * единый подход к логированию и обработке ошибок.
 *
 * @see <a href="https://www.jetbrains.com/help/teamcity/rest-api-build-types.html">TeamCity Build Types REST API</a>
 * @see <a href="https://www.jetbrains.com/help/teamcity/rest-api-builds.html">TeamCity Builds REST API</a>
 */
@Slf4j
public class BuildSteps {

    // ===== КОНСТАНТЫ =====
    private static final int DEFAULT_WAIT_INTERVAL_SECONDS = 2;
    private static final int DEFAULT_MAX_WAIT_SECONDS = 300;
    private static final String DEFAULT_CANCEL_COMMENT = "Canceled by API test";

    // ===== ПОЛЯ =====
    private final ApiClient client;
    private final ResponseValidator validator;
    private final String baseUrl;

    // ===== КОНСТРУКТОРЫ =====

    public BuildSteps(ApiClient client) {
        this.client = client;
        this.validator = new ResponseValidator();
        this.baseUrl = System.getProperty("base.url", "http://localhost:8111");
    }

    public BuildSteps(ApiClient client, ResponseValidator validator) {
        this.client = client;
        this.validator = validator;
        this.baseUrl = System.getProperty("base.url", "http://localhost:8111");
    }

    // =========================================================================
    // 1. CREATE — Создание билд-конфигов
    // =========================================================================

    /**
     * Создает новый билд-конфиг
     *
     * @param config объект билд-конфига для создания
     * @return созданный билд-конфиг
     */
    @Step("Create build config: {config.name}")
    @Severity(SeverityLevel.BLOCKER)
    public BuildConfig createBuildConfig(BuildConfig config) {
        log.info("Creating build config: {}", config.getName());

        Response response = client.post(Endpoint.BUILD_TYPES.getPath(), config);
        BuildConfig created = validator.validate(response, BuildConfig.class);

        log.info("Build config created: ID={}, Name={}", created.getId(), created.getName());
        return created;
    }

    // =========================================================================
    // 2. READ — Получение билд-конфигов
    // =========================================================================

    /**
     * Получает билд-конфиг по ID
     *
     * @param configId ID билд-конфига
     * @return найденный билд-конфиг
     * @throws ResourceNotFoundException если билд-конфиг не найден
     */
    @Step("Get build config by ID: {configId}")
    @Severity(SeverityLevel.BLOCKER)
    public BuildConfig getBuildConfig(String configId) {
        log.debug("Fetching build config: {}", configId);

        // ✅ Используем метод с RequestType.JSON
        Response response = client.get(
                Endpoint.BUILD_TYPE.format(configId),
                RequestType.JSON
        );
        BuildConfig config = validator.validate(response, BuildConfig.class);

        log.debug("Build config fetched: ID={}, Name={}", config.getId(), config.getName());
        return config;
    }

    /**
     * Получает BuildType по ID (расширенная информация)
     */
    @Step("Get build type by ID: {buildTypeId}")
    @Severity(SeverityLevel.NORMAL)
    public BuildType getBuildType(String buildTypeId) {
        log.debug("Fetching build type: {}", buildTypeId);

        // ✅ Используем метод с RequestType.JSON
        Response response = client.get(
                Endpoint.BUILD_TYPE.format(buildTypeId),
                RequestType.JSON
        );
        BuildType buildType = validator.validate(response, BuildType.class);

        log.debug("Build type fetched: ID={}, Name={}, Paused={}",
                buildType.getId(), buildType.getName(), buildType.getPaused());
        return buildType;
    }

    /**
     * Получает все билд-конфиги
     *
     * @return список всех билд-конфигов
     */
    @Step("Get all build configs")
    @Severity(SeverityLevel.NORMAL)
    public List<BuildConfig> getAllBuildConfigs() {
        log.debug("Fetching all build configs");

        Response response = client.get(Endpoint.BUILD_TYPES.getPath());
        List<BuildConfig> configs = validator.validate(
                response,
                res -> res.jsonPath().getList("buildType", BuildConfig.class)
        );

        log.info("Found {} build configs", configs != null ? configs.size() : 0);
        return configs != null ? configs : Collections.emptyList();
    }

    /**
     * Получает билд-конфиги по ID проекта
     *
     * @param projectId ID проекта
     * @return список билд-конфигов проекта
     */
    @Step("Get build configs by project: {projectId}")
    @Severity(SeverityLevel.NORMAL)
    public List<BuildConfig> getBuildConfigsByProject(String projectId) {
        log.debug("Fetching build configs for project: {}", projectId);

        String endpoint = String.format("%s?locator=project:(id:%s)",
                Endpoint.BUILD_TYPES.getPath(), projectId);

        Response response = client.get(endpoint);
        List<BuildConfig> configs = validator.validate(
                response,
                res -> res.jsonPath().getList("buildType", BuildConfig.class)
        );

        log.info("Found {} build configs for project {}",
                configs != null ? configs.size() : 0, projectId);
        return configs != null ? configs : Collections.emptyList();
    }

    // =========================================================================
    // 3. UPDATE — Обновление билд-конфигов
    // =========================================================================

    /**
     * Обновляет имя билд-конфига
     *
     * @param configId ID билд-конфига
     * @param newName  новое имя
     * @return обновленный билд-конфиг
     */
    @Step("Update build config name: {configId} -> {newName}")
    @Severity(SeverityLevel.NORMAL)
    public BuildConfig updateBuildConfig(String configId, String newName) {
        log.info("Updating build config name: {} -> {}", configId, newName);

        Response response = client.putText(Endpoint.BUILD_TYPE_NAME.format(configId), newName);
        validator.validateStatus(response);

        BuildConfig updated = getBuildConfig(configId);
        log.info("Build config name updated: ID={}, NewName={}", updated.getId(), updated.getName());
        return updated;
    }

    /**
     * Обновляет описание билд-конфига
     *
     * @param configId    ID билд-конфига
     * @param description новое описание
     * @return обновленный билд-конфиг
     */
    @Step("Update build config description: {configId} -> {description}")
    @Severity(SeverityLevel.NORMAL)
    public BuildConfig updateBuildConfigDescription(String configId, String description) {
        log.info("Updating build config description: {} -> {}", configId, description);

        String endpoint = Endpoint.BUILD_TYPE.format(configId) + "/description";
        Response response = client.putText(endpoint, description);
        validator.validateStatus(response);

        BuildConfig updated = getBuildConfig(configId);
        log.info("Build config description updated: ID={}", updated.getId());
        return updated;
    }

    // =========================================================================
    // 4. DELETE — Удаление билд-конфигов
    // =========================================================================

    /**
     * Удаляет билд-конфиг
     *
     * @param configId ID билд-конфига
     */
    @Step("Delete build config: {configId}")
    @Severity(SeverityLevel.BLOCKER)
    public void deleteBuildConfig(String configId) {
        log.info("Deleting build config: {}", configId);

        Response response = client.delete(Endpoint.BUILD_TYPE.format(configId));
        validator.validateStatus(response);

        log.info("Build config deleted: ID={}", configId);
    }

    /**
     * Удаляет билд-конфиг, если он существует (idempotent операция)
     *
     * @param configId ID билд-конфига
     * @return true если был удален, false если не существовал
     */
    @Step("Delete build config if exists: {configId}")
    @Severity(SeverityLevel.NORMAL)
    public boolean deleteBuildConfigIfExists(String configId) {
        if (buildConfigExists(configId)) {
            deleteBuildConfig(configId);
            return true;
        }
        log.debug("Build config {} does not exist, skipping deletion", configId);
        return false;
    }

    // =========================================================================
    // 5. PAUSE / RESUME — Управление паузой
    // =========================================================================

    /**
     * Приостанавливает билд-конфиг
     *
     * @param configId ID билд-конфига
     */
    @Step("Pause build config: {configId}")
    @Severity(SeverityLevel.NORMAL)
    public void pauseBuildConfig(String configId) {
        log.info("Pausing build config: {}", configId);

        Response response = client.putBoolean(Endpoint.BUILD_TYPE_PAUSED.format(configId), true);
        validator.validateStatus(response);

        log.info("Build config paused: {}", configId);
    }

    /**
     * Возобновляет билд-конфиг
     *
     * @param configId ID билд-конфига
     */
    @Step("Resume build config: {configId}")
    @Severity(SeverityLevel.NORMAL)
    public void resumeBuildConfig(String configId) {
        log.info("Resuming build config: {}", configId);

        Response response = client.putBoolean(Endpoint.BUILD_TYPE_PAUSED.format(configId), false);
        validator.validateStatus(response);

        log.info("Build config resumed: {}", configId);
    }

    /**
     * Устанавливает статус паузы для билд-конфига
     *
     * @param configId ID билд-конфига
     * @param paused   true - пауза, false - возобновить
     */
    @Step("Set build config pause: {configId} = {paused}")
    @Severity(SeverityLevel.NORMAL)
    public void setBuildConfigPaused(String configId, boolean paused) {
        if (paused) {
            pauseBuildConfig(configId);
        } else {
            resumeBuildConfig(configId);
        }
    }

    /**
     * Проверяет, приостановлен ли билд-конфиг
     *
     * @param configId ID билд-конфига
     * @return true если приостановлен, false если нет
     */
    @Step("Check if build config is paused: {configId}")
    @Severity(SeverityLevel.MINOR)
    public boolean isBuildConfigPaused(String configId) {
        BuildConfig config = getBuildConfig(configId);
        return config.getPaused() != null && config.getPaused();
    }

    /**
     * Переключает статус паузы билд-конфига (toggle)
     *
     * @param configId ID билд-конфига
     */
    @Step("Toggle build config pause: {configId}")
    @Severity(SeverityLevel.NORMAL)
    public void toggleBuildConfigPause(String configId) {
        boolean isPaused = isBuildConfigPaused(configId);
        setBuildConfigPaused(configId, !isPaused);
        log.info("Build config {}: {}", configId, !isPaused ? "paused" : "resumed");
    }

    // =========================================================================
    // 6. BUILDS — Управление билдами
    // =========================================================================

    /**
     * Запускает билд для конфига
     *
     * @param buildTypeId ID билд-конфига
     * @return созданный билд
     */
    @Step("Run build for config: {buildTypeId}")
    @Severity(SeverityLevel.BLOCKER)
    public Build runBuild(String buildTypeId) {
        log.info("Triggering build for config: {}", buildTypeId);

        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(buildTypeId)
                .build();

        Response response = client.post(Endpoint.BUILD_QUEUE.getPath(), request);
        Build created = validator.validate(response, Build.class);

        log.info("Build triggered: ID={}, State={}", created.getId(), created.getState());
        return created;
    }

    /**
     * Запускает билд с параметрами
     *
     * @param buildTypeId ID билд-конфига
     * @param parameters  параметры билда
     * @return созданный билд
     */
    @Step("Run build with parameters: {buildTypeId}")
    @Severity(SeverityLevel.NORMAL)
    public Build runBuild(String buildTypeId, Map<String, String> parameters) {
        log.info("Triggering build with parameters for config: {}", buildTypeId);

        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(buildTypeId)
                .parameters(parameters)
                .build();

        Response response = client.post(Endpoint.BUILD_QUEUE.getPath(), request);
        Build created = validator.validate(response, Build.class);

        log.info("Build triggered: ID={}, State={}, Params count={}",
                created.getId(), created.getState(),
                parameters != null ? parameters.size() : 0);
        log.debug("Build parameters: {}", parameters);
        return created;
    }

    /**
     * Запускает билд на конкретной ветке
     *
     * @param buildTypeId ID билд-конфига
     * @param branch      название ветки
     * @return созданный билд
     */
    @Step("Run build on branch: {buildTypeId} -> {branch}")
    @Severity(SeverityLevel.NORMAL)
    public Build runBuildOnBranch(String buildTypeId, String branch) {
        log.info("Triggering build for config: {} on branch: {}", buildTypeId, branch);

        RunBuildRequest request = RunBuildRequest.builder()
                .buildTypeId(buildTypeId)
                .branchName(branch)
                .build();

        Response response = client.post(Endpoint.BUILD_QUEUE.getPath(), request);
        Build created = validator.validate(response, Build.class);

        log.info("Build triggered: ID={}, State={}, Branch={}",
                created.getId(), created.getState(), branch);
        return created;
    }

    /**
     * Получает билд по ID
     *
     * @param buildId ID билда
     * @return найденный билд
     */
    @Step("Get build by ID: {buildId}")
    @Severity(SeverityLevel.BLOCKER)
    public Build getBuild(String buildId) {
        log.debug("Fetching build: {}", buildId);

        Response response = client.get(Endpoint.BUILD.format(buildId));
        Build build = validator.validate(response, Build.class);

        log.debug("Build fetched: ID={}, State={}, Status={}",
                build.getId(), build.getState(), build.getStatus());
        return build;
    }

    /**
     * Получает все билды для конфига
     *
     * @param buildTypeId ID билд-конфига
     * @return список билдов
     */
    @Step("Get builds for config: {buildTypeId}")
    @Severity(SeverityLevel.NORMAL)
    public List<Build> getBuildsForConfig(String buildTypeId) {
        log.debug("Fetching builds for config: {}", buildTypeId);

        String endpoint = String.format("%s?locator=buildType:%s",
                Endpoint.BUILDS.getPath(), buildTypeId);

        Response response = client.get(endpoint);
        List<Build> builds = validator.validate(
                response,
                res -> res.jsonPath().getList("build", Build.class)
        );

        log.info("Found {} builds for config {}",
                builds != null ? builds.size() : 0, buildTypeId);
        return builds != null ? builds : Collections.emptyList();
    }

    /**
     * Получает последний билд для конфига
     *
     * @param buildTypeId ID билд-конфига
     * @return последний билд или null
     */
    @Step("Get last build for config: {buildTypeId}")
    @Severity(SeverityLevel.NORMAL)
    public Build getLastBuildForConfig(String buildTypeId) {
        List<Build> builds = getBuildsForConfig(buildTypeId);
        if (builds.isEmpty()) {
            log.debug("No builds found for config: {}", buildTypeId);
            return null;
        }
        return builds.get(0);
    }

    /**
     * Отменяет билд
     *
     * @param buildId ID билда
     * @param comment комментарий к отмене
     */
    @Step("Cancel build: {buildId} with comment: {comment}")
    @Severity(SeverityLevel.NORMAL)
    public void cancelBuild(String buildId, String comment) {
        log.info("Canceling build: {} with comment: {}", buildId, comment);

        BuildCancelRequest request = new BuildCancelRequest();
        request.setComment(comment);

        Response response = client.post(Endpoint.BUILD.format(buildId), request);
        validator.validateStatus(response);

        log.info("Build canceled: {}", buildId);
    }

    /**
     * Отменяет билд с комментарием по умолчанию
     *
     * @param buildId ID билда
     */
    @Step("Cancel build: {buildId}")
    @Severity(SeverityLevel.NORMAL)
    public void cancelBuild(String buildId) {
        cancelBuild(buildId, DEFAULT_CANCEL_COMMENT);
    }

    // =========================================================================
    // 7. WAIT / MONITOR — Ожидание и мониторинг
    // =========================================================================

    /**
     * Ожидает завершения билда
     *
     * @param buildId      ID билда
     * @param maxWaitSeconds максимальное время ожидания в секундах
     * @return завершенный билд
     */
    @Step("Wait for build to finish: {buildId}")
    @Severity(SeverityLevel.CRITICAL)
    public Build waitForBuildFinish(String buildId, int maxWaitSeconds) {
        log.info("Waiting for build {} to finish (max {}s)", buildId, maxWaitSeconds);

        int attempts = maxWaitSeconds / DEFAULT_WAIT_INTERVAL_SECONDS;
        for (int i = 0; i < attempts; i++) {
            Build build = getBuild(buildId);

            if (BuildStatus.isFinished(build.getState())) {
                log.info("Build {} finished in {} seconds", buildId, i * DEFAULT_WAIT_INTERVAL_SECONDS);
                return build;
            }

            if (BuildStatus.isFailed(build.getState())) {
                log.warn("Build {} failed: {}", buildId, build.getStatusText());
                return build;
            }

            sleep(DEFAULT_WAIT_INTERVAL_SECONDS);
        }

        log.warn("Build {} did not finish within {} seconds", buildId, maxWaitSeconds);
        return getBuild(buildId);
    }

    /**
     * Ожидает завершения билда с таймаутом по умолчанию
     *
     * @param buildId ID билда
     * @return завершенный билд
     */
    @Step("Wait for build to finish: {buildId}")
    @Severity(SeverityLevel.CRITICAL)
    public Build waitForBuildFinish(String buildId) {
        return waitForBuildFinish(buildId, DEFAULT_MAX_WAIT_SECONDS);
    }

    /**
     * Ожидает, что билд будет в определенном состоянии
     *
     * @param buildId     ID билда
     * @param expectedState ожидаемое состояние
     * @param maxWaitSeconds максимальное время ожидания
     * @return билд в ожидаемом состоянии
     */
    @Step("Wait for build state: {buildId} -> {expectedState}")
    @Severity(SeverityLevel.NORMAL)
    public Build waitForBuildState(String buildId, String expectedState, int maxWaitSeconds) {
        log.info("Waiting for build {} to reach state {} (max {}s)",
                buildId, expectedState, maxWaitSeconds);

        int attempts = maxWaitSeconds / DEFAULT_WAIT_INTERVAL_SECONDS;
        for (int i = 0; i < attempts; i++) {
            Build build = getBuild(buildId);
            if (expectedState.equalsIgnoreCase(build.getState())) {
                log.info("Build {} reached state {} in {} seconds",
                        buildId, expectedState, i * DEFAULT_WAIT_INTERVAL_SECONDS);
                return build;
            }
            sleep(DEFAULT_WAIT_INTERVAL_SECONDS);
        }

        throw new RuntimeException(String.format(
                "Build %s did not reach state %s within %d seconds",
                buildId, expectedState, maxWaitSeconds));
    }

    /**
     * Ожидает, пока билд-конфиг станет доступен
     *
     * @param configId       ID билд-конфига
     * @param maxWaitSeconds максимальное время ожидания
     * @return готовый билд-конфиг
     */
    @Step("Wait for build config to be ready: {configId}")
    @Severity(SeverityLevel.NORMAL)
    public BuildConfig waitForBuildConfigReady(String configId, int maxWaitSeconds) {
        log.info("Waiting for build config {} to be ready", configId);

        int attempts = maxWaitSeconds / DEFAULT_WAIT_INTERVAL_SECONDS;
        for (int i = 0; i < attempts; i++) {
            try {
                BuildConfig config = getBuildConfig(configId);
                log.debug("Build config {} is ready", configId);
                return config;
            } catch (Exception e) {
                log.debug("Build config {} not ready yet (attempt {}/{})",
                        configId, i + 1, attempts);
                sleep(DEFAULT_WAIT_INTERVAL_SECONDS);
            }
        }

        throw new RuntimeException(String.format(
                "Build config %s not ready after %d seconds", configId, maxWaitSeconds));
    }

    // =========================================================================
    // 8. SEARCH / FILTER — Поиск и фильтрация
    // =========================================================================

    /**
     * Проверяет существование билд-конфига
     *
     * @param configId ID билд-конфига
     * @return true если существует
     */
    @Step("Check if build config exists: {configId}")
    @Severity(SeverityLevel.NORMAL)
    public boolean buildConfigExists(String configId) {
        try {
            getBuildConfig(configId);
            return true;
        } catch (Exception e) {
            log.debug("Build config {} does not exist: {}", configId, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет существование билда
     *
     * @param buildId ID билда
     * @return true если существует
     */
    @Step("Check if build exists: {buildId}")
    @Severity(SeverityLevel.NORMAL)
    public boolean buildExists(String buildId) {
        try {
            getBuild(buildId);
            return true;
        } catch (Exception e) {
            log.debug("Build {} does not exist: {}", buildId, e.getMessage());
            return false;
        }
    }

    /**
     * Находит билд-конфиг по имени
     *
     * @param name имя билд-конфига
     * @return Optional с найденным конфигом
     */
    @Step("Find build config by name: {name}")
    @Severity(SeverityLevel.NORMAL)
    public Optional<BuildConfig> findBuildConfigByName(String name) {
        log.debug("Searching for build config by name: {}", name);

        List<BuildConfig> configs = getAllBuildConfigs();
        return configs.stream()
                .filter(config -> name.equals(config.getName()))
                .findFirst();
    }

    /**
     * Находит билд-конфиги по префиксу имени
     *
     * @param prefix префикс имени
     * @return список билд-конфигов с указанным префиксом
     */
    @Step("Find build configs by name prefix: {prefix}")
    @Severity(SeverityLevel.NORMAL)
    public List<BuildConfig> findBuildConfigsByNamePrefix(String prefix) {
        log.debug("Searching for build configs by name prefix: {}", prefix);

        List<BuildConfig> configs = getAllBuildConfigs();
        return configs.stream()
                .filter(config -> config.getName() != null && config.getName().startsWith(prefix))
                .collect(Collectors.toList());
    }

    /**
     * Находит билды по статусу
     *
     * @param buildTypeId ID билд-конфига
     * @param status      статус (SUCCESS, FAILURE, etc.)
     * @return список билдов с указанным статусом
     */
    @Step("Find builds by status: {buildTypeId} -> {status}")
    @Severity(SeverityLevel.NORMAL)
    public List<Build> findBuildsByStatus(String buildTypeId, String status) {
        log.debug("Searching builds with status: {} for config: {}", status, buildTypeId);

        List<Build> builds = getBuildsForConfig(buildTypeId);
        return builds.stream()
                .filter(build -> status.equalsIgnoreCase(build.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Находит неудачные билды
     *
     * @param buildTypeId ID билд-конфига
     * @return список неудачных билдов
     */
    @Step("Find failed builds for config: {buildTypeId}")
    @Severity(SeverityLevel.NORMAL)
    public List<Build> findFailedBuilds(String buildTypeId) {
        return findBuildsByStatus(buildTypeId, "FAILURE");
    }

    /**
     * Находит успешные билды
     *
     * @param buildTypeId ID билд-конфига
     * @return список успешных билдов
     */
    @Step("Find successful builds for config: {buildTypeId}")
    @Severity(SeverityLevel.NORMAL)
    public List<Build> findSuccessfulBuilds(String buildTypeId) {
        return findBuildsByStatus(buildTypeId, "SUCCESS");
    }

    // =========================================================================
    // 9. UTILITY — Утилитные методы
    // =========================================================================

    /**
     * Получает URL билд-конфига в веб-интерфейсе
     *
     * @param configId ID билд-конфига
     * @return URL страницы конфига
     */
    @Step("Get build config web URL: {configId}")
    public String getBuildConfigWebUrl(String configId) {
        return String.format("%s/buildConfiguration/%s", baseUrl, configId);
    }

    /**
     * Получает URL билда в веб-интерфейсе
     *
     * @param buildId ID билда
     * @return URL страницы билда
     */
    @Step("Get build web URL: {buildId}")
    public String getBuildWebUrl(String buildId) {
        return String.format("%s/build/%s", baseUrl, buildId);
    }

    /**
     * Получает href билд-конфига
     *
     * @param configId ID билд-конфига
     * @return href билд-конфига
     */
    public String getBuildConfigHref(String configId) {
        return String.format("/app/rest/buildTypes/id:%s", configId);
    }

    // =========================================================================
    // 10. CLEANUP — Очистка
    // =========================================================================

    /**
     * Очищает тестовые билд-конфиги по префиксу
     *
     * @param prefix префикс имени
     * @return количество удаленных конфигов
     */
    @Step("Cleanup test build configs with prefix: {prefix}")
    @Severity(SeverityLevel.NORMAL)
    public int cleanupTestBuildConfigs(String prefix) {
        log.info("Cleaning up test build configs with prefix: {}", prefix);

        List<BuildConfig> configs = findBuildConfigsByNamePrefix(prefix);
        int deleted = 0;

        for (BuildConfig config : configs) {
            try {
                if (deleteBuildConfigIfExists(config.getId())) {
                    deleted++;
                }
            } catch (Exception e) {
                log.warn("Failed to delete build config {}: {}", config.getId(), e.getMessage());
            }
        }

        log.info("Cleaned up {} build configs", deleted);
        return deleted;
    }

    // =========================================================================
    // 11. PRIVATE METHODS
    // =========================================================================

    /**
     * Безопасный сон
     *
     * @param seconds количество секунд для сна
     */
    private void sleep(int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
    }

    // =========================================================================
    // 12. ENUMS — Вспомогательные перечисления
    // =========================================================================

    /**
     * Статусы билда
     */
    public enum BuildStatus {
        QUEUED("queued"),
        RUNNING("running"),
        FINISHED("finished"),
        FAILED("failed"),
        CANCELLED("cancelled"),
        UNKNOWN("unknown");

        private final String value;

        BuildStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static boolean isFinished(String state) {
            return FINISHED.value.equalsIgnoreCase(state) ||
                    FAILED.value.equalsIgnoreCase(state) ||
                    CANCELLED.value.equalsIgnoreCase(state);
        }

        public static boolean isFailed(String state) {
            return FAILED.value.equalsIgnoreCase(state) ||
                    CANCELLED.value.equalsIgnoreCase(state);
        }

        public static boolean isInProgress(String state) {
            return QUEUED.value.equalsIgnoreCase(state) ||
                    RUNNING.value.equalsIgnoreCase(state);
        }
    }
}