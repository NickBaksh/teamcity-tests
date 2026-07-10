package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Модель VCS Root в TeamCity.
 * Соответствует сущности vcs-root в TeamCity REST API.
 *
 * @see <a href="https://www.jetbrains.com/help/teamcity/rest/manage-vcs-roots.html">TeamCity VCS Roots REST API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VcsRoot {

    // === Основные поля ===

    /**
     * Уникальный ID VCS Root
     * Пример: "MyProject_MyVcsRoot"
     */
    private String id;

    /**
     * Название VCS Root
     */
    private String name;

    /**
     * Тип VCS (jetbrains.git, github, gitlab, svn, perforce, tfs и т.д.)
     */
    @JsonProperty("vcsName")
    private String vcsName;

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
     * Проект, к которому принадлежит VCS Root
     */
    private Project project;

    /**
     * Свойства VCS Root (URL, credentials, ветки и т.д.)
     */
    private Map<String, String> properties;

    /**
     * Экземпляры VCS Root
     */
    private VcsRootInstances instances;

    // === Вложенные классы ===

    /**
     * Экземпляры VCS Root
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VcsRootInstances {
        private int count;
        @JsonProperty("vcs-root-instance")
        private List<VcsRootInstance> items;
        private String href;
    }

    /**
     * Экземпляр VCS Root
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VcsRootInstance {
        private String id;
        private String name;
        private String vcsName;
        private String href;
        @JsonProperty("webUrl")
        private String webUrl;
        private Project project;
        private Map<String, String> properties;
        private VcsRootInstanceStatus status;
    }

    /**
     * Статус экземпляра VCS Root
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VcsRootInstanceStatus {
        private String status;
        @JsonProperty("statusText")
        private String statusText;
    }

    /**
     * Параметр VCS Root
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VcsRootParameter {
        private String name;
        private String value;
    }

    /**
     * Параметры VCS Root
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VcsRootParameters {
        private int count;
        @JsonProperty("property")
        private List<VcsRootParameter> items;
        private String href;
    }
}