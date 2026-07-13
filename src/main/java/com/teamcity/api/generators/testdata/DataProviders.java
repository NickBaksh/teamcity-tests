package com.teamcity.api.generators.testdata;

import com.teamcity.api.generators.RandomModelGenerator;
import com.teamcity.api.models.dto_models.builds.BuildQueueRequest;
import com.teamcity.api.models.dto_models.builds.BuildTagsRequest;
import com.teamcity.api.models.dto_models.builds.BuildTypeRequest;
import com.teamcity.api.models.dto_models.projects.ProjectRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Централизованный провайдер тестовых данных
 * Генерация происходит через модели с аннотацией @GeneratingRule
 */
public class DataProviders {

    private static final String DEFAULT_BRANCH = "main";
    private static final String NON_EXISTENT_BUILD_ID = "999999999";

    // ============================================
    // ПРОЕКТЫ (Projects) — через модель ProjectRequest
    // ============================================

    /**
     * Сгенерировать запрос на создание проекта с уникальными данными
     */
    public static ProjectRequest generateProjectRequest() {
        return RandomModelGenerator.generateWithBuilder(ProjectRequest.class);
    }

    /**
     * Получить уникальное имя проекта
     */
    public static String generateUniqueProjectName() {
        return generateProjectRequest().getName();
    }

    /**
     * Получить уникальный ID проекта
     */
    public static String generateUniqueProjectId() {
        return generateProjectRequest().getId();
    }

    /**
     * Получить поток уникальных имен проектов
     */
    public static Stream<String> uniqueProjectNames() {
        return Stream.generate(DataProviders::generateUniqueProjectName).limit(5);
    }

    // ============================================
    // BUILD TYPES — через модель BuildTypeRequest
    // ============================================

    /**
     * Сгенерировать запрос на создание Build Type с уникальными данными
     */
    public static BuildTypeRequest generateBuildTypeRequest() {
        return RandomModelGenerator.generateWithBuilder(BuildTypeRequest.class);
    }

    /**
     * Получить уникальный ID Build Type
     */
    public static String generateUniqueBuildTypeId() {
        return generateBuildTypeRequest().getId();
    }

    /**
     * Получить уникальное имя Build Type
     */
    public static String generateUniqueBuildTypeName() {
        return generateBuildTypeRequest().getName();
    }

    /**
     * Получить поток валидных Build Type ID
     */
    public static Stream<String> validBuildTypeIds() {
        return Stream.generate(DataProviders::generateUniqueBuildTypeId).limit(5);
    }

    /**
     * Получить поток невалидных Build Type ID
     */
    public static Stream<String> invalidBuildTypeIds() {
        return Stream.of(
                "",                    // пустая строка
                "a",                   // слишком короткая
                "A".repeat(100),       // слишком длинная
                "test with space",     // с пробелом
                "test@#$%",            // со спецсимволами
                "non-existent-id",     // несуществующий ID
                "_test",               // начинается с подчеркивания
                "test-"                // заканчивается на дефис
        );
    }

    // ============================================
    // ВЕТКИ (Branches) — через модель BuildQueueRequest
    // ============================================

    /**
     * Получить валидное имя ветки по умолчанию
     */
    public static String getDefaultBranchName() {
        return "main";
    }

    /**
     * Получить случайное валидное имя ветки через модель
     */
    public static String getRandomValidBranchName() {
        return RandomModelGenerator.generateWithBuilder(BuildQueueRequest.class).getBranchName();
    }

    /**
     * Получить поток валидных имен веток
     */
    public static Stream<String> validBranchNames() {
        return Stream.of(
                "main",
                "develop",
                "feature/test",
                "feature/TC-123",
                "hotfix/urgent",
                "release/v1.0"
        );
    }

    // ============================================
    // ТЕГИ (Tags) — через модель BuildTagsRequest
    // ============================================

    /**
     * Сгенерировать запрос на обновление тегов
     */
    public static BuildTagsRequest generateBuildTagsRequest() {
        return RandomModelGenerator.generateWithBuilder(BuildTagsRequest.class);
    }

    /**
     * Сгенерировать запрос на обновление тегов с append = true
     */
    public static BuildTagsRequest generateBuildTagsRequestWithAppend() {
        BuildTagsRequest request = generateBuildTagsRequest();
        request.setAppend(true);
        return request;
    }

    /**
     * Получить список валидных тегов
     */
    public static List<String> getDefaultTagList() {
        return generateBuildTagsRequest().getTags();
    }

    /**
     * Получить пустой список тегов
     */
    public static List<String> getEmptyTagList() {
        return List.of();
    }

    // ============================================
    // БИЛДЫ (Builds) — через модель BuildQueueRequest
    // ============================================

    /**
     * Сгенерировать запрос на создание билда с уникальными данными
     */
    public static BuildQueueRequest generateBuildQueueRequest() {
        return RandomModelGenerator.generateWithBuilder(BuildQueueRequest.class);
    }

    /**
     * Получить поток валидных Build Queue запросов
     */
    public static Stream<BuildQueueRequest> validBuildQueueRequests() {
        return Stream.generate(DataProviders::generateBuildQueueRequest).limit(3);
    }

    // ============================================
    // КОМБИНИРОВАННЫЕ ДАННЫЕ
    // ============================================

    /**
     * Получить все необходимые данные для создания билда
     */
    public static BuildTestData getBuildTestData() {
        BuildQueueRequest buildRequest = generateBuildQueueRequest();
        ProjectRequest projectRequest = generateProjectRequest();
        BuildTypeRequest buildTypeRequest = generateBuildTypeRequest();

        return BuildTestData.builder()
                .projectName(projectRequest.getName())
                .projectId(projectRequest.getId())
                .buildTypeId(buildTypeRequest.getId())
                .buildTypeName(buildTypeRequest.getName())
                .branchName(buildRequest.getBranchName())
                .tags(generateBuildTagsRequest().getTags())
                .build();
    }

    /**
     * Вспомогательный класс для хранения данных теста
     */
    @lombok.Builder
    @lombok.Data
    public static class BuildTestData {
        private String projectName;
        private String projectId;
        private String buildTypeId;
        private String buildTypeName;
        private String branchName;
        private List<String> tags;
    }
}