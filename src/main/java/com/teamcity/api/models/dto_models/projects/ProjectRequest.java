package com.teamcity.api.models.dto_models.projects;

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
public class ProjectRequest extends BaseModel {

    @GeneratingRule(regex = "^Project_[A-Za-z0-9]{8}$")
    private String id;

    @GeneratingRule(regex = "^Project_[A-Za-z]{6,10}_[0-9]{13}$")
    private String name;
}