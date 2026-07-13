package com.teamcity.api.requests.steps;

import com.teamcity.api.generators.testdata.DataProviders;
import com.teamcity.api.models.dto_models.builds.*;
import com.teamcity.api.models.dto_models.issue.RelatedIssuesResponse;
import com.teamcity.api.models.dto_models.projects.ProjectRequest;
import com.teamcity.api.requests.skelethon.Endpoint;
import com.teamcity.api.requests.skelethon.requesters.CrudRequester;
import com.teamcity.api.requests.skelethon.requesters.ValidatedCrudRequester;
import com.teamcity.api.specs.RequestSpecs;
import com.teamcity.api.specs.ResponseSpecs;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class BuildSteps {

    // ==================== Хранилища для очистки ====================

    private static final ThreadLocal<List<TestBuildContext>> testBuilds = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<String>> testProjects = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<String>> testBuildTypes = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Boolean> isCleaningUp = ThreadLocal.withInitial(() -> false);


    /**
     * Создать новый проект с уникальным именем
     */
    public static String createProject() {
        ProjectRequest request = DataProviders.generateProjectRequest();

        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<newProjectDescription id=\"" + request.getId() + "\" name=\"" + request.getName() + "\">\n" +
                "  <parentProject locator=\"id:_Root\"/>\n" +
                "</newProjectDescription>";

        given()
                .spec(RequestSpecs.adminSpec())
                .contentType("application/xml")
                .accept("application/xml")
                .body(xmlBody)
                .post("/app/rest/projects")
                .then()
                .statusCode(200);

        testProjects.get().add(request.getId());
        System.out.println("✅ Project created: " + request.getId() + " (name: " + request.getName() + ")");
        return request.getId();
    }

    /**
     * Создать Build Type с уникальными данными
     */
    public static String createBuildType(String projectId) {
        BuildTypeRequest request = DataProviders.generateBuildTypeRequest();
        request.setProjectId(projectId);  // Передаем реальный projectId

        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<buildType id=\"" + request.getId() + "\" name=\"" + request.getName() + "\" projectId=\"" + request.getProjectId() + "\">\n" +
                "  <settings>\n" +
                "    <options>\n" +
                "      <option name=\"buildNumberPattern\" value=\"%build.counter%\"/>\n" +
                "    </options>\n" +
                "  </settings>\n" +
                "</buildType>";

        given()
                .spec(RequestSpecs.adminSpec())
                .contentType("application/xml")
                .accept("application/xml")
                .body(xmlBody)
                .post("/app/rest/buildTypes")
                .then()
                .statusCode(200);

        testBuildTypes.get().add(request.getId());
        System.out.println("✅ Build Type created: " + request.getId() + " in project: " + projectId);
        return request.getId();
    }


    // ==================== Создание проекта ====================

    /**
     * Создать новый проект в TeamCity
     */
    public static String createProject(String projectName) {
        String projectId = "Project_" + UUID.randomUUID().toString().substring(0, 8);

        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<newProjectDescription id=\"" + projectId + "\" name=\"" + projectName + "\">\n" +
                "  <parentProject locator=\"id:_Root\"/>\n" +
                "</newProjectDescription>";

        given()
                .spec(RequestSpecs.adminSpec())
                .contentType("application/xml")
                .accept("application/xml")
                .body(xmlBody)
                .post("/app/rest/projects")
                .then()
                .statusCode(200);

        testProjects.get().add(projectId);

        System.out.println("✅ Project created: " + projectId);
        return projectId;
    }

    /**
     * Удалить проект
     */
    public static void deleteProject(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            return;
        }

        try {
            given()
                    .spec(RequestSpecs.adminSpec())
                    .delete("/app/rest/projects/id:" + projectId)
                    .then()
                    .statusCode(204);
            System.out.println("🗑️ Project deleted: " + projectId);
        } catch (Exception e) {
            System.err.println("Failed to delete project: " + projectId);
        }
    }

    // ==================== Создание VCS Root ====================

    /**
     * Создать VCS Root для проекта (опционально, для полноценных билдов)
     */
    public static String createVcsRoot(String projectId, String vcsName) {
        String vcsRootId = "VcsRoot_" + UUID.randomUUID().toString().substring(0, 8);

        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<vcs-root id=\"" + vcsRootId + "\" name=\"" + vcsName + "\" vcsName=\"jetbrains.git\">\n" +
                "  <project id=\"" + projectId + "\"/>\n" +
                "  <properties>\n" +
                "    <property name=\"url\" value=\"https://github.com/example/repo.git\"/>\n" +
                "    <property name=\"branch\" value=\"refs/heads/main\"/>\n" +
                "  </properties>\n" +
                "</vcs-root>";

        given()
                .spec(RequestSpecs.adminSpec())
                .contentType("application/xml")
                .accept("application/xml")
                .body(xmlBody)
                .post("/app/rest/vcs-roots")
                .then()
                .statusCode(200);

        System.out.println("✅ VCS Root created: " + vcsRootId);
        return vcsRootId;
    }

    // ==================== Создание Build Type ====================

    /**
     * Создать новый Build Type в проекте
     */
    public static String createBuildType(String projectId, String namePrefix) {
        String buildTypeId = namePrefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        String buildTypeName = namePrefix + " Build " + System.currentTimeMillis();

        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<buildType id=\"" + buildTypeId + "\" name=\"" + buildTypeName + "\" projectId=\"" + projectId + "\">\n" +
                "  <settings>\n" +
                "    <options>\n" +
                "      <option name=\"buildNumberPattern\" value=\"%build.counter%\"/>\n" +
                "    </options>\n" +
                "  </settings>\n" +
                "</buildType>";

        given()
                .spec(RequestSpecs.adminSpec())
                .contentType("application/xml")
                .accept("application/xml")
                .body(xmlBody)
                .post("/app/rest/buildTypes")
                .then()
                .statusCode(200);

        testBuildTypes.get().add(buildTypeId);

        System.out.println("✅ Build Type created: " + buildTypeId + " in project: " + projectId);
        return buildTypeId;
    }

    /**
     * Создать Build Type с простым шагом
     */
    public static String createBuildTypeWithStep(String projectId, String namePrefix) {
        String buildTypeId = createBuildType(projectId, namePrefix);
        addSimpleBuildStep(buildTypeId);
        return buildTypeId;
    }

    /**
     * Создать полноценный Build Type с проектом и VCS root
     */
    public static String createFullBuildType(String projectName, String namePrefix) {
        String projectId = createProject(projectName);
        createVcsRoot(projectId, "Test VCS");
        String buildTypeId = createBuildTypeWithStep(projectId, namePrefix);
        return buildTypeId;
    }

    /**
     * Добавить простой build step
     */
    public static void addSimpleBuildStep(String buildTypeId) {
        String stepXml =
                "<step id=\"RUNNER_1\" name=\"Print Hello\" type=\"simpleRunner\">\n" +
                        "  <parameters>\n" +
                        "    <param name=\"script.content\" value=\"echo 'Hello from TeamCity Test'\"/>\n" +
                        "    <param name=\"teamcity.step.mode\" value=\"default\"/>\n" +
                        "  </parameters>\n" +
                        "</step>";

        given()
                .spec(RequestSpecs.adminSpec())
                .contentType("application/xml")
                .accept("application/xml")
                .body(stepXml)
                .post("/app/rest/buildTypes/id:" + buildTypeId + "/steps")
                .then()
                .statusCode(200);
    }

    /**
     * Удалить Build Type
     */
    public static void deleteBuildType(String buildTypeId) {
        if (buildTypeId == null || buildTypeId.isEmpty()) {
            return;
        }

        try {
            given()
                    .spec(RequestSpecs.adminSpec())
                    .delete("/app/rest/buildTypes/id:" + buildTypeId)
                    .then()
                    .statusCode(204);
            System.out.println("🗑️ Build Type deleted: " + buildTypeId);
        } catch (Exception e) {
            System.err.println("Failed to delete Build Type: " + buildTypeId);
        }
    }

    // ==================== Создание билдов ====================

    /**
     * Создать билд с полным набором данных (проект → build type → build)
     */
    public static BuildResponse createBuildWithFullSetup(String projectName, String namePrefix, String branchName) {
        String projectId = createProject(projectName);
        createVcsRoot(projectId, "Test VCS");
        String buildTypeId = createBuildTypeWithStep(projectId, namePrefix);
        return createBuild(buildTypeId, branchName);
    }

    /**
     * Создать билд с минимальным набором данных (проект + build type + build)
     */
    public static BuildResponse createBuildWithMinimalSetup(String projectName, String namePrefix, String branchName) {
        String projectId = createProject(projectName);
        String buildTypeId = createBuildTypeWithStep(projectId, namePrefix);
        return createBuild(buildTypeId, branchName);
    }

    /**
     * Создать билд с минимальным набором данных
     * Все имена генерируются автоматически через модели
     */
    public static BuildResponse createBuildWithMinimalSetup() {
        // Генерируем уникальные данные через модели
        ProjectRequest projectRequest = DataProviders.generateProjectRequest();
        BuildTypeRequest buildTypeRequest = DataProviders.generateBuildTypeRequest();

        String projectId = createProject(projectRequest.getName());
        String buildTypeId = createBuildTypeWithCustomId(projectId, buildTypeRequest.getId(), buildTypeRequest.getName());
        addSimpleBuildStep(buildTypeId);
        return createBuild(buildTypeId, "main");
    }

    /**
     * Создать Build Type с кастомным ID и именем
     */
    public static String createBuildTypeWithCustomId(String projectId, String buildTypeId, String buildTypeName) {
        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<buildType id=\"" + buildTypeId + "\" name=\"" + buildTypeName + "\" projectId=\"" + projectId + "\">\n" +
                "  <settings>\n" +
                "    <options>\n" +
                "      <option name=\"buildNumberPattern\" value=\"%build.counter%\"/>\n" +
                "    </options>\n" +
                "  </settings>\n" +
                "</buildType>";

        given()
                .spec(RequestSpecs.adminSpec())
                .contentType("application/xml")
                .accept("application/xml")
                .body(xmlBody)
                .post("/app/rest/buildTypes")
                .then()
                .statusCode(200);

        testBuildTypes.get().add(buildTypeId);
        System.out.println("✅ Build Type created: " + buildTypeId + " in project: " + projectId);
        return buildTypeId;
    }

    /**
     * Создать билд с существующим Build Type
     */
    public static BuildResponse createBuild(String buildTypeId, String branchName) {
        BuildQueueResponse queueResponse = triggerBuild(buildTypeId, branchName);
        BuildResponse buildResponse = getBuild(queueResponse.getId());

        TestBuildContext context = TestBuildContext.builder()
                .buildId(buildResponse.getId())
                .buildTypeId(buildTypeId)
                .branchName(branchName != null ? branchName : "main")
                .status(buildResponse.getStatus())
                .queuedId(queueResponse.getId())
                .build();

        testBuilds.get().add(context);

        return buildResponse;
    }

    // ==================== Работа с билдами ====================

    /**
     * Запустить билд в очередь
     */
    public static BuildQueueResponse triggerBuild(String buildTypeId, String branchName) {
        BuildQueueRequest request = BuildQueueRequest.builder()
                .buildTypeId(buildTypeId)
                .branchName(branchName != null ? branchName : "main")
                .cleanSources(true)
                .build();

        return new ValidatedCrudRequester<BuildQueueResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.BUILD_QUEUE_ADD
        ).post(request);
    }

    /**
     * Получить билд по ID
     */
    public static BuildResponse getBuild(String buildLocator) {
        return new ValidatedCrudRequester<BuildResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.BUILDS_GET
        ).get(buildLocator);
    }

    /**
     * Получить статистику билда
     */
    public static BuildStatistic getBuildStatistics(String buildLocator) {
        return new ValidatedCrudRequester<BuildStatistic>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.BUILDS_STATISTICS
        ).get(buildLocator);
    }

    /**
     * Удалить билд
     */
    public static DeleteBuildResponse deleteBuild(String buildLocator) {
        return new ValidatedCrudRequester<DeleteBuildResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.BUILDS_DELETE
        ).delete(buildLocator);
    }

    /**
     * Обновить теги билда
     */
    public static BuildTagsResponse updateBuildTags(String buildLocator, BuildTagsRequest request) {
        return new ValidatedCrudRequester<BuildTagsResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.BUILDS_TAGS_UPDATE
        ).put(buildLocator, request);
    }

    /**
     * Получить связанные issues
     */
    public static RelatedIssuesResponse getRelatedIssues(String buildLocator) {
        return new ValidatedCrudRequester<RelatedIssuesResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.BUILDS_RELATED_ISSUES
        ).get(buildLocator);
    }

    /**
     * Генерация уникального buildTypeId
     */
    public static String generateBuildTypeId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ==================== Очистка ====================

    public static List<TestBuildContext> getCurrentTestBuilds() {
        return new ArrayList<>(testBuilds.get());
    }

    public static void cleanupTestBuilds() {
        if (isCleaningUp.get()) {
            return;
        }

        try {
            isCleaningUp.set(true);
            Long threadId = Thread.currentThread().getId();

            CrudRequester deleteRequester = new CrudRequester(
                    RequestSpecs.adminSpec(),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.BUILDS_DELETE
            );

            // 1. Удаляем билды
            for (TestBuildContext context : testBuilds.get()) {
                try {
                    if (context.getBuildId() != null) {
                        deleteRequester.delete(context.getBuildId());
                        System.out.println("🗑️ Build deleted: " + context.getBuildId());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to delete build: " + context.getBuildId());
                }
            }
            testBuilds.get().clear();

            // 2. Удаляем Build Types
            for (String buildTypeId : testBuildTypes.get()) {
                try {
                    deleteBuildType(buildTypeId);
                } catch (Exception e) {
                    System.err.println("Failed to delete Build Type: " + buildTypeId);
                }
            }
            testBuildTypes.get().clear();

            // 3. Удаляем проекты
            for (String projectId : testProjects.get()) {
                try {
                    deleteProject(projectId);
                } catch (Exception e) {
                    System.err.println("Failed to delete project: " + projectId);
                }
            }
            testProjects.get().clear();

            System.out.println("✅ Cleanup completed for thread " + threadId);

        } finally {
            isCleaningUp.remove();
        }
    }

    /**
     * Ожидать, пока билд не завершится или не перейдет в состояние running
     *
     * @param buildLocator ID билда
     * @param timeoutSeconds Максимальное время ожидания в секундах
     * @param pollIntervalSeconds Интервал опроса в секундах
     * @return Final build state
     */
    public static BuildResponse waitForBuildToFinish(String buildLocator, int timeoutSeconds, int pollIntervalSeconds) {
        Instant start = Instant.now();
        Duration timeout = Duration.ofSeconds(timeoutSeconds);

        while (Duration.between(start, Instant.now()).compareTo(timeout) < 0) {
            BuildResponse build = getBuild(buildLocator);

            // Если билд завершился (state == "finished") или запущен (state == "running")
            if ("finished".equalsIgnoreCase(build.getState()) ||
                    "running".equalsIgnoreCase(build.getState())) {
                return build;
            }

            System.out.println("⏳ Build " + buildLocator + " is still " + build.getState() +
                    ", waiting " + pollIntervalSeconds + "s...");

            try {
                Thread.sleep(pollIntervalSeconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Wait interrupted", e);
            }
        }

        throw new RuntimeException("Build " + buildLocator + " did not finish within " + timeoutSeconds + " seconds");
    }

    /**
     * Ожидать, пока билд не завершится (дефолтные параметры: 30 секунд, интервал 2 секунды)
     */
    public static BuildResponse waitForBuildToFinish(String buildLocator) {
        return waitForBuildToFinish(buildLocator, 30, 2);
    }
}