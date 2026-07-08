package com.teamcity.core.exceptions;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String endpoint;
    private final String requestId;

    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.endpoint = null;
        this.requestId = null;
    }

    public ApiException(String message, int statusCode, String endpoint) {
        super(message);
        this.statusCode = statusCode;
        this.endpoint = endpoint;
        this.requestId = null;
    }

    public ApiException(String message, Throwable cause, int statusCode, String endpoint) {
        super(message, cause);
        this.statusCode = statusCode;
        this.endpoint = endpoint;
        this.requestId = null;
    }

    public ApiException(String message, int statusCode, String endpoint, String requestId) {
        super(message);
        this.statusCode = statusCode;
        this.endpoint = endpoint;
        this.requestId = requestId;
    }

    // ===== Методы для проверки статусов =====

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isBadRequest() {
        return statusCode == 400;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isConflict() {
        return statusCode == 409;
    }

    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isRetryable() {
        return statusCode == 408 || statusCode == 429 || isServerError();
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}