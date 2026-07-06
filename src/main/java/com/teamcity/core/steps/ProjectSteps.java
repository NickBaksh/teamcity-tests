package com.teamcity.core.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.models.Project;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ProjectSteps {
    private final RestClient client;

    public ProjectSteps(RestClient client) {
        this.client = client;
    }

    @Step("Create project with name: {projectName}")
    public Project createProject(String projectName) {
        return createProject(projectName, "_Root");
    }

    @Step("Create project with name: {projectName}, parent: {parentProjectId}")
    public Project createProject(String projectName, String parentProjectId) {
        Project project = Project.builder()
                .name(projectName)
                .parentProjectId(parentProjectId)
                .build();

        Response response = client.post("/app/rest/projects", project);
        assertEquals(200, response.statusCode(), "Failed to create project");
        assertNotNull(response.getBody(), "Response body is empty");

        Project created = response.as(Project.class);
        assertNotNull(created.getId(), "Project ID is null");
        assertEquals(projectName, created.getName(), "Project name mismatch");

        log.info("Project created: ID={}, Name={}", created.getId(), created.getName());
        return created;
    }

    @Step("Get project by ID: {projectId}")
    public Project getProject(String projectId) {
        Response response = client.get("/app/rest/projects/{projectLocator}", projectId);
        assertEquals(200, response.statusCode(), "Failed to get project: " + response.getBody().asString());

        return response.as(Project.class);
    }

    @Step("Get all projects")
    public List<Project> getAllProjects() {
        Response response = client.get("/app/rest/projects");
        assertEquals(200, response.statusCode(), "Failed to get all projects");

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody().asString());
            JsonNode projectsNode = root.get("project");
            if (projectsNode == null || !projectsNode.isArray()) {
                return new ArrayList<>();
            }
            return mapper.convertValue(projectsNode, new TypeReference<List<Project>>() {});
        } catch (Exception e) {
            log.error("Failed to parse projects list", e);
            return new ArrayList<>();
        }
    }

    @Step("Update project: {projectId} to name: {newName}")
    public Project updateProject(String projectId, String newName) {
        Response response = client.put("/app/rest/projects/{projectLocator}/name", newName, projectId);
        assertEquals(200, response.statusCode(), "Failed to update project");

        return getProject(projectId);
    }

    @Step("Delete project: {projectId}")
    public void deleteProject(String projectId) {
        Response response = client.delete("/app/rest/projects/{projectLocator}", projectId);
        assertEquals(204, response.statusCode(), "Failed to delete project: " + response.getBody().asString());
        log.info("Project deleted: ID={}", projectId);
    }

    @Step("Check if project exists: {projectId}")
    public boolean projectExists(String projectId) {
        Response response = client.get("/app/rest/projects/{projectLocator}", projectId);
        return response.statusCode() == 200;
    }
}