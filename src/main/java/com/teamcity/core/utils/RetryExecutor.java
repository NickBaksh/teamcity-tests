package com.teamcity.core.utils;

import com.teamcity.core.exceptions.ApiException;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Supplier;

@Slf4j
public class RetryExecutor {
    private final int maxRetries;
    private final long delayMs;
    private final boolean exponentialBackoff;

    // НЕ РЕТРАИМ эти статусы (они не исправятся повторным запросом)
    private static final Set<Integer> NON_RETRYABLE_STATUSES = Set.of(
            400,  // Bad Request
            401,  // Unauthorized
            403,  // Forbidden
            404,  // Not Found
            405,  // Method Not Allowed
            406,  // Not Acceptable
            422   // Unprocessable Entity
    );

    public RetryExecutor(int maxRetries, long delayMs, boolean exponentialBackoff) {
        this.maxRetries = maxRetries;
        this.delayMs = delayMs;
        this.exponentialBackoff = exponentialBackoff;
    }

    public static RetryExecutor defaultRetry() {
        return new RetryExecutor(3, 1000, true);
    }

    public static RetryExecutor noRetry() {
        return new RetryExecutor(1, 0, false);
    }

    public <T> T execute(Supplier<T> action) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    log.debug("Retry attempt {}/{}", attempt, maxRetries);
                }
                return action.get();
            } catch (ApiException e) {
                lastException = e;

                // Если статус НЕЛЬЗЯ ретраить — сразу выбрасываем
                if (NON_RETRYABLE_STATUSES.contains(e.getStatusCode())) {
                    log.debug("Non-retryable status {}: throwing immediately", e.getStatusCode());
                    throw e;
                }

                log.warn("Attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    long waitTime = calculateWaitTime(attempt);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    long waitTime = calculateWaitTime(attempt);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
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

    private long calculateWaitTime(int attempt) {
        if (exponentialBackoff) {
            return delayMs * (long) Math.pow(2, attempt - 1);
        }
        return delayMs * attempt;
    }
}