package com.teamcity.core.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.models.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class UserSteps {
    private final RestClient client;

    public UserSteps(RestClient client) {
        this.client = client;
    }

    @Step("Create user: {user.username}")
    public User createUser(User user) {
        Response response = client.post("/app/rest/users", user);
        assertEquals(200, response.statusCode(), "Failed to create user: " + response.getBody().asString());

        User created = response.as(User.class);
        assertNotNull(created.getUsername(), "Username is null");
        assertEquals(user.getUsername(), created.getUsername(), "Username mismatch");

        log.info("User created: {}", created.getUsername());
        return created;
    }

    @Step("Get user: {username}")
    public User getUser(String username) {
        Response response = client.get("/app/rest/users/{userLocator}", username);
        assertEquals(200, response.statusCode(), "Failed to get user: " + response.getBody().asString());

        return response.as(User.class);
    }

    @Step("Get all users")
    public List<User> getAllUsers() {
        Response response = client.get("/app/rest/users");
        assertEquals(200, response.statusCode(), "Failed to get all users");

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody().asString());
            JsonNode usersNode = root.get("user");
            if (usersNode == null || !usersNode.isArray()) {
                return new ArrayList<>();
            }
            return mapper.convertValue(usersNode, new TypeReference<List<User>>() {});
        } catch (Exception e) {
            log.error("Failed to parse users list", e);
            return new ArrayList<>();
        }
    }

    @Step("Delete user: {username}")
    public void deleteUser(String username) {
        Response response = client.delete("/app/rest/users/{userLocator}", username);
        assertEquals(204, response.statusCode(), "Failed to delete user: " + response.getBody().asString());
        log.info("User deleted: {}", username);
    }
}