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
public class Build {
    private Long id;
    private String buildTypeId;
    private String number;
    private String status;
    private String state;
    private Boolean running;
    private Boolean composite;
    private Boolean personal;
    private String href;
    private String webUrl;
    private String statusText;
    private String branchName;
    private Boolean pinned;
}