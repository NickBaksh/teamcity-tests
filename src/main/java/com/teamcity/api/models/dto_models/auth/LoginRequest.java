package com.teamcity.api.models.dto_models.auth;

import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest extends BaseModel {
    private String username;
    private String password;
}
