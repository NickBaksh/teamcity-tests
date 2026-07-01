package com.teamcity.api.requests.skelethon;

import com.teamcity.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    // Admin endpoints
    ADMIN_USERS_GET(
            "/admin/users",
            null,  // GET запрос без body
            ResponseModelName.class,
            null
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
    private final String pathParam;
}
