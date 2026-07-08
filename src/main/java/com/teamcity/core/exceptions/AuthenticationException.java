package com.teamcity.core.exceptions;

public class AuthenticationException extends ApiException {
    public AuthenticationException() {
        super("Authentication failed. Invalid credentials or token.", 401, "/app/rest/server");
    }

    public AuthenticationException(String message) {
        super(message, 401, "/app/rest/server");
    }
}