package com.teamcity.core.exceptions;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resourceType, String id) {
        super(String.format("%s not found: %s", resourceType, id), 404, "/" + resourceType + "/" + id);
    }

    public ResourceNotFoundException(String message) {
        super(message, 404, null);
    }
}