package com.teamcity.core.steps;

import com.teamcity.core.client.ApiClient;
import com.teamcity.core.client.ResponseValidator;
import com.teamcity.core.endpoints.Endpoint;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class UserSteps extends BaseSteps {

    public UserSteps(ApiClient client) {
        super(client);
    }

    public UserSteps(ApiClient client, ResponseValidator validator) {
        super(client, validator);
    }

    @Step("Create user: {user.username}")
    public User createUser(User user) {
        Response response = client.post(Endpoint.USERS.getPath(), user);
        return validator.validate(response, User.class);
    }

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

    @Step("Delete user if exists: {username}")
    public boolean deleteUserIfExists(String username) {
        if (!userExists(username)) {
            return false;
        }
        deleteUser(username);
        return true;
    }

    @Step("Check if user exists: {username}")
    public boolean userExists(String username) {
        try {
            getUser(username);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}
