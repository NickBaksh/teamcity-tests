package com.teamcity.api.models.dto_models.auth;

import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse extends BaseModel {
    private String token;
    private String accessToken;
    private String username;
    private String role;
    private String sessionId;
}
