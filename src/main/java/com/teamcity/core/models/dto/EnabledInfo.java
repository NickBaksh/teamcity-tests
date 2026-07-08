package com.teamcity.core.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnabledInfo {
    private Boolean status;
    private Comment comment;
    private String statusSwitchTime;
}