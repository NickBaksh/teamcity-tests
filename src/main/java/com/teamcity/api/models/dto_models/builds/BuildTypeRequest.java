package com.teamcity.api.models.dto_models.builds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.teamcity.api.generators.GeneratingRule;
import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildTypeRequest extends BaseModel {

    @GeneratingRule(regex = "^BuildType_[A-Za-z0-9]{8}$")
    private String id;

    @GeneratingRule(regex = "^BuildType_[A-Za-z]{6,10}$")
    private String name;

    @GeneratingRule(regex = "^Project_[A-Za-z0-9]{8}$")
    private String projectId;
}