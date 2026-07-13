package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teamcity.core.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuildConfig {
    private String id;

    @GeneratingRule(regex = "BuildConfig_[0-9]{10,13}_[a-z0-9]{8}")
    private String name;

    private String projectId;
    private String projectName;
    private String description;
    private String href;
    private String webUrl;
    private Boolean paused;
}
