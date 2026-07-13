package com.teamcity.common.extensions;

import com.teamcity.api.configs.Config;
import com.teamcity.common.annotations.AdminSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Base64;

/**
 * Extension for handling admin session setup before test execution.
 * TeamCity uses Basic Authentication, not OAuth tokens.
 */
public class AdminSessionExtension implements BeforeEachCallback {

    private static final ThreadLocal<String> adminAuthHeader = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isAdminSessionActive = ThreadLocal.withInitial(() -> false);

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        Method testMethod = extensionContext.getRequiredTestMethod();
        AdminSession annotation = testMethod.getAnnotation(AdminSession.class);

        if (annotation != null) {
            ensureAdminSession();
            System.out.println("🔐 Admin session activated for test: " + testMethod.getName());
        }
    }

    /**
     * Ensures that admin session is active with Basic Auth.
     */
    private synchronized void ensureAdminSession() {
        if (isAdminSessionActive.get()) {
            return;
        }

        try {
            String adminUsername = Config.getProperty("admin.username", "admin");
            String adminPassword = Config.getProperty("admin.password", "admin");

            // Create Basic Auth header
            String credentials = adminUsername + ":" + adminPassword;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            String authHeader = "Basic " + encodedCredentials;

            adminAuthHeader.set(authHeader);
            isAdminSessionActive.set(true);

            System.out.println("✅ Admin session established with Basic Auth for thread: " +
                    Thread.currentThread().getName());

        } catch (Exception e) {
            throw new RuntimeException("Failed to establish admin session: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the admin Basic Auth header for current thread.
     */
    public static String getAdminAuthHeader() {
        if (!isAdminSessionActive.get()) {
            // Fallback: create Basic Auth header from config
            String adminUsername = Config.getProperty("admin.username", "admin");
            String adminPassword = Config.getProperty("admin.password", "admin");
            String credentials = adminUsername + ":" + adminPassword;
            return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        }
        return adminAuthHeader.get();
    }

    /**
     * Cleans up admin session for current thread.
     */
    public static void cleanupAdminSession() {
        adminAuthHeader.remove();
        isAdminSessionActive.remove();
    }
}