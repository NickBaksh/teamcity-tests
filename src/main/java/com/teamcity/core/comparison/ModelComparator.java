package com.teamcity.core.comparison;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ModelComparator {
    private static final boolean DEBUG = false;  // переключить на true для отладки

    private final Properties properties = new Properties();
    private static ModelComparator instance;

    private ModelComparator() {
        InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("model-comparison.properties");

        if (input == null) {
            input = ModelComparator.class.getClassLoader()
                    .getResourceAsStream("model-comparison.properties");
        }

        if (input == null) {
            input = ClassLoader.getSystemResourceAsStream("model-comparison.properties");
        }

        if (input == null) {
            throw new RuntimeException(
                    "model-comparison.properties not found in classpath! " +
                            "Put file in src/main/resources/ or src/test/resources/"
            );
        }

        try {
            properties.load(input);
            debug("Loaded properties: " + properties);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load model-comparison.properties", e);
        }
    }

    public static ModelComparator getInstance() {
        if (instance == null) {
            instance = new ModelComparator();
        }
        return instance;
    }

    /**
     * Возвращает маппинг полей: requestField → responseField.
     * Формат в properties: RequestClass=ResponseClass:field1=field2,field3=field4
     */
    public Map<String, String> getFieldMapping(Class<?> requestClass, Class<?> responseClass) {
        String fullValue = properties.getProperty(requestClass.getSimpleName());
        debug("Looking for: " + requestClass.getSimpleName() + " → found: " + fullValue);

        if (fullValue == null) {
            return Collections.emptyMap();
        }

        String[] parts = fullValue.split(":", 2);
        if (parts.length < 2) {
            return Collections.emptyMap();
        }

        String expectedResponse = parts[0].trim();
        if (!expectedResponse.equals(responseClass.getSimpleName())) {
            debug("Response mismatch: expected " + expectedResponse + " but got " + responseClass.getSimpleName());
            return Collections.emptyMap();
        }

        Map<String, String> fieldMap = new LinkedHashMap<>();
        for (String pair : parts[1].split(",")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                fieldMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        debug("Field mapping: " + fieldMap);
        return fieldMap;
    }

    /**
     * Поля, которые игнорируются при сравнении (например, пароль).
     */
    public Set<String> getIgnoredFields(Class<?> requestClass, Class<?> responseClass) {
        return Set.of("password");
    }

    private static void debug(String message) {
        if (DEBUG) {
            System.out.println("[ModelComparator] " + message);
        }
    }
}