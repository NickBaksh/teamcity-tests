package com.teamcity.core.client;

import com.teamcity.core.exceptions.ApiException;
import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
    private final boolean isNegativeTest;
    private final HeaderConfig defaultHeaders;

    private RestClient(Builder builder) {
        RestAssured.baseURI = builder.baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        this.maxRetries = builder.isNegativeTest ? 1 : builder.retryCount;
        this.retryDelay = builder.retryDelay;
        this.isNegativeTest = builder.isNegativeTest;
        this.responseValidator = new ResponseValidator();

        HeaderConfig defaultHeaders = HeaderConfig.defaultHeaders();
        if (builder.headers != null && !builder.headers.isEmpty()) {
            defaultHeaders.withCustomHeaders(builder.headers);
        }
        this.defaultHeaders = defaultHeaders;

        RequestSpecification spec = RestAssured.given()
                .relaxedHTTPSValidation()
                .filters(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter(),
                        new AllureRestAssured()
                );

        spec.headers(this.defaultHeaders.build());

        if (builder.token != null && !builder.token.isEmpty()) {
            log.info("Using Bearer Token authentication");
            spec.header("Authorization", "Bearer " + builder.token);
        } else if (builder.username != null && builder.password != null) {
            log.info("Using Basic authentication");
            spec.auth().basic(builder.username, builder.password);
        } else {
            log.info("No authentication configured");
        }

        this.requestSpec = spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ===== БАЗОВЫЕ МЕТОДЫ =====

    @Override
    public Response get(String endpoint, Object... pathParams) {
        return executeWithRetry(() -> requestSpec.get(endpoint, pathParams));
    }

    @Override
    public Response post(String endpoint, Object body) {
        return executeWithRetry(() -> requestSpec.body(body).post(endpoint));
    }

    @Override
    public Response post(String endpoint, Object body, Object... pathParams) {
        return executeWithRetry(() -> requestSpec.body(body).post(endpoint, pathParams));
    }

    @Override
    public Response put(String endpoint, Object body, Object... pathParams) {
        return executeWithRetry(() -> requestSpec.body(body).put(endpoint, pathParams));
    }

    @Override
    public Response delete(String endpoint, Object... pathParams) {
        return executeWithRetry(() -> requestSpec.delete(endpoint, pathParams));
    }

    // ===== МЕТОДЫ С КАСТОМНЫМИ ЗАГОЛОВКАМИ (НОВЫЕ) =====

    @Override
    public Response get(String endpoint, Map<String, String> headers, Object... pathParams) {
        return executeWithRetry(() -> {
            RequestSpecification spec = requestSpec.given();
            if (headers != null && !headers.isEmpty()) {
                spec.headers(headers);
            }
            return spec.get(endpoint, pathParams);
        });
    }

    @Override
    public Response post(String endpoint, Object body, Map<String, String> headers, Object... pathParams) {
        return executeWithRetry(() -> {
            RequestSpecification spec = requestSpec.given();
            if (headers != null && !headers.isEmpty()) {
                spec.headers(headers);
            }
            return spec.body(body).post(endpoint, pathParams);
        });
    }

    @Override
    public Response put(String endpoint, Object body, Map<String, String> headers, Object... pathParams) {
        return executeWithRetry(() -> {
            RequestSpecification spec = requestSpec.given();
            if (headers != null && !headers.isEmpty()) {
                spec.headers(headers);
            }
            return spec.body(body).put(endpoint, pathParams);
        });
    }

    @Override
    public Response delete(String endpoint, Map<String, String> headers, Object... pathParams) {
        return executeWithRetry(() -> {
            RequestSpecification spec = requestSpec.given();
            if (headers != null && !headers.isEmpty()) {
                spec.headers(headers);
            }
            return spec.delete(endpoint, pathParams);
        });
    }

    // ===== МЕТОДЫ С RequestType =====

    /**
     * GET запрос с явным указанием типа Accept
     */
    public Response get(String endpoint, RequestType requestType, Object... pathParams) {
        return get(endpoint, requestType.toHeaderConfig().build(), pathParams);
    }

    /**
     * POST запрос с кастомными заголовками через RequestType
     */
    public Response post(String endpoint, Object body, RequestType requestType, Object... pathParams) {
        return post(endpoint, body, requestType.toHeaderConfig().build(), pathParams);
    }

    /**
     * PUT запрос с кастомными заголовками через RequestType
     */
    public Response put(String endpoint, Object body, RequestType requestType, Object... pathParams) {
        return put(endpoint, body, requestType.toHeaderConfig().build(), pathParams);
    }

    // ===== СПЕЦИАЛЬНЫЕ МЕТОДЫ =====

    /**
     * GET запрос с текстовым ответом
     */
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

    // ===== EXECUTE МЕТОДЫ =====

    @Override
    public <T> T execute(ApiRequest request, Class<T> responseType) {
        return execute(request, response -> response.as(responseType));
    }

    @Override
    public <T> T execute(ApiRequest request, ResponseHandler<T> handler) {
        return (T) executeWithRetry(() -> {
            Response response = sendRequest(request);
            return (Response) responseValidator.validate(response, handler);
        });
    }

    // ===== ПРИВАТНЫЕ МЕТОДЫ =====

    private Response executeWithRetry(Supplier<Response> request) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    log.debug("Retry attempt {}/{}", attempt, maxRetries);
                }

                long startTime = System.nanoTime();
                Response response = request.get();

                if (isNegativeTest && response.statusCode() == 401) {
                    return response;
                }

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
                    long delay = calculateDelay(attempt);
                    sleep(delay);
                }
            }
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

    private Response sendRequest(ApiRequest request) {
        RequestSpecification spec = requestSpec.given();

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            spec.headers(request.getHeaders());
        }

        if (request.getContentType() != null) {
            spec.contentType(request.getContentType().getValue());
        }

        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            spec.queryParams(request.getQueryParams());
        }

        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            spec.pathParams(request.getPathParams());
        }

        if (request.getBody() != null) {
            spec.body(request.getBody());
        }

        return spec.request(request.getMethod().name(), request.getEndpoint());
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

    // ===== BUILDER =====

    public static class Builder {
        private String baseUrl;
        private String username;
        private String password;
        private String token;
        private Map<String, String> headers;
        private int retryCount = DEFAULT_RETRY_COUNT;
        private long retryDelay = DEFAULT_RETRY_DELAY_MS;
        private boolean isNegativeTest = false;

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
            log.debug("Retry count set to {}", retryCount);
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
            log.debug("Retry delay set to {} ms", retryDelayMs);
            return this;
        }

        public Builder retryDelaySeconds(int retryDelaySec) {
            if (retryDelaySec < 1 || retryDelaySec > 5) {
                throw new IllegalArgumentException(
                        String.format("Retry delay must be between 1 and 5 seconds, got: %d", retryDelaySec)
                );
            }
            return retryDelay(retryDelaySec * 1000L);
        }

        public Builder withRetry(int retryCount) {
            return retryCount(retryCount);
        }

        public Builder forNegativeTest() {
            this.isNegativeTest = true;
            this.retryCount = 1;
            log.debug("Negative test mode enabled, retry count set to 1");
            return this;
        }

        public RestClient build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL must be set");
            }
            log.info("Building RestClient with baseUrl: {}, retryCount: {}, retryDelay: {}ms",
                    baseUrl, retryCount, retryDelay);
            return new RestClient(this);
        }
    }
}