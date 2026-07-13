package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildAgent extends BaseModel {
    private String id;
    private String name;
    private String typeId;

    @Builder.Default
    private String status = "IDLE";  // IDLE, RUNNING, OFFLINE, DISABLED

    private String hostName;
    private String ipAddress;
    private String os;
    private String osVersion;
    private String javaVersion;

    @Builder.Default
    private Integer cpuCores = 4;

    @Builder.Default
    private Long memoryTotal = 8192L;  // в MB

    @Builder.Default
    private Long memoryAvailable = 4096L;  // в MB

    @Builder.Default
    private Long diskSpaceTotal = 102400L;  // в MB

    @Builder.Default
    private Long diskSpaceAvailable = 51200L;  // в MB

    @Builder.Default
    private Boolean isEnabled = true;

    @Builder.Default
    private Boolean isAuthorized = true;

    private String lastBuildStartDate;
    private String lastBuildFinishDate;
    private Integer currentBuildsCount;

    // Методы-помощники
    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(status);
    }

    public boolean isAvailable() {
        return "IDLE".equalsIgnoreCase(status) && isEnabled && isAuthorized;
    }

    public double getMemoryUsagePercent() {
        if (memoryTotal == 0) return 0.0;
        return (double) (memoryTotal - memoryAvailable) / memoryTotal * 100;
    }
}
