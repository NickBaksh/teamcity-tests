package com.teamcity.core.client;

import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.AuthenticationException;
import com.teamcity.core.exceptions.DuplicateResourceException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.exceptions.ValidationException;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public class ResponseValidator {

    private static final Set<Integer> SUCCESS_CODES = Set.of(200, 201, 202, 204);
    private static final Set<Integer> CLIENT_ERROR_CODES = Set.of(400, 401, 403, 404, 405, 406, 409, 422);

    private static final Map<Integer, Function<String, RuntimeException>> ERROR_HANDLERS = Map.of(
        401, message -> new AuthenticationException("Authentication failed: " + message, 401),
        403, message -> new AuthenticationException("Access denied: " + message, 403),
        404, message -> new ResourceNotFoundException("Resource not found: " + message),
        406, message -> new ValidationException("Not acceptable: " + message),
        409, message -> new DuplicateResourceException("Conflict: " + message)
        );

    public void validateStatus(Response response) {
        int statusCode = response.statusCode();
        String requestId = response.getHeader("X-Request-ID");
        String endpoint = requestId != null ? requestId : "unknown";

        if (SUCCESS_CODES.contains(statusCode)) {
            log.debug("Request successful: {} - {}", endpoint, statusCode);
            return;
        }

        String errorMessage = extractErrorMessage(response);
        log.error("Request failed: {} - {} | {}", endpoint, statusCode, errorMessage);
        handleError(statusCode, errorMessage, response);
    }

    public <T> T validate(Response response, ResponseHandler<T> handler) {
        validateStatus(response);
        try {
            T result = handler.handle(response);
            log.debug("Response parsed successfully");
            return result;
        } catch (Exception e) {
            throw new ApiException(
                    "Failed to parse response: " + e.getMessage(),
                    e,
                    response.statusCode(),
                    response.getHeader("X-Request-ID")
            );
        }
    }

    public <T> T validate(Response response, Class<T> responseType) {
        return validate(response, res -> res.as(responseType));
    }

    public void validateStatusCode(Response response, int expectedStatusCode) {
        int actual = response.statusCode();
        if (actual != expectedStatusCode) {
            throw new ApiException(
                    String.format("Expected status %d but got %d: %s",
                            expectedStatusCode, actual, extractErrorMessage(response)),
                    actual,
                    response.getHeader("X-Request-ID")
            );
        }
    }

    public void validateStatusInRange(Response response, int start, int end) {
        int actual = response.statusCode();
        if (actual < start || actual > end) {
            throw new ApiException(
                    String.format("Expected status in [%d-%d] but got %d: %s",
                            start, end, actual, extractErrorMessage(response)),
                    actual,
                    response.getHeader("X-Request-ID")
            );
        }
    }

    public void validateStatusIn(Response response, Set<Integer> expected) {
        int actual = response.statusCode();
        if (!expected.contains(actual)) {
            throw new ApiException(
                    String.format("Expected status in %s but got %d: %s",
                            expected, actual, extractErrorMessage(response)),
                    actual,
                    response.getHeader("X-Request-ID")
            );
        }
    }

    public void validateNegativeStatus(Response response) {
        int statusCode = response.statusCode();
        if (statusCode >= 400) {
            log.debug("Negative test successful: {}", statusCode);
            return;
        }
        throw new ApiException(
                String.format("Expected error status but got %d", statusCode),
                statusCode,
                response.getHeader("X-Request-ID")
        );
    }

    public boolean isSuccess(int statusCode) {
        return SUCCESS_CODES.contains(statusCode);
    }

    public boolean isClientError(int statusCode) {
        return CLIENT_ERROR_CODES.contains(statusCode);
    }

    public boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }

    // ===== Приватные методы =====

    private void handleError(int statusCode, String errorMessage, Response response) {
        if (statusCode == 400) {
            handleBadRequest(errorMessage, response);
            return;
        }

        Function<String, RuntimeException> handler = ERROR_HANDLERS.get(statusCode);
        if (handler != null) {
            throw handler.apply(errorMessage);
        }

        throw new ApiException(
                String.format("Request failed with status %d: %s", statusCode, errorMessage),
                statusCode,
                response.getHeader("X-Request-ID")
        );
    }

    private void handleBadRequest(String errorMessage, Response response) {
        String lower = errorMessage.toLowerCase();
        String requestId = response.getHeader("X-Request-ID");

        if (lower.contains("already exists") || lower.contains("duplicate") || lower.contains("already in use")) {
            throw new DuplicateResourceException(errorMessage);
        }

        if (lower.contains("must not be empty") || lower.contains("cannot be empty") ||
                lower.contains("invalid") || lower.contains("should be") || lower.contains("required")) {
            throw new ValidationException("Validation failed: " + errorMessage, requestId);
        }

        if (lower.contains("name")) {
            throw new ValidationException("Invalid name: " + errorMessage, requestId);
        }

        if (lower.contains("project") || lower.contains("parent")) {
            throw new ValidationException("Project error: " + errorMessage, requestId);
        }

        throw new ValidationException("Bad request: " + errorMessage, requestId);
    }

    private String extractErrorMessage(Response response) {
        try {
            String body = response.getBody().asString();
            if (body == null || body.isEmpty()) {
                return response.statusLine();
            }

            String jsonMessage = extractFromJson(body);
            if (jsonMessage != null) {
                return jsonMessage;
            }

            if (response.contentType() != null && response.contentType().contains("xml")) {
                String xmlMessage = response.xmlPath().getString("error.message");
                if (xmlMessage != null && !xmlMessage.isEmpty()) {
                    return xmlMessage;
                }
            }

            return body;
        } catch (Exception e) {
            log.debug("Failed to parse error message: {}", e.getMessage());
            return response.getBody().asString();
        }
    }

    private String extractFromJson(String body) {
        try {
            String message = extractJsonPath(body, "errors[0].message");
            if (message != null && !message.isEmpty()) {
                return message;
            }

            String additionalMessage = extractJsonPath(body, "errors[0].additionalMessage");
            if (additionalMessage != null && !additionalMessage.isEmpty()) {
                String[] parts = additionalMessage.split(": ");
                return parts.length > 1 ? parts[parts.length - 1] : additionalMessage;
            }

            String rootMessage = extractJsonPath(body, "message");
            if (rootMessage != null && !rootMessage.isEmpty()) {
                return rootMessage;
            }

            return null;
        } catch (Exception e) {
            log.debug("Failed to extract message from JSON: {}", e.getMessage());
            return null;
        }
    }

    private String extractJsonPath(String body, String path) {
        try {
            io.restassured.path.json.JsonPath jsonPath = new io.restassured.path.json.JsonPath(body);
            Object value = jsonPath.get(path);
            return value != null ? String.valueOf(value) : null;
        } catch (Exception e) {
            return null;
        }
    }
}