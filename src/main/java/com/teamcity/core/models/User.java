package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teamcity.core.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    @GeneratingRule(regex = "testuser_[0-9]{10,13}")
    private String username;

    private String name;
    private Long id;

    @GeneratingRule(regex = "test_[a-z0-9]{8}@example\\.com")
    private String email;

    private String lastLogin;

    @GeneratingRule(regex = "P@ssw0rd_[A-Za-z0-9]{8}!")
    private String password;

    private Boolean hasPassword;
    private String realm;
    private String href;
    private PropertiesContainer properties;
    private RoleAssignments roles;
    private Groups groups;
    private String locator;
    private UserAvatars avatars;
    private Boolean enabled2FA;
}
