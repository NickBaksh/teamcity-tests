// src/main/java/com/teamcity/core/models/BuildType.java
package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Модель Build Type (Build Configuration) в TeamCity.
 * Соответствует сущности buildType в TeamCity REST API.
 *
 * @see <a href="https://www.jetbrains.com/help/teamcity/rest-api-build-types.html">TeamCity Build Types REST API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildType {

    // === Основные поля ===

    /**
     * Уникальный ID билд-конфига
     * Пример: "ProjectId_BuildConfigId"
     */
    private String id;

    /**
     * Название билд-конфига
     */
    private String name;

    /**
     * Описание билд-конфига
     */
    private String description;

    /**
     * ID проекта, к которому принадлежит билд-конфиг
     */
    @JsonProperty("projectId")
    private String projectId;

    /**
     * Название проекта
     */
    @JsonProperty("projectName")
    private String projectName;

    /**
     * Ссылка на ресурс в API
     */
    private String href;

    /**
     * Ссылка на страницу в веб-интерфейсе
     */
    @JsonProperty("webUrl")
    private String webUrl;

    /**
     * Приостановлен ли билд-конфиг
     */
    @JsonProperty("paused")
    private Boolean paused;

    /**
     * Причина приостановки (если приостановлен)
     */
    @JsonProperty("pausedComment")
    private String pausedComment;

    // === Вложенные объекты ===

    /**
     * Проект, к которому принадлежит билд-конфиг
     */
    private Project project;

    /**
     * Шаблоны, используемые в билд-конфиге
     */
    private Templates templates;

    /**
     * Настройки билд-конфига
     */
    private Settings settings;

    /**
     * Параметры билд-конфига
     */
    private Parameters parameters;

    /**
     * Шаги сборки
     */
    private Steps steps;

    /**
     * Особенности (features)
     */
    private Features features;

    /**
     * Триггеры
     */
    private Triggers triggers;

    /**
     * Зависимости по снапшотам
     */
    @JsonProperty("snapshot-dependencies")
    private SnapshotDependencies snapshotDependencies;

    /**
     * Артефактные зависимости
     */
    @JsonProperty("artifact-dependencies")
    private ArtifactDependencies artifactDependencies;

    /**
     * Требования к агенту
     */
    @JsonProperty("agent-requirements")
    private AgentRequirements agentRequirements;

    /**
     * Ссылка на список билдов
     */
    private Builds builds;

    /**
     * Ссылка на расследования
     */
    private Investigations investigations;

    /**
     * Совместимые агенты
     */
    @JsonProperty("compatibleAgents")
    private CompatibleAgents compatibleAgents;

    /**
     * Совместимые облачные образы
     */
    @JsonProperty("compatibleCloudImages")
    private CompatibleCloudImages compatibleCloudImages;

    // === Вложенные классы ===

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Templates {
        private int count;
        @JsonProperty("buildType")
        private List<BuildType> items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Settings {
        private int count;
        @JsonProperty("property")
        private List<Property> properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Property {
        private String name;
        private String value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parameters {
        private String href;
        private int count;
        @JsonProperty("property")
        private List<Property> properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Steps {
        private int count;
        private List<Step> step;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private String id;
        private String name;
        private String type;
        @JsonProperty("execution-timeout")
        private String executionTimeout;
        private Properties properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private int count;
        @JsonProperty("property")
        private List<Property> properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Features {
        private int count;
        private List<Feature> feature;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private String id;
        private String type;
        private Properties properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Triggers {
        private int count;
        private List<Trigger> trigger;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trigger {
        private String id;
        private String type;
        private Properties properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SnapshotDependencies {
        private int count;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArtifactDependencies {
        private int count;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentRequirements {
        private int count;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builds {
        private String href;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Investigations {
        private String href;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompatibleAgents {
        private String href;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompatibleCloudImages {
        private String href;
    }
}