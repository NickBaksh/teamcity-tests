package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String name;
    private String projectId;
    private String projectName;
    private String description;
    private String href;
    private String webUrl;
    private Boolean paused;
    private String internalId;
    private Boolean templateFlag;
    private String type;
    private String uuid;
    private String projectInternalId;
}