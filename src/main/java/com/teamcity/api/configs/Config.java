package com.teamcity.api.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties");
        }
    }

    public static String getProperty(String key) {
        return INSTANCE.properties.getProperty(key);
    }

    public static String getTestEnvironment() {
        return getProperty("test.environment");
    }

    public static String getProperty(String key, String defaultValue) {
        String value = INSTANCE.properties.getProperty(key);
        return value != null ? value : defaultValue;
    }

    public static String getApiBaseUrl() {
        String url = System.getProperty("apiBaseUrl");
        if (url != null && !url.isEmpty()) {
            return url;
        }
        return getProperty("apiBaseUrl", "http://localhost:8111");
    }

    public static String getTeamCityUser() {
        return System.getProperty("teamcity.user", getProperty("teamcity.user", "admin"));
    }

    public static String getTeamCityPassword() {
        return System.getProperty("teamcity.password", getProperty("teamcity.password", "admin"));
    }

    public static String getAuthHeader() {
        String credentials = getTeamCityUser() + ":" + getTeamCityPassword();
        return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}