package com.teamcity.core.exceptions;

import com.teamcity.core.client.HttpStatusCodes;

public class AuthenticationException extends ApiException {
    public AuthenticationException() {
        super("Authentication failed. Invalid credentials or token.",
                HttpStatusCodes.UNAUTHORIZED,
                "/app/rest/server");
    }

    public AuthenticationException(String message) {
        super(message, HttpStatusCodes.UNAUTHORIZED, "/app/rest/server");
    }

    public AuthenticationException(String message, int statusCode) {
        super(message, statusCode, "/app/rest/server");
    }
}
