package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildStatistic extends BaseModel {
    private String buildId;
    private String buildTypeId;

    @Builder.Default
    private Long duration = 0L;           // в миллисекундах

    @Builder.Default
    private Integer testsCount = 0;

    @Builder.Default
    private Integer testsPassed = 0;

    @Builder.Default
    private Integer testsFailed = 0;

    @Builder.Default
    private Integer testsIgnored = 0;

    private Double coveragePercentage;

    @Builder.Default
    private Integer artifactsSize = 0;    // в байтах

    private String compilationStatus;
    private String testStatus;
    private String codeCoverageStatus;

    // Дополнительная статистика
    private Map<String, Object> customStatistics;
    private List<String> warnings;
    private List<String> errors;

    // Время выполнения по этапам
    @Builder.Default
    private Long checkoutDuration = 0L;
    @Builder.Default
    private Long compilationDuration = 0L;
    @Builder.Default
    private Long testExecutionDuration = 0L;
    @Builder.Default
    private Long packagingDuration = 0L;
}