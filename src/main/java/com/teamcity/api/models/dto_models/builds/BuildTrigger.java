package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildTrigger extends BaseModel {
    private String id;
    private String type;           // MANUAL, SCHEDULED, VCS, DEPENDENCY, RETRY

    @Builder.Default
    private String triggeredBy = "User";

    private String timestamp;
    private String userId;
    private String userName;
    private String sourceBuildId;

    // Дополнительная информация о триггере
    private String scheduledTime;
    private String vcsRevision;
    private String branchName;

    // Методы-помощники
    public boolean isManual() {
        return "MANUAL".equalsIgnoreCase(type);
    }

    public boolean isScheduled() {
        return "SCHEDULED".equalsIgnoreCase(type);
    }

    public boolean isVcsTriggered() {
        return "VCS".equalsIgnoreCase(type);
    }
}
