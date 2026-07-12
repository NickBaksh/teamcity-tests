package com.teamcity.core.steps;

import com.teamcity.core.cleanup.CleanupRegistry;
import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RequestType;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.BuildType;
import com.teamcity.core.models.Project;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Шаги для работы с проектами в TeamCity API.
 * <p>
 * Предоставляет методы для CRUD операций, валидации и поиска проектов.
 * Все методы логируют действия и интегрированы с Allure для отчетности.
 *
 * @see <a href="https://www.jetbrains.com/help/teamcity/rest-api-projects.html">TeamCity Projects REST API</a>
 */
@Slf4j
public class ProjectSteps {

    private final ApiClient client;
    private final ResponseValidator validator;
    private final String baseUrl;
    private final TestDataFactory dataFactory = new TestDataFactory();

    // ===== КОНСТРУКТОРЫ =====

    public ProjectSteps(ApiClient client) {
        this.client = client;
        this.validator = new ResponseValidator();
        this.baseUrl = System.getProperty("base.url", "http://localhost:8111");
    }

    public ProjectSteps(ApiClient client, ResponseValidator validator) {
        this.client = client;
        this.validator = validator;
        this.baseUrl = System.getProperty("base.url", "http://localhost:8111");
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    /**
     * Создает новый проект
     *
     * @param project объект проекта для создания
     * @return созданный проект
     */
    @Step("Create project: {project.name}")
    public Project createProject(Project project) {
        log.info("Creating project: {}", project.getName());

        Response response = client.post(Endpoint.PROJECTS.getPath(), project);
        Project created = validator.validate(response, Project.class);
        CleanupRegistry.get().register(() -> {

            try {

                deleteProject(created.getId());

            } catch (Exception ignored) {

            }

        });

        log.info("Project created: ID={}, Name={}", created.getId(), created.getName());
        return created;
    }

    @Step("Create random project")
    public Project createRandomProject() {
        return createProject(
                dataFactory.createRandomProject()
        );
    }

    /**
     * Создает проект с кастомными заголовками
     *
     * @param project     объект проекта для создания
     * @param requestType тип запроса (JSON, TEXT и т.д.)
     * @return созданный проект
     */
    @Step("Create project with custom headers: {project.name}")
    public Project createProject(Project project, RequestType requestType) {
        log.info("Creating project with {}: {}", requestType, project.getName());

        Response response = client.post(Endpoint.PROJECTS.getPath(), project, requestType);
        Project created = validator.validate(response, Project.class);

        log.info("Project created: ID={}, Name={}", created.getId(), created.getName());
        return created;
    }

    // =========================================================================
    // READ
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

        // ✅ Явно указываем Accept: application/json
        Response response = client.get(Endpoint.BUILD_TYPE.format(configId), RequestType.JSON);
        BuildConfig config = validator.validate(response, BuildConfig.class);

        log.debug("Build config fetched: ID={}, Name={}", config.getId(), config.getName());
        return config;
    }

    /**
     * Получает проект по ID
     *
     * @param projectId ID проекта
     * @return найденный проект
     * @throws ResourceNotFoundException если проект не найден
     */
    @Step("Get project by ID: {projectId}")
    @Severity(SeverityLevel.BLOCKER)
    public Project getProject(String projectId) {
        log.debug("Fetching project: {}", projectId);

        // ✅ Явно указываем Accept: application/json
        Response response = client.get(Endpoint.PROJECT.format(projectId), RequestType.JSON);
        Project project = validator.validate(response, Project.class);

        log.debug("Project fetched: ID={}, Name={}", project.getId(), project.getName());
        return project;
    }

    /**
     * Получает BuildType по ID (расширенная информация)
     *
     * @param buildTypeId ID билд-конфига
     * @return BuildType с полной информацией
     */
    @Step("Get build type by ID: {buildTypeId}")
    @Severity(SeverityLevel.NORMAL)
    public BuildType getBuildType(String buildTypeId) {
        log.debug("Fetching build type: {}", buildTypeId);

        // ✅ Явно указываем Accept: application/json
        Response response = client.get(Endpoint.BUILD_TYPE.format(buildTypeId), RequestType.JSON);
        BuildType buildType = validator.validate(response, BuildType.class);

        log.debug("Build type fetched: ID={}, Name={}, Paused={}",
                buildType.getId(), buildType.getName(), buildType.getPaused());
        return buildType;
    }

    /**
     * Получает все проекты
     *
     * @return список всех проектов
     */
    @Step("Get all projects")
    public List<Project> getAllProjects() {
        log.debug("Fetching all projects");

        Response response = client.get(Endpoint.PROJECTS.getPath());
        List<Project> projects = validator.validate(
                response,
                res -> res.jsonPath().getList("project", Project.class)
        );

        log.info("Found {} projects", projects != null ? projects.size() : 0);
        return projects != null ? projects : Collections.emptyList();
    }

    /**
     * Получает дочерние проекты для указанного родителя
     *
     * @param parentProjectId ID родительского проекта
     * @return список дочерних проектов
     */
    @Step("Get child projects for: {parentProjectId}")
    public List<Project> getChildProjects(String parentProjectId) {
        log.debug("Fetching child projects for: {}", parentProjectId);

        String endpoint = String.format("%s?locator=parentProject:(id:%s)",
                Endpoint.PROJECTS.getPath(), parentProjectId);

        Response response = client.get(endpoint);
        List<Project> projects = validator.validate(
                response,
                res -> res.jsonPath().getList("project", Project.class)
        );

        log.info("Found {} child projects for {}",
                projects != null ? projects.size() : 0, parentProjectId);
        return projects != null ? projects : Collections.emptyList();
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    /**
     * Обновляет имя проекта
     *
     * @param projectId ID проекта
     * @param newName   новое имя
     * @return обновленный проект
     */
    @Step("Update project name: {projectId} -> {newName}")
    public Project updateProject(String projectId, String newName) {
        log.info("Updating project name: {} -> {}", projectId, newName);

        Response response = client.putText(Endpoint.PROJECT_NAME.format(projectId), newName);
        validator.validateStatus(response);

        Project updated = getProject(projectId);
        log.info("Project name updated: ID={}, NewName={}", updated.getId(), updated.getName());
        return updated;
    }

    /**
     * Обновляет описание проекта
     *
     * @param projectId     ID проекта
     * @param newDescription новое описание
     * @return обновленный проект
     */
    @Step("Update project description: {projectId}")
    public Project updateProjectDescription(String projectId, String newDescription) {
        log.info("Updating project description: {}", projectId);

        Response response = client.putText(Endpoint.PROJECT_DESCRIPTION.format(projectId), newDescription);
        validator.validateStatus(response);

        Project updated = getProject(projectId);
        log.info("Project description updated: ID={}", updated.getId());
        return updated;
    }

    /**
     * Обновляет проект с кастомными заголовками
     *
     * @param projectId   ID проекта
     * @param newName     новое имя
     * @param requestType тип запроса
     * @return обновленный проект
     */
    @Step("Update project with custom headers: {projectId}")
    public Project updateProject(String projectId, String newName, RequestType requestType) {
        log.info("Updating project with {}: {} -> {}", requestType, projectId, newName);

        Response response = client.put(Endpoint.PROJECT_NAME.format(projectId), newName, requestType);
        validator.validateStatus(response);

        Project updated = getProject(projectId);
        log.info("Project updated: ID={}, NewName={}", updated.getId(), updated.getName());
        return updated;
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * Удаляет проект
     *
     * @param projectId ID проекта
     */
    @Step("Delete project: {projectId}")
    public void deleteProject(String projectId) {
        log.info("Deleting project: {}", projectId);

        Response response = client.delete(Endpoint.PROJECT.format(projectId));
        validator.validateStatus(response);

        log.info("Project deleted: ID={}", projectId);
    }

    /**
     * Удаляет проект, если он существует (idempotent операция)
     *
     * @param projectId ID проекта
     * @return true если проект был удален, false если не существовал
     */
    @Step("Delete project if exists: {projectId}")
    public boolean deleteProjectIfExists(String projectId) {
        if (projectExists(projectId)) {
            deleteProject(projectId);
            log.info("Project deleted: {}", projectId);
            return true;
        }
        log.debug("Project {} does not exist, skipping deletion", projectId);
        return false;
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    /**
     * Проверяет существование проекта
     *
     * @param projectId ID проекта
     * @return true если проект существует, false если нет
     */
    @Step("Check if project exists: {projectId}")
    public boolean projectExists(String projectId) {
        try {
            getProject(projectId);
            return true;
        } catch (ResourceNotFoundException e) {
            log.debug("Project {} does not exist: {}", projectId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Error checking project existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Находит проект по имени
     *
     * @param name имя проекта
     * @return Optional с найденным проектом или пустой Optional
     */
    @Step("Find project by name: {name}")
    public Optional<Project> findProjectByName(String name) {
        log.debug("Searching for project by name: {}", name);

        List<Project> projects = getAllProjects();
        return projects.stream()
                .filter(project -> name.equals(project.getName()))
                .findFirst();
    }

    /**
     * Находит проекты по префиксу имени (для cleanup)
     *
     * @param prefix префикс имени
     * @return список проектов с указанным префиксом
     */
    @Step("Find projects by name prefix: {prefix}")
    public List<Project> findProjectsByNamePrefix(String prefix) {
        log.debug("Searching for projects by name prefix: {}", prefix);

        List<Project> projects = getAllProjects();
        return projects.stream()
                .filter(project -> project.getName() != null && project.getName().startsWith(prefix))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // UTILITY
    // =========================================================================

    /**
     * Получает URL проекта в веб-интерфейсе
     *
     * @param projectId ID проекта
     * @return URL для просмотра в браузере
     */
    @Step("Get project web URL: {projectId}")
    public String getProjectWebUrl(String projectId) {
        return String.format("%s/project.html?projectId=%s", baseUrl, projectId);
    }

    /**
     * Получает полный href проекта
     *
     * @param projectId ID проекта
     * @return href проекта
     */
    @Step("Get project href: {projectId}")
    public String getProjectHref(String projectId) {
        return String.format("/app/rest/projects/id:%s", projectId);
    }

    /**
     * Ждет, пока проект станет доступен
     *
     * @param projectId      ID проекта
     * @param maxWaitSeconds максимальное время ожидания в секундах
     * @return найденный проект
     * @throws RuntimeException если проект не стал доступен за указанное время
     */
    @Step("Wait for project to be ready: {projectId}")
    public Project waitForProjectReady(String projectId, int maxWaitSeconds) {
        log.info("Waiting for project {} to be ready (max {}s)", projectId, maxWaitSeconds);

        int attempts = maxWaitSeconds / 2;
        for (int i = 0; i < attempts; i++) {
            try {
                Project project = getProject(projectId);
                log.debug("Project {} is ready", projectId);
                return project;
            } catch (Exception e) {
                log.debug("Project {} not ready yet (attempt {}/{})",
                        projectId, i + 1, attempts);
                sleep(2000);
            }
        }

        throw new RuntimeException(String.format(
                "Project %s not ready after %d seconds", projectId, maxWaitSeconds));
    }

    /**
     * Создает несколько проектов для тестов
     *
     * @param count количество проектов для создания
     * @return список созданных проектов
     */
    @Step("Create multiple projects: {count}")
    public List<Project> createMultipleProjects(int count) {
        log.info("Creating {} projects", count);

        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    Project project = Project.builder()
                            .name("Project_" + System.currentTimeMillis() + "_" + i)
                            .description("Auto-generated project for testing")
                            .parentProjectId("_Root")
                            .build();
                    return createProject(project);
                })
                .collect(Collectors.toList());
    }

    /**
     * Очищает тестовые проекты по префиксу
     *
     * @param prefix префикс имени для очистки
     * @return количество удаленных проектов
     */
    @Step("Cleanup test projects with prefix: {prefix}")
    public int cleanupTestProjects(String prefix) {
        log.info("Cleaning up test projects with prefix: {}", prefix);

        List<Project> projects = findProjectsByNamePrefix(prefix);
        int deleted = 0;

        for (Project project : projects) {
            try {
                if (deleteProjectIfExists(project.getId())) {
                    deleted++;
                }
            } catch (Exception e) {
                log.warn("Failed to delete project {}: {}", project.getId(), e.getMessage());
            }
        }

        log.info("Cleaned up {} projects", deleted);
        return deleted;
    }

    // =========================================================================
    // PRIVATE METHODS
    // =========================================================================

    /**
     * Безопасный сон
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
    }
}