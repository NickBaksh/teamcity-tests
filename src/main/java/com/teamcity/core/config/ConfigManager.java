package com.teamcity.core.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigManager {
    private static final Properties properties = new Properties();
    private static final String ENV = System.getProperty("env",
            System.getenv().getOrDefault("TEST_ENV", "local"));

    static {
        try {
            String configFile = String.format("config/%s.properties", ENV);
            log.info("Loading config from: {}", configFile);
            InputStream input = ConfigManager.class.getClassLoader()
                    .getResourceAsStream(configFile);
            if (input != null) {
                properties.load(input);
                log.info("Config loaded successfully");
            } else {
                log.warn("Config file not found: {}, using defaults", configFile);
            }
        } catch (IOException e) {
            log.error("Failed to load config", e);
        }
    }

    private static String getParam(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        String envKey = key.toUpperCase().replace(".", "_");
        value = System.getenv(envKey);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        value = properties.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        return defaultValue;
    }

    public static String getApiBaseUrl() {
        return getParam("api.base.url", "http://localhost:8111");
    }

    public static String getApiPath() {
        return getParam("api.path", "/app/rest");
    }

    public static String getAdminLogin() {
        return getParam("admin.login", "admin");
    }

    public static String getAdminPassword() {
        return getParam("admin.password", "admin123");
    }

    public static String getUserLogin() {
        return getParam("user.login", "user");
    }

    public static String getUserPassword() {
        return getParam("user.password", "user123");
    }

    public static int getApiTimeout() {
        return Integer.parseInt(getParam("api.timeout", "30000"));
    }

    public static int getRetryCount() {
        return Integer.parseInt(getParam("api.retry.count", "3"));
    }

    public static long getRetryDelay() {
        return Long.parseLong(getParam("api.retry.delay", "1000"));
    }

    public static boolean isRetryExponential() {
        return Boolean.parseBoolean(getParam("api.retry.exponential", "true"));
    }

    public static int getBuildTimeout() {
        return Integer.parseInt(getParam("build.timeout", "90"));
    }

    public static long getBuildPollInterval() {
        return Long.parseLong(getParam("build.poll.interval", "2000"));
    }

    public static String getLogLevel() {
        return getParam("log.level", "INFO");
    }

    public static boolean isAllureEnabled() {
        return Boolean.parseBoolean(getParam("allure.enabled", "true"));
    }

    public static String getAllureReportPath() {
        return getParam("allure.report.path", "target/allure-results");
    }

    public static String getUiBaseUrl() {
        return getParam("ui.base.url", getApiBaseUrl());
    }

    public static String getBrowser() {
        return getParam("browser", "chrome");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getParam("browser.headless", "false"));
    }

    public static String getSelenoidUrl() {
        return getParam("selenoid.url", null);
    }

    public static long getDefaultTimeout() {
        return Long.parseLong(getParam("default.timeout", "30000"));
    }

    public static long getDefaultPollInterval() {
        return Long.parseLong(getParam("default.poll.interval", "1000"));
    }

    public static boolean isCiMode() {
        return Boolean.parseBoolean(getParam("ci.mode", "false"));
    }

    public static int getParallelThreads() {
        return Integer.parseInt(getParam("parallel.threads", "4"));
    }

    public static String getApiToken() {
        return getParam("api.token", "");
    }

    public static String getFullUrl(String endpoint) {
        return getApiBaseUrl() + endpoint;
    }

    public static boolean isCiEnvironment() {
        return System.getenv("CI") != null || isCiMode();
    }

    public static String getEnvironment() {
        return ENV;
    }

    public static void reload() {
        properties.clear();
        try {
            String configFile = String.format("config/%s.properties", ENV);
            InputStream input = ConfigManager.class.getClassLoader()
                    .getResourceAsStream(configFile);
            if (input != null) {
                properties.load(input);
                log.info("Config reloaded successfully");
            }
        } catch (IOException e) {
            log.error("Failed to reload config", e);
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return getParam(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        return Integer.parseInt(getParam(key, String.valueOf(defaultValue)));
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getParam(key, String.valueOf(defaultValue)));
    }

    public static long getLongProperty(String key, long defaultValue) {
        return Long.parseLong(getParam(key, String.valueOf(defaultValue)));
    }

    public static String getAdminUsername() {
        return getAdminLogin();
    }

    public static String getUserUsername() {
        return getUserLogin();
    }
}