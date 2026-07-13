package com.teamcity.core.client;

import com.teamcity.core.config.ConfigManager;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.specs.RequestSpecs;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;

/**
 * Реализация {@link ApiClient} на RestAssured.
 * Спецификации и логирование берутся из {@link RequestSpecs}; в тестах {@code given()} не используется.
 */
@Slf4j
public class RestClient implements ApiClient {

    private static final Set<Integer> NON_RETRYABLE_STATUSES = Set.of(
            400, 401, 403, 404, 405, 406, 409, 422
    );

    private static final long MIN_RETRY_DELAY_MS = 100;
    private static final long MAX_RETRY_DELAY_MS = 5000;
    private static final long DEFAULT_RETRY_DELAY_MS = 1000;
    private static final int MIN_RETRY_COUNT = 1;
    private static final int MAX_RETRY_COUNT = 5;
    private static final int DEFAULT_RETRY_COUNT = 3;

    private final RequestSpecification requestSpec;
    private final ResponseValidator responseValidator;
    private final int maxRetries;
    private final long retryDelay;

    private RestClient(Builder builder) {
        this.maxRetries = builder.retryCount;
        this.retryDelay = builder.retryDelay;
        this.responseValidator = new ResponseValidator();

        RequestSpecification baseSpec;
        if (builder.token != null && !builder.token.isEmpty()) {
            log.info("Using Bearer Token authentication");
            baseSpec = RequestSpecs.withBearerToken(builder.token);
        } else if (builder.username != null && builder.password != null) {
            log.info("Using Basic authentication");
            baseSpec = RequestSpecs.withBasicAuth(builder.username, builder.password);
        } else {
            log.info("No authentication configured");
            baseSpec = RequestSpecs.base();
        }

        if (builder.baseUrl != null && !builder.baseUrl.equals(ConfigManager.getApiBaseUrl())) {
            baseSpec = given().spec(baseSpec).baseUri(builder.baseUrl);
        }

        if (builder.headers != null && !builder.headers.isEmpty()) {
            baseSpec = given().spec(baseSpec).headers(builder.headers);
        }

        this.requestSpec = baseSpec;
    }

    public static Builder builder() {
        return new Builder();
    }

    private RequestSpecification newRequest() {
        return given().spec(requestSpec);
    }

    @Override
    public Response get(String endpoint, Object... pathParams) {
        return executeWithRetry(() -> newRequest().get(endpoint, pathParams));
    }

    @Override
    public Response post(String endpoint, Object body) {
        return executeWithRetry(() -> newRequest().body(body).post(endpoint));
    }

    @Override
    public Response post(String endpoint, Object body, Object... pathParams) {
        return executeWithRetry(() -> newRequest().body(body).post(endpoint, pathParams));
    }

    @Override
    public Response put(String endpoint, Object body, Object... pathParams) {
        return executeWithRetry(() -> newRequest().body(body).put(endpoint, pathParams));
    }

    @Override
    public Response delete(String endpoint, Object... pathParams) {
        validatePathParams(endpoint, pathParams);
        return executeWithRetry(() -> newRequest().delete(endpoint, pathParams));
    }

    public Response delete(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Endpoint cannot be null or empty");
        }
        if (endpoint.contains("{") && endpoint.contains("}")) {
            throw new IllegalArgumentException(
                    String.format("Endpoint '%s' contains placeholders. Use delete(endpoint, params).", endpoint)
            );
        }
        return executeWithRetry(() -> newRequest().delete(endpoint));
    }

    @Override
    public Response get(String endpoint, Map<String, String> headers, Object... pathParams) {
        return executeWithRetry(() -> {
            RequestSpecification spec = newRequest();
            if (headers != null && !headers.isEmpty()) {
                spec.headers(headers);
            }
            return spec.get(endpoint, pathParams);
        });
    }

    @Override
    public Response post(String endpoint, Object body, Map<String, String> headers, Object... pathParams) {
        return executeWithRetry(() -> {
            RequestSpecification spec = newRequest();
            if (headers != null && !headers.isEmpty()) {
                spec.headers(headers);
            }
            return spec.body(body).post(endpoint, pathParams);
        });
    }

    @Override
    public Response put(String endpoint, Object body, Map<String, String> headers, Object... pathParams) {
        return executeWithRetry(() -> {
            RequestSpecification spec = newRequest();
            if (headers != null && !headers.isEmpty()) {
                spec.headers(headers);
            }
            return spec.body(body).put(endpoint, pathParams);
        });
    }

    @Override
    public Response delete(String endpoint, Map<String, String> headers, Object... pathParams) {
        return executeWithRetry(() -> {
            RequestSpecification spec = newRequest();
            if (headers != null && !headers.isEmpty()) {
                spec.headers(headers);
            }
            return spec.delete(endpoint, pathParams);
        });
    }

    @Override
    public Response get(String endpoint, RequestType requestType, Object... pathParams) {
        return get(endpoint, requestType.toHeaderConfig().build(), pathParams);
    }

    @Override
    public Response post(String endpoint, Object body, RequestType requestType, Object... pathParams) {
        return post(endpoint, body, requestType.toHeaderConfig().build(), pathParams);
    }

    @Override
    public Response put(String endpoint, Object body, RequestType requestType, Object... pathParams) {
        return put(endpoint, body, requestType.toHeaderConfig().build(), pathParams);
    }

    public Response getText(String endpoint, Object... pathParams) {
        return get(endpoint, RequestType.TEXT_ACCEPT_JSON, pathParams);
    }

    @Override
    public Response putText(String endpoint, String body, Object... pathParams) {
        return put(endpoint, body, RequestType.TEXT, pathParams);
    }

    @Override
    public Response putBoolean(String endpoint, boolean body, Object... pathParams) {
        return putText(endpoint, String.valueOf(body), pathParams);
    }

    @Override
    public <T> T execute(HttpRequest request, Class<T> responseType) {
        return execute(request, response -> response.as(responseType));
    }

    @Override
    public <T> T execute(HttpRequest request, ResponseHandler<T> handler) {
        Response response = executeWithRetry(() -> sendRequest(request));
        return responseValidator.validate(response, handler);
    }

    private Response executeWithRetry(Supplier<Response> request) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    log.debug("Retry attempt {}/{}", attempt, maxRetries);
                }

                long startTime = System.nanoTime();
                Response response = request.get();
                responseValidator.validateStatus(response);
                logRequestDetails(response, System.nanoTime() - startTime);
                return response;
            } catch (ApiException e) {
                lastException = e;
                if (NON_RETRYABLE_STATUSES.contains(e.getStatusCode())) {
                    log.debug("Non-retryable status {}: throwing immediately", e.getStatusCode());
                    throw e;
                }
                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt);
                    log.warn("Attempt {}/{} failed: {}. Retrying in {}ms",
                            attempt, maxRetries, e.getMessage(), delay);
                    sleep(delay);
                } else {
                    log.error("Attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    sleep(calculateDelay(attempt));
                }
            }
        }

        if (lastException instanceof ApiException apiException) {
            throw apiException;
        }

        throw new ApiException(
                String.format("All %d retries failed", maxRetries),
                lastException,
                0,
                null
        );
    }

    private long calculateDelay(int attempt) {
        return retryDelay * (long) Math.pow(2, attempt - 1);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted", e);
        }
    }

    private Response sendRequest(HttpRequest request) {
        RequestSpecification spec = newRequest();

        if (!request.getHeaders().isEmpty()) {
            spec.headers(request.getHeaders());
        }
        if (request.getContentType() != null) {
            spec.contentType(request.getContentType().getValue());
        }
        if (!request.getQueryParams().isEmpty()) {
            spec.queryParams(request.getQueryParams());
        }
        if (!request.getPathParams().isEmpty()) {
            spec.pathParams(request.getPathParams());
        }
        if (request.getBody() != null) {
            spec.body(request.getBody());
        }

        return spec.request(request.getMethod().name(), request.getEndpoint());
    }

    private void validatePathParams(String endpoint, Object... pathParams) {
        boolean hasPlaceholders = endpoint != null && endpoint.contains("{") && endpoint.contains("}");
        if (!hasPlaceholders && pathParams != null && pathParams.length > 0) {
            throw new IllegalArgumentException(
                    String.format("Endpoint '%s' has no placeholders but %d parameters provided",
                            endpoint, pathParams.length)
            );
        }
        if (hasPlaceholders) {
            int expected = countPlaceholders(endpoint);
            int actual = pathParams != null ? pathParams.length : 0;
            if (actual != expected) {
                throw new IllegalArgumentException(
                        String.format("Endpoint '%s' expects %d parameter(s) but got %d",
                                endpoint, expected, actual)
                );
            }
        }
    }

    private int countPlaceholders(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = endpoint.indexOf('{', index)) != -1) {
            int endIndex = endpoint.indexOf('}', index);
            if (endIndex == -1) {
                throw new IllegalArgumentException(
                        String.format("Invalid endpoint: '%s' - unclosed placeholder at %d", endpoint, index)
                );
            }
            if (endpoint.substring(index + 1, endIndex).trim().isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("Invalid endpoint: '%s' - empty placeholder at %d", endpoint, index)
                );
            }
            count++;
            index = endIndex + 1;
        }
        return count;
    }

    private void logRequestDetails(Response response, long durationNanos) {
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        String requestId = response.getHeader("X-Request-ID");
        log.debug("Request completed in {}ms, X-Request-ID: {}", durationMs, requestId);
        Allure.addAttachment("Response Time", "text/plain", durationMs + " ms");
        if (requestId != null) {
            Allure.addAttachment("Request ID", "text/plain", requestId);
        }
    }

    public static class Builder {
        private String baseUrl;
        private String username;
        private String password;
        private String token;
        private Map<String, String> headers;
        private int retryCount = DEFAULT_RETRY_COUNT;
        private long retryDelay = DEFAULT_RETRY_DELAY_MS;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder basicAuth(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder bearerToken(String token) {
            this.token = token;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers != null ? new HashMap<>(headers) : null;
            return this;
        }

        public Builder header(String key, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(key, value);
            return this;
        }

        public Builder retryCount(int retryCount) {
            if (retryCount < MIN_RETRY_COUNT || retryCount > MAX_RETRY_COUNT) {
                throw new IllegalArgumentException(
                        String.format("Retry count must be between %d and %d, got: %d",
                                MIN_RETRY_COUNT, MAX_RETRY_COUNT, retryCount)
                );
            }
            this.retryCount = retryCount;
            return this;
        }

        public Builder retryDelay(long retryDelayMs) {
            if (retryDelayMs < MIN_RETRY_DELAY_MS || retryDelayMs > MAX_RETRY_DELAY_MS) {
                throw new IllegalArgumentException(
                        String.format("Retry delay must be between %d and %d ms, got: %d",
                                MIN_RETRY_DELAY_MS, MAX_RETRY_DELAY_MS, retryDelayMs)
                );
            }
            this.retryDelay = retryDelayMs;
            return this;
        }

        public Builder withRetry(int retryCount) {
            return retryCount(retryCount);
        }

        public RestClient build() {
            if (baseUrl == null) {
                this.baseUrl = ConfigManager.getApiBaseUrl();
            }
            log.info("Building RestClient with baseUrl: {}, retryCount: {}, retryDelay: {}ms",
                    baseUrl, retryCount, retryDelay);
            return new RestClient(this);
        }
    }
}
