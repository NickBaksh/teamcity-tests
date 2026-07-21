package com.teamcity.core.models.dto;

import com.teamcity.core.models.Project;
import com.teamcity.core.models.PropertiesContainer;
import lombok.*;

import java.util.Map;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateVcsRootRequest {
    private String name;
    private String url;
    private String branch;
    private String description;
    private String username;
    private String password;
    private String vcsName;
    private PropertiesContainer properties;
    private Project project;
}
