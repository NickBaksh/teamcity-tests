package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewProjectDescription {
    private String name;
    private String id;
    private String description;
    private String parentProjectId;

    @JsonProperty("parentProject")
    private ProjectReference parentProject;

    @JsonProperty("sourceProject")
    private ProjectReference sourceProject;

    // Factory method для создания подпроекта
    public static NewProjectDescription createChild(String name, String parentId) {
        return NewProjectDescription.builder()
                .name(name)
                .parentProject(ProjectReference.of(parentId))
                .build();
    }

    /** Parent/source project ref for create/copy — TeamCity expects locator, not bare id. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectReference {
        private String locator;

        public static ProjectReference of(String projectId) {
            return ProjectReference.builder().locator("id:" + projectId).build();
        }
    }
}