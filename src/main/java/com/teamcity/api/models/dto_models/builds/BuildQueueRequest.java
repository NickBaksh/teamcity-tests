package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.generators.GeneratingRule;
import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildQueueRequest extends BaseModel {

    @GeneratingRule(regex = "^[A-Za-z0-9_-]{3,20}$")
    private String buildTypeId;

    @GeneratingRule(regex = "^(main|develop|feature/[A-Za-z0-9_-]{3,15})$")
    private String branchName;

    private String comment;

    @Builder.Default
    private Boolean cleanSources = false;

    @Builder.Default
    private Boolean rebuildAllDependencies = false;
}