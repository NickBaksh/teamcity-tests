package com.teamcity.api.models.dto_models.issue;

import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RelatedIssue extends BaseModel {
    private String id;
    private String url;
    private String issueType;    // BUG, TASK, STORY, EPIC
    private String status;       // OPEN, RESOLVED, CLOSED
    private String summary;
    private String priority;     // CRITICAL, HIGH, MEDIUM, LOW

    @Builder.Default
    private Boolean isResolved = false;

    @Builder.Default
    private Boolean isBlocking = false;

    private String assignee;
    private String createdDate;
    private String resolvedDate;
    private String projectKey;
    private String component;
    private String fixVersion;

    // Методы-помощники
    public boolean isBug() {
        return "BUG".equalsIgnoreCase(issueType);
    }

    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(priority);
    }

    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status);
    }
}
