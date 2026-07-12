package com.teamcity.core.steps;

import com.teamcity.core.cleanup.CleanupRegistry;
import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.models.User;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class UserSteps {
    private final ApiClient client;
    private final ResponseValidator validator;
    private final TestDataFactory dataFactory = new TestDataFactory();

    public UserSteps(ApiClient client) {
        this.client = client;
        this.validator = new ResponseValidator();
    }

    public UserSteps(ApiClient client, ResponseValidator validator) {
        this.client = client;
        this.validator = validator;
    }

    @Step("Create user: {user.username}")
    public User createUser(User user) {
        Response response = client.post(Endpoint.USERS.getPath(), user);

        User createdUser = validator.validate(response, User.class);

        user.setId(createdUser.getId());
        user.setHref(createdUser.getHref());
        user.setName(createdUser.getName());
        user.setEmail(createdUser.getEmail());
        CleanupRegistry.get().register(() -> {
            try {
                deleteUser(createdUser.getUsername());
            } catch (Exception ignored) {
            }
        });

        return user;
    }
//    @Step("Create user: {user.username}")
//    public User createUser(User user) {
//        Response response = client.post(Endpoint.USERS.getPath(), user);
//        return validator.validate(response, User.class);
//    }

    @Step("Get user: {username}")
    public User getUser(String username) {
        Response response = client.get(Endpoint.USER.format(username));
        return validator.validate(response, User.class);
    }

    @Step("Get all users")
    public List<User> getAllUsers() {
        Response response = client.get(Endpoint.USERS.getPath());
        return validator.validate(response, res -> res.jsonPath().getList("user", User.class));
    }

    @Step("Delete user: {username}")
    public void deleteUser(String username) {
        Response response = client.delete(Endpoint.USER.format(username));
        validator.validateStatus(response);
        log.info("User deleted: {}", username);
    }

    public User createRandomUser() {
        return createUser(dataFactory.createRandomUser());
    }
}