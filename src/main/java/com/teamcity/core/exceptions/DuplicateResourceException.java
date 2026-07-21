package com.teamcity.core.exceptions;

import com.teamcity.core.client.HttpStatusCodes;

public class DuplicateResourceException extends ApiException {
    public DuplicateResourceException(String resourceType, String name) {
        super(String.format("%s already exists: %s", resourceType, name),
                HttpStatusCodes.BAD_REQUEST,
                null);
    }

    public DuplicateResourceException(String message) {
        super(message, HttpStatusCodes.BAD_REQUEST, null);
    }
}
