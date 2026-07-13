package com.teamcity.api.models.dto_models.issue;

import com.teamcity.api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RelatedIssuesResponse extends BaseModel {
    private String buildId;
    private List<RelatedIssue> issues;
    private Integer totalCount;

    @Builder.Default
    private Integer openIssuesCount = 0;

    @Builder.Default
    private Integer resolvedIssuesCount = 0;

    @Builder.Default
    private Integer criticalIssuesCount = 0;

    @Builder.Default
    private Integer blockingIssuesCount = 0;

    // Методы-помощники
    public boolean hasIssues() {
        return issues != null && !issues.isEmpty();
    }

    public boolean hasOpenIssues() {
        return openIssuesCount > 0;
    }

    public boolean hasBlockingIssues() {
        return blockingIssuesCount > 0;
    }

    public List<RelatedIssue> getOpenIssues() {
        if (issues == null) return List.of();
        return issues.stream()
                .filter(RelatedIssue::isOpen)
                .toList();
    }

    public List<RelatedIssue> getBlockingIssues() {
        if (issues == null) return List.of();
        return issues.stream()
                .filter(RelatedIssue::getIsBlocking)
                .toList();
    }
}
