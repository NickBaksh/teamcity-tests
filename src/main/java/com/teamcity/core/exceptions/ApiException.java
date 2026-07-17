package com.teamcity.core.exceptions;

import com.teamcity.core.client.HttpStatusCodes;
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

    public boolean isNotFound() {
        return statusCode == HttpStatusCodes.NOT_FOUND;
    }

    public boolean isBadRequest() {
        return statusCode == HttpStatusCodes.BAD_REQUEST;
    }

    public boolean isUnauthorized() {
        return statusCode == HttpStatusCodes.UNAUTHORIZED;
    }

    public boolean isForbidden() {
        return statusCode == HttpStatusCodes.FORBIDDEN;
    }

    public boolean isConflict() {
        return statusCode == HttpStatusCodes.CONFLICT;
    }

    public boolean isServerError() {
        return statusCode >= HttpStatusCodes.SERVER_ERROR_MIN && statusCode < HttpStatusCodes.SERVER_ERROR_MAX;
    }

    public boolean isClientError() {
        return statusCode >= HttpStatusCodes.CLIENT_ERROR_MIN && statusCode < HttpStatusCodes.CLIENT_ERROR_MAX;
    }

    public boolean isRetryable() {
        return statusCode == HttpStatusCodes.REQUEST_TIMEOUT
                || statusCode == HttpStatusCodes.TOO_MANY_REQUESTS
                || isServerError();
    }

    public boolean isSuccess() {
        return statusCode >= HttpStatusCodes.SUCCESS_MIN && statusCode < HttpStatusCodes.SUCCESS_MAX;
    }
}
