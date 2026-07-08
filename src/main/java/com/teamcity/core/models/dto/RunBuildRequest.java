// src/main/java/com/teamcity/core/models/dto/RunBuildRequest.java
package com.teamcity.core.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunBuildRequest {
    private String buildTypeId;
    private String branchName;
    private Boolean cleanSources;
    private Map<String, String> parameters;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("buildTypeId", buildTypeId);

        if (branchName != null) {
            map.put("branchName", branchName);
        }

        if (cleanSources != null) {
            map.put("cleanSources", cleanSources);
        }

        if (parameters != null && !parameters.isEmpty()) {
            map.put("parameters", parameters);
        }

        return map;
    }

}