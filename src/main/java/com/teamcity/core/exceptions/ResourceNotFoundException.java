package com.teamcity.core.exceptions;

import com.teamcity.core.client.HttpStatusCodes;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resourceType, String id) {
        super(String.format("%s not found: %s", resourceType, id),
                HttpStatusCodes.NOT_FOUND,
                "/" + resourceType + "/" + id);
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatusCodes.NOT_FOUND, null);
    }
}
