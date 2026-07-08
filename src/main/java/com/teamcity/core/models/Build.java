// src/main/java/com/teamcity/core/models/Build.java
package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // Игнорируем неизвестные поля
public class Build {
    private String id;
    private String buildTypeId;
    private String state;
    private String status;
    private String statusText;
    private String href;
    private String webUrl;
    private Map<String, String> parameters;

    // Добавляем поле buildType, если нужно
    private BuildType buildType;

    // Дополнительные поля по необходимости
    private String queuedDate;
    private String waitReason;
    private Triggered triggered;
    private Changes changes;
    private Revisions revisions;
    private CompatibleAgents compatibleAgents;
    private Artifacts artifacts;

    // Вложенные классы для сложных объектов
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Triggered {
        private String type;
        private String date;
        private User user;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String username;
        private int id;
        private String href;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Changes {
        private String href;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Revisions {
        private int count;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompatibleAgents {
        private String href;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artifacts {
        private String href;
    }
}