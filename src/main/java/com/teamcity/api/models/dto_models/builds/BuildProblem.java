package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildProblem extends BaseModel {
    private String id;
    private String buildId;
    private String type;  // COMPILATION, TEST, DEPENDENCY, TIMEOUT, UNKNOWN

    @Builder.Default
    private String severity = "ERROR";  // ERROR, WARNING, INFO

    private String message;
    private String description;
    private String stackTrace;

    @Builder.Default
    private Boolean isResolved = false;

    private String resolvedDate;
    private String resolution;

    // Для тестовых проблем
    private String testClassName;
    private String testMethodName;

    // Для проблем зависимостей
    private String dependencyId;
    private String dependencyName;

    // Для проблем сборки
    private String compilationErrorLine;
    private String filePath;
    @Builder.Default
    private Integer lineNumber = 0;
    @Builder.Default
    private Integer columnNumber = 0;

    // Дополнительная информация
    private List<String> relatedIssues;
    private List<String> suggestions;

    // Методы-помощники
    public boolean isCompilationProblem() {
        return "COMPILATION".equalsIgnoreCase(type);
    }

    public boolean isTestProblem() {
        return "TEST".equalsIgnoreCase(type);
    }

    public boolean isTimeoutProblem() {
        return "TIMEOUT".equalsIgnoreCase(type);
    }

    public boolean isDependencyProblem() {
        return "DEPENDENCY".equalsIgnoreCase(type);
    }

    public boolean isCritical() {
        return "ERROR".equalsIgnoreCase(severity);
    }

    public String getShortMessage() {
        if (message == null) return "";
        return message.length() > 100 ? message.substring(0, 100) + "..." : message;
    }
}
