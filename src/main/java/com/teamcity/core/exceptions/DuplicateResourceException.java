package com.teamcity.core.exceptions;

public class DuplicateResourceException extends ApiException {
    public DuplicateResourceException(String resourceType, String name) {
        super(String.format("%s already exists: %s", resourceType, name), 400, null);
    }

    public DuplicateResourceException(String message) {
        super(message, 400, null);
    }
}