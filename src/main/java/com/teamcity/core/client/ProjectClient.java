package com.teamcity.core.client;

import com.teamcity.core.models.Project;
import java.util.List;

public interface ProjectClient {
    Project createProject(Project project);
    Project getProject(String projectId);
    List<Project> getAllProjects();
    Project updateProject(String projectId, String newName);
    Project updateProjectDescription(String projectId, String description);
    void deleteProject(String projectId);
    boolean projectExists(String projectId);
}