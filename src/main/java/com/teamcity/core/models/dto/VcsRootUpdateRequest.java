package com.teamcity.core.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VcsRootUpdateRequest {
    private String name;
    private String url;
    private String branch;
    private String description;
    private String username;
    private String password;
}
