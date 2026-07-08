package com.teamcity.core.client;

import com.teamcity.core.models.User;
import java.util.List;

public interface UserClient {
    User createUser(User user);
    User getUser(String username);
    List<User> getAllUsers();
    void deleteUser(String username);
    boolean userExists(String username);
}