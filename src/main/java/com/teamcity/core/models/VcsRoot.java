package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VcsRoot {
    private String id;
    private String name;
    private String vcsName;
    private String url;
    private Project project;
    private String projectName;
    private String href;
    private String webUrl;
    private String description;
    private PropertiesContainer properties;
    private VcsRootInfo vcsRoot;
    private String status;
    private String statusText;

    public String getProjectId() {
        return project != null ? project.getId() : null;
    }

    public String getProjectName() {
        return project != null ? project.getName() : null;
    }

    /** Prefer top-level url; fall back to properties.url (TeamCity GET shape). */
    public String getUrl() {
        if (url != null && !url.isBlank()) {
            return url;
        }
        return propertyValue("url");
    }

    public String propertyValue(String propertyName) {
        if (properties == null || properties.getProperty() == null) {
            return null;
        }
        return properties.getProperty().stream()
                .filter(property -> propertyName.equals(property.getName()))
                .map(Property::getValue)
                .findFirst()
                .orElse(null);
    }
}
