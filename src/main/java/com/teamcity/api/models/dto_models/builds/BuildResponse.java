package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import com.teamcity.api.models.dto_models.issue.RelatedIssue;
import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildResponse extends BaseModel {
    private String id;
    private String buildTypeId;
    private String number;
    private String status;      // SUCCESS, FAILURE, CANCELLED, RUNNING
    private String state;       // QUEUED, RUNNING, FINISHED
    private String branchName;
    private String startDate;
    private String finishDate;
    private List<String> tags;
    private List<RelatedIssue> relatedIssues;

    // Информация о триггере
    private BuildTrigger trigger;

    // Информация об агенте
    private BuildAgent agent;

    // Статистика (опционально)
    private BuildStatistic statistics;

    // Параметры билда
    private Map<String, String> parameters;

    // Методы-помощники
    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return "FAILURE".equalsIgnoreCase(status);
    }

    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(status);
    }

    public boolean isFinished() {
        return "FINISHED".equalsIgnoreCase(state);
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    public long getDurationInMillis() {
        if (statistics != null && statistics.getDuration() != null) {
            return statistics.getDuration();
        }
        return 0L;
    }

    public String getFormattedDuration() {
        long duration = getDurationInMillis();
        if (duration == 0) return "N/A";

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
