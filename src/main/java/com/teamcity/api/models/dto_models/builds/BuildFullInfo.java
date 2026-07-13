package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import com.teamcity.api.models.dto_models.issue.RelatedIssue;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildFullInfo extends BaseModel {
    private BuildResponse build;
    private BuildStatistic statistics;
    private List<RelatedIssue> relatedIssues;
    private BuildTrigger trigger;
    private BuildAgent agent;
    private BuildChanges changes;
    private List<BuildProblem> problems;
    private List<String> tags;
    private BuildQueueResponse queueInfo;

    // Методы-помощники
    public boolean isBuildSuccessful() {
        return build != null && "SUCCESS".equalsIgnoreCase(build.getStatus());
    }

    public boolean hasTestFailures() {
        return statistics != null && statistics.getTestsFailed() > 0;
    }

    public boolean hasProblems() {
        return problems != null && !problems.isEmpty();
    }

    public List<String> getAllIssuesIds() {
        if (relatedIssues == null) return List.of();
        return relatedIssues.stream()
                .map(RelatedIssue::getId)
                .toList();
    }

    public int getTotalProblemsCount() {
        if (problems == null) return 0;
        return problems.size();
    }

    public int getCriticalProblemsCount() {
        if (problems == null) return 0;
        return (int) problems.stream()
                .filter(BuildProblem::isCritical)
                .count();
    }
}
