package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RequestType;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ProjectCreationException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.BuildType;
import com.teamcity.core.models.NewProjectDescription;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.ProjectMoveRequest;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

    // ===== КОНСТАНТЫ =====
    private static final String ROOT_PROJECT_ID = "_Root";
    private static final String PROJECT_ENDPOINT = "/app/rest/projects/%s";

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
    // CREATE (БАЗОВЫЕ МЕТОДЫ)
    // =========================================================================

    /**
     * Создает новый проект в корне.
     *
     * @param project объект проекта для создания
     * @return созданный проект
     */
    @Step("Create project: {project.name}")
    public Project createProject(Project project) {
        log.info("Creating project: {}", project.getName());

        Response response = client.post(Endpoint.PROJECTS.getPath(), project);
        Project created = validator.validate(response, Project.class);

        log.info("Project created: ID={}, Name={}", created.getId(), created.getName());
        return created;
    }

    /**
     * Создает проект с кастомными заголовками.
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
    // CREATE - РАСШИРЕННЫЕ МЕТОДЫ (SENIOR LEVEL)
    // =========================================================================

    /**
     * Создает проект под указанным родителем (одношаговый подход).
     * Использует NewProjectDescription с parentProject объектом согласно Swagger.
     *
     * @param project  проект для создания
     * @param parentId ID родительского проекта
     * @return созданный проект
     * @throws ProjectCreationException если создание не удалось
     */
    @Step("Create project under parent: {project.name} -> {parentId}")
    public Project createProjectUnderParent(Project project, String parentId) {
        log.info("Creating project '{}' under parent: {}", project.getName(), parentId);

        // 1. Валидация
        validateCreateProject(project, parentId);

        // 2. Проверяем существование родителя
        validateParentExists(parentId);

        // 3. Создаем DTO для API согласно Swagger
        NewProjectDescription request = NewProjectDescription.createChild(
                project.getName(),
                parentId
        );
        request.setDescription(project.getDescription());

        // 4. Отправляем запрос
        try {
            Response response = client.post(Endpoint.PROJECTS.getPath(), request);
            Project created = validator.validate(response, Project.class);

            log.info("✅ Project created under parent: ID={}, Name={}, Parent={}",
                    created.getId(), created.getName(), created.getParentProjectId());
            return created;
        } catch (Exception e) {
            throw new ProjectCreationException(
                    String.format("Failed to create project '%s' under parent '%s'",
                            project.getName(), parentId),
                    e
            );
        }
    }

    /**
     * Создает проект под указанным родителем (двухшаговый подход - FALLBACK).
     * Сначала создает в корне, затем перемещает.
     *
     * @param project  проект для создания
     * @param parentId ID родительского проекта
     * @return созданный проект
     * @throws ProjectCreationException если создание не удалось
     */
    @Step("Create project under parent (two-step fallback): {project.name} -> {parentId}")
    public Project createProjectUnderParentTwoStep(Project project, String parentId) {
        log.info("Creating project '{}' under parent (two-step): {}", project.getName(), parentId);

        validateCreateProject(project, parentId);
        validateParentExists(parentId);

        try {
            // 1. Создаем проект в корне
            Project created = createProject(project);
            log.info("Project created in root: {}", created.getId());

            // 2. Перемещаем под родителя
            return moveProject(created.getId(), parentId);
        } catch (Exception e) {
            throw new ProjectCreationException(
                    String.format("Failed to create project '%s' under parent '%s' (two-step)",
                            project.getName(), parentId),
                    e
            );
        }
    }

    /**
     * Умное создание проекта под родителем - автоматически выбирает стратегию.
     * Пытается одношаговый подход, при ошибке переключается на двухшаговый.
     *
     * @param project  проект для создания
     * @param parentId ID родительского проекта
     * @return созданный проект
     */
    @Step("Smart create project under parent: {project.name} -> {parentId}")
    public Project createProjectSmartUnderParent(Project project, String parentId) {
        log.info("Smart creating project '{}' under parent: {}", project.getName(), parentId);

        validateCreateProject(project, parentId);

        try {
            // Пробуем одношаговый подход
            return createProjectUnderParent(project, parentId);
        } catch (Exception e) {
            log.warn("Single-step creation failed: {}, falling back to two-step", e.getMessage());
            // Fallback на двухшаговый
            return createProjectUnderParentTwoStep(project, parentId);
        }
    }

    /**
     * Умное создание проекта - автоматически определяет, где создавать.
     * Если указан parentProjectId и он не Root - создает подпроект.
     * Иначе создает в корне.
     *
     * @param project проект с заполненными данными (включая parentProjectId если нужно)
     * @return созданный проект
     */
    @Step("Smart create project: {project.name}")
    public Project createProjectSmart(Project project) {
        String parentId = project.getParentProjectId();

        // Если указан валидный родитель (не Root и не null)
        if (parentId != null && !parentId.isEmpty() && !ROOT_PROJECT_ID.equals(parentId)) {
            log.debug("Creating project as subproject under: {}", parentId);
            return createProjectSmartUnderParent(project, parentId);
        }

        // Создаем в корне
        log.debug("Creating project in root");
        return createProject(project);
    }

    /**
     * Создает дочерний проект с автоматической генерацией данных.
     * Удобный метод для быстрого создания иерархии.
     *
     * @param parentId    ID родительского проекта
     * @param projectName имя дочернего проекта (если null - генерируется)
     * @return созданный дочерний проект
     */
    @Step("Create child project under: {parentId}")
    public Project createChildProject(String parentId, String projectName) {
        String name = projectName != null ? projectName : generateProjectName("Child");

        Project childProject = Project.builder()
                .name(name)
                .description("Child project under " + parentId)
                .build();

        return createProjectSmartUnderParent(childProject, parentId);
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
    // UPDATE (БАЗОВЫЕ МЕТОДЫ)
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
     * @param projectId      ID проекта
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
    // UPDATE - РАСШИРЕННЫЕ МЕТОДЫ (SENIOR LEVEL)
    // =========================================================================

    /**
     * Перемещает проект под другого родителя.
     * Использует ProjectMoveRequest DTO для PUT запроса.
     *
     * @param projectId    ID перемещаемого проекта
     * @param newParentId  ID нового родительского проекта
     * @return обновленный проект
     * @throws ResourceNotFoundException если проект или новый родитель не найдены
     */
    @Step("Move project: {projectId} -> {newParentId}")
    public Project moveProject(String projectId, String newParentId) {
        log.info("Moving project {} to parent: {}", projectId, newParentId);

        // 1. Проверяем существование
        validateProjectExists(projectId);
        validateParentExists(newParentId);

        // 2. Создаем правильный DTO для перемещения
        ProjectMoveRequest request = ProjectMoveRequest.builder()
                .parentProject(ProjectMoveRequest.ProjectReference.of(newParentId))
                .build();

        // 3. Отправляем запрос
        String endpoint = String.format(PROJECT_ENDPOINT, projectId);
        Response response = client.put(endpoint, request);
        validator.validateStatus(response);

        // 4. Получаем обновленный проект
        Project updated = getProject(projectId);
        log.info("Project moved: ID={}, NewParent={}", updated.getId(), updated.getParentProjectId());
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

    /**
     * Генерирует уникальное имя проекта с префиксом.
     * Использует timestamp + UUID для гарантированной уникальности.
     *
     * @param prefix префикс для имени (если null, используется "Project")
     * @return уникальное имя проекта
     */
    @Step("Generate project name with prefix: {prefix}")
    public String generateProjectName(String prefix) {
        String safePrefix = prefix != null ? prefix : "Project";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s", safePrefix, timestamp, uuid);
    }

    /**
     * Генерирует уникальное имя проекта с префиксом "Test".
     *
     * @return уникальное имя проекта
     */
    public String generateProjectName() {
        return generateProjectName("Test");
    }

    // =========================================================================
    // PRIVATE METHODS - ВАЛИДАЦИЯ (SENIOR LEVEL)
    // =========================================================================

    /**
     * Валидирует входные данные для создания проекта под родителем.
     *
     * @param project  проект для валидации
     * @param parentId ID родительского проекта для валидации
     * @throws IllegalArgumentException если данные невалидны
     */
    private void validateCreateProject(Project project, String parentId) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        if (parentId == null || parentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Parent project ID cannot be null or empty");
        }
    }

    /**
     * Проверяет существование проекта.
     *
     * @param projectId ID проекта
     * @throws ResourceNotFoundException если проект не найден
     */
    private void validateProjectExists(String projectId) {
        if (!projectExists(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
    }

    /**
     * Проверяет существование родительского проекта.
     *
     * @param parentId ID родительского проекта
     * @throws ResourceNotFoundException если родитель не найден
     */
    private void validateParentExists(String parentId) {
        if (!ROOT_PROJECT_ID.equals(parentId) && !projectExists(parentId)) {
            throw new ResourceNotFoundException("Parent project not found: " + parentId);
        }
    }

    // =========================================================================
    // PRIVATE METHODS - UTILITY
    // =========================================================================

    /**
     * Безопасный сон.
     *
     * @param millis время сна в миллисекундах
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