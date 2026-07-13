package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.RequestType;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ProjectCreationException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.NewProjectDescription;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.ProjectMoveRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ProjectSteps extends BaseSteps {

    private static final String ROOT_PROJECT_ID = "_Root";

    public ProjectSteps(ApiClient client) {
        super(client);
    }

    public ProjectSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    @Step("Create project: {project.name}")
    public Project createProject(Project project) {
        Response response = client.post(Endpoint.PROJECTS.getPath(), project);
        Project created = validator.validate(response, Project.class);
        log.info("Project created: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    @Step("Create project under parent: {project.name} -> {parentId}")
    public Project createProjectUnderParent(Project project, String parentId) {
        validateCreateProject(project, parentId);
        validateParentExists(parentId);

        NewProjectDescription request = NewProjectDescription.createChild(project.getName(), parentId);
        request.setDescription(project.getDescription());

        try {
            Response response = client.post(Endpoint.PROJECTS.getPath(), request);
            return validator.validate(response, Project.class);
        } catch (Exception e) {
            throw new ProjectCreationException(
                    String.format("Failed to create project '%s' under parent '%s'", project.getName(), parentId),
                    e
            );
        }
    }

    @Step("Create project under parent (two-step): {project.name} -> {parentId}")
    public Project createProjectUnderParentTwoStep(Project project, String parentId) {
        validateCreateProject(project, parentId);
        validateParentExists(parentId);
        Project created = createProject(project);
        return moveProject(created.getId(), parentId);
    }

    @Step("Smart create project under parent: {project.name} -> {parentId}")
    public Project createProjectSmartUnderParent(Project project, String parentId) {
        try {
            return createProjectUnderParent(project, parentId);
        } catch (Exception e) {
            log.warn("Single-step create failed, fallback to two-step: {}", e.getMessage());
            return createProjectUnderParentTwoStep(project, parentId);
        }
    }

    @Step("Smart create project: {project.name}")
    public Project createProjectSmart(Project project) {
        String parentId = project.getParentProjectId();
        if (parentId != null && !parentId.isEmpty() && !ROOT_PROJECT_ID.equals(parentId)) {
            return createProjectSmartUnderParent(project, parentId);
        }
        return createProject(project);
    }

    @Step("Get project: {projectId}")
    public Project getProject(String projectId) {
        Response response = client.get(Endpoint.PROJECT.format(projectId), RequestType.JSON);
        return validator.validate(response, Project.class);
    }

    @Step("Get all projects")
    public List<Project> getAllProjects() {
        Response response = client.get(Endpoint.PROJECTS.getPath());
        List<Project> projects = validator.validate(
                response,
                res -> res.jsonPath().getList("project", Project.class)
        );
        return projects != null ? projects : Collections.emptyList();
    }

    @Step("Get child projects: {parentProjectId}")
    public List<Project> getChildProjects(String parentProjectId) {
        // ProjectLocator uses `project` (direct parent), not parentProject — see TeamCity REST swagger
        String endpoint = Endpoint.PROJECTS.getPath()
                + "?locator=project:(id:" + parentProjectId + ")";
        Response response = client.get(endpoint);
        List<Project> projects = validator.validate(
                response,
                res -> res.jsonPath().getList("project", Project.class)
        );
        return projects != null ? projects : Collections.emptyList();
    }

    @Step("Update project name: {projectId} -> {newName}")
    public Project updateProject(String projectId, String newName) {
        Response response = client.putText(Endpoint.PROJECT_NAME.format(projectId), newName);
        validator.validateStatus(response);
        return getProject(projectId);
    }

    @Step("Update project description: {projectId}")
    public Project updateProjectDescription(String projectId, String newDescription) {
        Response response = client.putText(Endpoint.PROJECT_DESCRIPTION.format(projectId), newDescription);
        validator.validateStatus(response);
        return getProject(projectId);
    }

    @Step("Move project: {projectId} -> {newParentId}")
    public Project moveProject(String projectId, String newParentId) {
        validateProjectExists(projectId);
        validateParentExists(newParentId);

        ProjectMoveRequest request = ProjectMoveRequest.builder()
                .parentProject(ProjectMoveRequest.ProjectReference.of(newParentId))
                .build();

        Response response = client.put(Endpoint.PROJECT.format(projectId), request);
        validator.validateStatus(response);
        return getProject(projectId);
    }

    @Step("Delete project: {projectId}")
    public void deleteProject(String projectId) {
        Response response = client.delete(Endpoint.PROJECT.format(projectId));
        validator.validateStatus(response);
        log.info("Project deleted: {}", projectId);
    }

    @Step("Delete project if exists: {projectId}")
    public boolean deleteProjectIfExists(String projectId) {
        if (!projectExists(projectId)) {
            return false;
        }
        deleteProject(projectId);
        return true;
    }

    @Step("Check project exists: {projectId}")
    public boolean projectExists(String projectId) {
        try {
            getProject(projectId);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (RuntimeException e) {
            log.debug("projectExists({}) -> false: {}", projectId, e.getMessage());
            return false;
        }
    }

    @Step("Find project by name: {name}")
    public Optional<Project> findProjectByName(String name) {
        return getAllProjects().stream()
                .filter(project -> name.equals(project.getName()))
                .findFirst();
    }

    @Step("Find projects by name prefix: {prefix}")
    public List<Project> findProjectsByNamePrefix(String prefix) {
        return getAllProjects().stream()
                .filter(project -> project.getName() != null && project.getName().startsWith(prefix))
                .collect(Collectors.toList());
    }

    private void validateCreateProject(Project project, String parentId) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        if (parentId == null || parentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Parent project ID cannot be null or empty");
        }
    }

    private void validateProjectExists(String projectId) {
        if (!projectExists(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
    }

    private void validateParentExists(String parentId) {
        if (!ROOT_PROJECT_ID.equals(parentId) && !projectExists(parentId)) {
            throw new ResourceNotFoundException("Parent project not found: " + parentId);
        }
    }
}
