package com.teamcity.api.models.dto_models.auth;

import com.teamcity.api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserResponse extends BaseModel {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Account {
        private Long id;
        private String accountNumber;
        private Double balance;
    }
}