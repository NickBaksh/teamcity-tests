package com.teamcity.api;

import com.teamcity.core.exceptions.ApiException;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

@Slf4j
public class TestListener implements TestWatcher {

    @Override
    public void testSuccessful(ExtensionContext context) {
        log.info("Test passed: {}", context.getDisplayName());
        Allure.addAttachment("Test Result", "text/plain", "PASSED");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        log.error("Test failed: {}", context.getDisplayName(), cause);

        if (cause instanceof ApiException) {
            ApiException apiEx = (ApiException) cause;
            Allure.addAttachment("API Error",
                    "text/plain",
                    String.format("Status: %d%nEndpoint: %s%nMessage: %s",
                            apiEx.getStatusCode(),
                            apiEx.getEndpoint(),
                            apiEx.getMessage()));
        }

        Allure.addAttachment("Test Result", "text/plain", "FAILED");
        Allure.addAttachment("Error", "text/plain", cause.getMessage());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        log.warn("Test aborted: {}", context.getDisplayName(), cause);
        Allure.addAttachment("Test Result", "text/plain", "ABORTED");
    }
}
