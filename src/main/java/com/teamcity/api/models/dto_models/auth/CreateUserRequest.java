package com.teamcity.api.models.dto_models.auth;

import com.teamcity.api.generators.GeneratingRule;
import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {

    @GeneratingRule(regex = "^[A-Za-z0-9]{3,15}$")
    private String username;

    @GeneratingRule(regex = "^[a-zA-Z0-9!@#$%^&*]{8,20}$")
    private String password;

    private String role;

    private String name;

    // Методы-помощники
    public static CreateUserRequest getAdmin() {
        return CreateUserRequest.builder()
                .username("admin")
                .password("admin")
                .role("ADMIN")
                .build();
    }

    public static CreateUserRequest getUser() {
        return CreateUserRequest.builder()
                .username("user")
                .password("user")
                .role("USER")
                .build();
    }
}