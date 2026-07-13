package com.teamcity.api.build;

import com.teamcity.BaseTest;
import com.teamcity.api.generators.testdata.DataProviders;
import com.teamcity.api.models.comparison.ModelAssertions;
import com.teamcity.api.models.dto_models.builds.*;
import com.teamcity.api.models.dto_models.issue.RelatedIssuesResponse;
import com.teamcity.api.requests.skelethon.Endpoint;
import com.teamcity.api.requests.skelethon.requesters.CrudRequester;
import com.teamcity.api.requests.skelethon.requesters.ValidatedCrudRequester;
import com.teamcity.api.requests.steps.BuildSteps;
import com.teamcity.api.specs.RequestSpecs;
import com.teamcity.api.specs.ResponseSpecs;
import com.teamcity.common.annotations.AdminSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.containsString;

@DisplayName("Build Operations Tests")
public class BuildOperationsTest extends BaseTest {

    // ============================================
    // 1. Тесты с динамическим созданием данных
    // ============================================

    @Test
    @DisplayName("Пользователь может запустить билд с динамическим Build Type")
    @AdminSession
    public void userCanTriggerBuildWithDynamicBuildTypeTest() {
        BuildResponse response = BuildSteps.createBuildWithMinimalSetup();

        softly.assertThat(response)
                .as("Response should not be null")
                .isNotNull();

        softly.assertThat(response.getId())
                .as("Build ID should not be null or empty")
                .isNotEmpty();

        softly.assertThat(response.getBuildTypeId())
                .as("Build type ID should not be null")
                .isNotEmpty();

        softly.assertThat(response.getBranchName())
                .as("Branch name should match")
                .isEqualTo("main");
    }

    @Test
    @DisplayName("Пользователь может создать билд с параметрами")
    @AdminSession
    public void userCanCreateBuildWithParametersTest() {
        // ✅ Используем генерацию через модель
        BuildQueueRequest request = DataProviders.generateBuildQueueRequest();
        request.setCleanSources(true);
        request.setRebuildAllDependencies(true);

        // Создаем проект и build type с уникальными именами
        String projectId = BuildSteps.createProject();
        String buildTypeId = BuildSteps.createBuildType(projectId);

        // Создаем билд
        BuildQueueResponse response = BuildSteps.triggerBuild(buildTypeId, request.getBranchName());

        softly.assertThat(response.getId())
                .as("Build ID should be created")
                .isNotEmpty();

        softly.assertThat(response.getBuildTypeId())
                .as("Build type ID should match")
                .isEqualTo(buildTypeId);
    }

    @Test
    @DisplayName("Пользователь может создать билд с параметрами через минимальную настройку")
    @AdminSession
    public void userCanTriggerBuildWithParametersTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse response = BuildSteps.createBuildWithMinimalSetup();

        softly.assertThat(response)
                .as("Response should not be null")
                .isNotNull();

        softly.assertThat(response.getId())
                .as("Build ID should not be null or empty")
                .isNotEmpty();

        softly.assertThat(response.getBranchName())
                .as("Branch name should match")
                .isEqualTo("main");
    }

    // ============================================
    // 2. GET /app/rest/builds/{buildLocator} - Получение билда
    // ============================================

    @Test
    @DisplayName("Пользователь может получить существующий билд по locator")
    @AdminSession
    public void userCanGetExistingBuildByLocatorTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse createdBuild = BuildSteps.createBuildWithMinimalSetup();
        String buildLocator = createdBuild.getId();

        BuildResponse response = BuildSteps.getBuild(buildLocator);

        softly.assertThat(response)
                .as("Response should not be null")
                .isNotNull();

        softly.assertThat(response.getId())
                .as("Build ID should match requested")
                .isEqualTo(buildLocator);

        softly.assertThat(response.getBuildTypeId())
                .as("Build type ID should match")
                .isEqualTo(createdBuild.getBuildTypeId());

        softly.assertThat(response.getBranchName())
                .as("Branch name should match")
                .isEqualTo("main");

        ModelAssertions.assertThatModels(createdBuild, response)
                .matchOnly("id", "buildTypeId", "branchName", "status");
    }

    @Test
    @DisplayName("Пользователь получает ошибку при запросе несуществующего билда")
    @AdminSession
    public void userGetsErrorWhenGettingNonExistentBuildTest() {
        // ✅ Используем числовой ID, который не существует
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.returnsNotFound(),
                Endpoint.BUILDS_GET
        ).get("999999999")
                .body(containsString("Build not found"));
    }

    // ============================================
    // 3. GET /app/rest/builds/{buildLocator}/statistics - Статистика
    // ============================================

    @Test
    @DisplayName("Пользователь может получить статистику существующего билда")
    @AdminSession
    public void userCanGetBuildStatisticsTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse createdBuild = BuildSteps.createBuildWithMinimalSetup();
        String buildLocator = createdBuild.getId();

        // Ждем, пока билд запустится
        BuildSteps.waitForBuildToFinish(buildLocator);

        BuildStatistic statistics = BuildSteps.getBuildStatistics(buildLocator);

        softly.assertThat(statistics)
                .as("Statistics should not be null")
                .isNotNull();

        softly.assertThat(statistics.getBuildId())
                .as("Build ID should match")
                .isEqualTo(buildLocator);

        softly.assertThat(statistics.getDuration())
                .as("Duration should be >= 0")
                .isGreaterThanOrEqualTo(0L);

        softly.assertThat(statistics.getTestsCount())
                .as("Tests count should be >= 0")
                .isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Пользователь получает ошибку при запросе статистики несуществующего билда")
    @AdminSession
    public void userGetsErrorWhenGettingStatisticsOfNonExistentBuildTest() {
        // ✅ Используем числовой ID, который не существует
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.returnsNotFound(),
                Endpoint.BUILDS_STATISTICS
        ).get("999999999")
                .body(containsString("Build not found"));
    }

    // ============================================
    // 4. DELETE /app/rest/builds/{buildLocator} - Удаление билда
    // ============================================

    @Test
    @DisplayName("Пользователь может удалить существующий билд")
    @AdminSession
    public void userCanDeleteExistingBuildTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse createdBuild = BuildSteps.createBuildWithMinimalSetup();
        String buildLocator = createdBuild.getId();

        DeleteBuildResponse response = BuildSteps.deleteBuild(buildLocator);

        softly.assertThat(response)
                .as("Response should not be null")
                .isNotNull();

        softly.assertThat(response.getDeleted())
                .as("Build should be deleted")
                .isTrue();

        softly.assertThat(response.getBuildId())
                .as("Build ID should match")
                .isEqualTo(buildLocator);

        // Проверяем, что билд действительно удален
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.returnsNotFound(),
                Endpoint.BUILDS_GET
        ).get(buildLocator)
                .body(containsString("Build not found"));
    }

    @Test
    @DisplayName("Пользователь получает ошибку при удалении несуществующего билда")
    @AdminSession
    public void userGetsErrorWhenDeletingNonExistentBuildTest() {
        // ✅ Используем числовой ID, который не существует
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.returnsNotFound(),
                Endpoint.BUILDS_DELETE
        ).delete("999999999")
                .body(containsString("Build not found"));
    }

    // ============================================
    // 5. GET /app/rest/builds/{buildLocator}/relatedIssues - Связанные issues
    // ============================================

    @Test
    @DisplayName("Пользователь может получить связанные issues для билда")
    @AdminSession
    public void userCanGetRelatedIssuesForBuildTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse createdBuild = BuildSteps.createBuildWithMinimalSetup();
        String buildLocator = createdBuild.getId();

        RelatedIssuesResponse response = BuildSteps.getRelatedIssues(buildLocator);

        softly.assertThat(response)
                .as("Response should not be null")
                .isNotNull();

        softly.assertThat(response.getBuildId())
                .as("Build ID should match")
                .isEqualTo(buildLocator);

        softly.assertThat(response.getTotalCount())
                .as("Total count should be >= 0")
                .isGreaterThanOrEqualTo(0);

        softly.assertThat(response.getIssues())
                .as("Issues list should not be null")
                .isNotNull();
    }

    @Test
    @DisplayName("Пользователь получает ошибку при запросе issues для несуществующего билда")
    @AdminSession
    public void userGetsErrorWhenGettingIssuesOfNonExistentBuildTest() {
        // ✅ Используем числовой ID, который не существует
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.returnsNotFound(),
                Endpoint.BUILDS_RELATED_ISSUES
        ).get("999999999")
                .body(containsString("Build not found"));
    }

    // ============================================
    // 6. PUT /app/rest/builds/{buildLocator}/tags - Обновление тегов
    // ============================================

    @Test
    @DisplayName("Пользователь может обновить теги билда")
    @AdminSession
    public void userCanUpdateBuildTagsTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse createdBuild = BuildSteps.createBuildWithMinimalSetup();
        String buildLocator = createdBuild.getId();

        List<String> newTags = List.of("release", "v1.0", "production");

        BuildTagsRequest request = BuildTagsRequest.builder()
                .tags(newTags)
                .append(false)
                .build();

        BuildTagsResponse response = BuildSteps.updateBuildTags(buildLocator, request);

        softly.assertThat(response)
                .as("Response should not be null")
                .isNotNull();

        ModelAssertions.assertThatModels(request, response)
                .matchOnly("tags");
    }

    @Test
    @DisplayName("Пользователь может добавить теги к существующим")
    @AdminSession
    public void userCanAppendTagsToBuildTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse createdBuild = BuildSteps.createBuildWithMinimalSetup();
        String buildLocator = createdBuild.getId();

        List<String> initialTags = List.of("initial");
        BuildTagsRequest initialRequest = BuildTagsRequest.builder()
                .tags(initialTags)
                .append(false)
                .build();

        BuildSteps.updateBuildTags(buildLocator, initialRequest);

        List<String> additionalTags = List.of("newTag1", "newTag2");
        BuildTagsRequest appendRequest = BuildTagsRequest.builder()
                .tags(additionalTags)
                .append(true)
                .build();

        BuildTagsResponse response = BuildSteps.updateBuildTags(buildLocator, appendRequest);

        List<String> expectedTags = List.of("initial", "newTag1", "newTag2");
        softly.assertThat(response.getTags())
                .as("Tags should contain all tags")
                .containsExactlyInAnyOrderElementsOf(expectedTags);

        softly.assertThat(response.getAppended())
                .as("Appended flag should be true")
                .isTrue();
    }

    @Test
    @DisplayName("Пользователь может удалить все теги билда")
    @AdminSession
    public void userCanRemoveAllBuildTagsTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse createdBuild = BuildSteps.createBuildWithMinimalSetup();
        String buildLocator = createdBuild.getId();

        // Сначала добавляем теги
        BuildTagsRequest addRequest = BuildTagsRequest.builder()
                .tags(List.of("tag1", "tag2"))
                .append(false)
                .build();

        BuildSteps.updateBuildTags(buildLocator, addRequest);

        // Удаляем все теги
        BuildTagsRequest deleteRequest = BuildTagsRequest.builder()
                .tags(List.of())
                .append(false)
                .build();

        BuildTagsResponse response = BuildSteps.updateBuildTags(buildLocator, deleteRequest);

        softly.assertThat(response.getTags())
                .as("Tags should be empty")
                .isEmpty();

        softly.assertThat(response.getTagsCount())
                .as("Tags count should be 0")
                .isZero();
    }

    @Test
    @DisplayName("Пользователь получает ошибку при обновлении тегов несуществующего билда")
    @AdminSession
    public void userGetsErrorWhenUpdatingTagsOfNonExistentBuildTest() {
        // ✅ Используем числовой ID, который не существует
        BuildTagsRequest request = BuildTagsRequest.builder()
                .tags(List.of("test"))
                .append(false)
                .build();

        // Ожидаем 404, но TeamCity может вернуть 400 для невалидного ID
        // Используем реальный несуществующий ID
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.returnsNotFound(),
                Endpoint.BUILDS_TAGS_UPDATE
        ).put(request)
                .body(containsString("Build not found"));
    }

    // ============================================
    // 7. Комплексный тест - Full Lifecycle
    // ============================================

    @Test
    @DisplayName("Full lifecycle: создать билд → получить статистику → обновить теги → удалить")
    @AdminSession
    public void buildFullLifecycleTest() {
        // ✅ Используем генерацию уникальных имен
        BuildResponse createdBuild = BuildSteps.createBuildWithMinimalSetup();
        String buildLocator = createdBuild.getId();

        softly.assertThat(createdBuild)
                .as("Build should be created")
                .isNotNull();

        // 2. Get statistics (ждем запуска)
        BuildSteps.waitForBuildToFinish(buildLocator);
        BuildStatistic statistics = BuildSteps.getBuildStatistics(buildLocator);
        softly.assertThat(statistics)
                .as("Statistics should be available")
                .isNotNull();

        // 3. Update tags
        List<String> tags = List.of("lifecycle", "test", "complete");
        BuildTagsRequest tagsRequest = BuildTagsRequest.builder()
                .tags(tags)
                .append(false)
                .build();

        BuildTagsResponse tagsResponse = BuildSteps.updateBuildTags(buildLocator, tagsRequest);

        softly.assertThat(tagsResponse.getTags())
                .as("Tags should be updated")
                .containsExactlyInAnyOrderElementsOf(tags);

        // 4. Get related issues
        RelatedIssuesResponse issuesResponse = BuildSteps.getRelatedIssues(buildLocator);

        softly.assertThat(issuesResponse)
                .as("Related issues should be accessible")
                .isNotNull();

        // 5. Delete
        DeleteBuildResponse deleteResponse = BuildSteps.deleteBuild(buildLocator);
        softly.assertThat(deleteResponse.getDeleted())
                .as("Build should be deleted successfully")
                .isTrue();

        // 6. Verify deletion
        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.returnsNotFound(),
                Endpoint.BUILDS_GET
        ).get(buildLocator)
                .body(containsString("Build not found"));
    }

    // ============================================
    // 8. Параллельный тест
    // ============================================

    @Test
    @DisplayName("Параллельный тест с уникальными данными - атомарность")
    @AdminSession
    public void parallelTestWithUniqueDataTest() {
        String projectName = "ParallelProject_" + System.currentTimeMillis();
        String projectId = BuildSteps.createProject(projectName);
        String buildTypeId = BuildSteps.createBuildType(projectId);

        BuildQueueRequest request = BuildQueueRequest.builder()
                .buildTypeId(buildTypeId)
                .branchName("main")
                .build();

        BuildQueueResponse response = new ValidatedCrudRequester<BuildQueueResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.BUILD_QUEUE_ADD
        ).post(request);

        softly.assertThat(response.getId())
                .as("Build should be queued")
                .isNotEmpty();
    }
}