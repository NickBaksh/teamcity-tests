package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.HttpStatusCodes;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.*;
import com.teamcity.core.models.dto.CreateVcsRootRequest;
import com.teamcity.core.models.dto.VcsRootUpdateRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VcsRootSteps extends BaseSteps {

    public VcsRootSteps(ApiClient client) {
        super(client);
    }

    public VcsRootSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    @Step("Create VCS Root: {request.name}")
    public VcsRoot createVcsRoot(CreateVcsRootRequest request) {
        if (request.getVcsName() == null) {
            request.setVcsName("jetbrains.git");
        }

        Response response = client.post(Endpoint.VCS_ROOTS.getPath(), request);
        return validator.validate(response, VcsRoot.class);
    }

    @Step("Create VCS Root: {config.name}")
    public VcsRoot createVcsRoot(VcsRootConfig config) {
        PropertiesContainer properties = new PropertiesContainer();
        List<Property> propertyList = new ArrayList<>();

        propertyList.add(createProperty("url", config.getUrl()));
        propertyList.add(createProperty("authMethod", "PASSWORD"));

        properties.setProperty(propertyList);
        properties.setCount(propertyList.size());

        Project project = Project.builder()
                .id(config.getProjectId())
                .build();

        CreateVcsRootRequest request = CreateVcsRootRequest.builder()
                .name(config.getName())
                .vcsName("jetbrains.git")
                .url(config.getUrl())
                .project(project)
                .description(config.getDescription())
                .username(config.getUsername())
                .password(config.getPassword())
                .branch(config.getBranch())
                .properties(properties)
                .build();

        Response response = client.post(Endpoint.VCS_ROOTS.getPath(), request);
        VcsRoot created = validator.validate(response, VcsRoot.class);
        log.info("VCS Root created: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    private Property createProperty(String name, String value) {
        return Property.builder()
                .name(name)
                .value(value)
                .build();
    }

    @Step("Get VCS Root by ID: {vcsRootId}")
    public VcsRoot getVcsRoot(String vcsRootId) {
        Response response = client.get(Endpoint.VCS_ROOT.format(vcsRootId));
        return validator.validate(response, VcsRoot.class);
    }

    @Step("Get all VCS Roots")
    public List<VcsRoot> getAllVcsRoots() {
        Response response = client.get(Endpoint.VCS_ROOTS.getPath());
        return validator.validate(response, res -> {
            VcsRootList list = res.as(VcsRootList.class);
            return list.getVcsRoot();
        });
    }

    @Step("Get VCS Roots in project: {projectId}")
    public List<VcsRoot> getVcsRootsByProject(String projectId) {
        String path = Endpoint.VCS_ROOTS.getPath() + "?locator=project:(id:" + projectId + ")";
        Response response = client.get(path);
        return validator.validate(response, res -> {
            VcsRootList list = res.as(VcsRootList.class);
            return list.getVcsRoot();
        });
    }

    @Step("Update VCS Root: {vcsRootId}")
    public VcsRoot updateVcsRoot(String vcsRootId, VcsRootUpdateRequest request) {
        Response response = client.post(Endpoint.VCS_ROOT.format(vcsRootId), request);
        return validator.validate(response, VcsRoot.class);
    }

    @Step("Update VCS Root: {vcsRootId} with name: {newName}")
    public VcsRoot updateVcsRoot(String vcsRootId, String newName, String newUrl) {
        VcsRootUpdateRequest request = VcsRootUpdateRequest.builder()
                .name(newName)
                .url(newUrl)
                .build();
        return updateVcsRoot(vcsRootId, request);
    }

    @Step("Delete VCS Root: {vcsRootId}")
    public void deleteVcsRoot(String vcsRootId) {
        Response response = client.delete(Endpoint.VCS_ROOT.format(vcsRootId));
        validator.validateStatus(response);
        log.info("VCS Root deleted: {}", vcsRootId);
    }

    @Step("Check if VCS Root exists: {vcsRootId}")
    public boolean vcsRootExists(String vcsRootId) {
        try {
            getVcsRoot(vcsRootId);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (ApiException e) {
            if (e.getStatusCode() == HttpStatusCodes.NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    @Step("Delete VCS Root if exists: {vcsRootId}")
    public boolean deleteVcsRootIfExists(String vcsRootId) {
        if (!vcsRootExists(vcsRootId)) {
            return false;
        }
        deleteVcsRoot(vcsRootId);
        return true;
    }

    @Step("Test connection for VCS Root: {vcsRootId}")
    public void testConnection(String vcsRootId) {
        Response response = client.post(Endpoint.VCS_ROOT_TEST_CONNECTION.format(vcsRootId), null);
        validator.validateStatus(response);
        log.info("VCS Root connection tested: {}", vcsRootId);
    }

    @Step("Get VCS Root status: {vcsRootId}")
    public String getVcsRootStatus(String vcsRootId) {
        Response response = client.get(Endpoint.VCS_ROOT_STATUS.format(vcsRootId));
        return validator.validate(response, res -> res.path("status"));
    }

    @Step("Get VCS Root fields: {vcsRootId}")
    public PropertiesContainer getVcsRootFields(String vcsRootId) {
        Response response = client.get(Endpoint.VCS_ROOT_FIELDS.format(vcsRootId));
        return validator.validate(response, PropertiesContainer.class);
    }

    @Step("Find VCS Root by name: {name}")
    public VcsRoot findVcsRootByName(String name) {
        List<VcsRoot> all = getAllVcsRoots();
        if (all != null) {
            return all.stream()
                    .filter(vcsRoot -> name.equals(vcsRoot.getName()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
