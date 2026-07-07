package com.teamcity.core.utils;

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
}