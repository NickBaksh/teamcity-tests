package com.teamcity.core.exceptions;

import com.teamcity.core.client.HttpStatusCodes;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message, HttpStatusCodes.BAD_REQUEST, null);
    }

    public ValidationException(String message, String endpoint) {
        super(message, HttpStatusCodes.BAD_REQUEST, endpoint);
    }
}
