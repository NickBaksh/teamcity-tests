package com.teamcity.api.requests.steps;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestBuildContext {
    private String buildId;
    private String buildTypeId;
    private String branchName;
    private String status;
    private String queuedId; // если добавлен в очередь

    // Вспомогательные методы
    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return "FAILURE".equalsIgnoreCase(status);
    }

    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(status);
    }

    public String getDisplayName() {
        return String.format("Build %s (%s) - %s", buildId, buildTypeId, status);
    }
}