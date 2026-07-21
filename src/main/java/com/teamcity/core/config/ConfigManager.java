package com.teamcity.core.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigManager {
    private static final Properties properties = new Properties();
    private static final String ENV = System.getProperty("env", "local");

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
                throw new RuntimeException("Config file not found: " + configFile);
            }
        } catch (IOException e) {
            log.error("Failed to load config", e);
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static String getApiBaseUrl() {
        return properties.getProperty("api.base.url", "http://localhost:8111");
    }

    public static String getAdminLogin() {
        return properties.getProperty("admin.login", "admin");
    }

    public static String getAdminPassword() {
        return properties.getProperty("admin.password", "admin");
    }

    public static String getUserLogin() {
        return properties.getProperty("user.login", "user");
    }

    public static String getUserPassword() {
        return properties.getProperty("user.password", "user123");
    }

    public static int getApiTimeout() {
        return Integer.parseInt(properties.getProperty("api.timeout", "30000"));
    }

    public static String getUiBaseUrl() {
        return properties.getProperty("ui.base.url", getApiBaseUrl());
    }

    public static String getBrowser() {
        return properties.getProperty("browser", "chrome");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(properties.getProperty("browser.headless", "false"));
    }

    public static String getApiToken() {
        return properties.getProperty("api.token", "");
    }

    public static int getRetryCount() {
        return Integer.parseInt(properties.getProperty("api.retry.count", "3"));
    }

    public static long getRetryDelay() {
        return Long.parseLong(properties.getProperty("api.retry.delay", "1000"));
    }

    public static boolean isRetryExponential() {
        return Boolean.parseBoolean(properties.getProperty("api.retry.exponential", "true"));
    }

    public static int getBuildTimeout() {
        return Integer.parseInt(properties.getProperty("build.timeout", "300"));
    }

    public static long getBuildPollInterval() {
        return Long.parseLong(properties.getProperty("build.poll.interval", "2000"));
    }

    public static String getLogLevel() {
        return properties.getProperty("log.level", "INFO");
    }

    public static boolean isAllureEnabled() {
        return Boolean.parseBoolean(properties.getProperty("allure.enabled", "true"));
    }

    public static String getAllureReportPath() {
        return properties.getProperty("allure.report.path", "target/allure-results");
    }

    public static long getDefaultTimeout() {
        return Long.parseLong(properties.getProperty("default.timeout", "30000"));
    }

    public static long getDefaultPollInterval() {
        return Long.parseLong(properties.getProperty("default.poll.interval", "1000"));
    }

    public static boolean isCiMode() {
        return Boolean.parseBoolean(properties.getProperty("ci.mode", "false"));
    }

    public static int getParallelThreads() {
        return Integer.parseInt(properties.getProperty("parallel.threads", "4"));
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
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    public static long getLongProperty(String key, long defaultValue) {
        try {
            return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getAdminUsername() {
        return getAdminLogin();
    }

    public static String getUserUsername() {
        return getUserLogin();
    }
}
