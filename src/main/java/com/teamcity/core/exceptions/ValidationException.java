package com.teamcity.core.exceptions;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message, 400, null);
    }

    public ValidationException(String message, String endpoint) {
        super(message, 400, endpoint);
    }
}