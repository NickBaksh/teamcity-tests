package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String username;
    private String name;
    private Long id;
    private String email;
    private String lastLogin;
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