package com.teamcity.core.comparison;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Загружает правила сравнения моделей из {@code model-comparison.properties}.
 * Формат: RequestClass=ResponseClass:field1=field2,field3=field4
 * Игнор: RequestClass.ignore=password,id
 */
@Slf4j
public final class ModelComparator {

    private static final ModelComparator INSTANCE = new ModelComparator();

    private final Properties properties = new Properties();

    private ModelComparator() {
        try (InputStream input = resolveStream()) {
            if (input == null) {
                throw new IllegalStateException(
                        "model-comparison.properties not found in classpath"
                );
            }
            properties.load(input);
            log.debug("Loaded model-comparison properties: {}", properties.stringPropertyNames());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load model-comparison.properties", e);
        }
    }

    public static ModelComparator getInstance() {
        return INSTANCE;
    }

    public Map<String, String> getFieldMapping(Class<?> requestClass, Class<?> responseClass) {
        String fullValue = properties.getProperty(requestClass.getSimpleName());
        if (fullValue == null || fullValue.isBlank()) {
            return Collections.emptyMap();
        }

        String[] parts = fullValue.split(":", 2);
        if (parts.length < 2) {
            return Collections.emptyMap();
        }

        String expectedResponse = parts[0].trim();
        if (!expectedResponse.equals(responseClass.getSimpleName())) {
            return Collections.emptyMap();
        }

        Map<String, String> fieldMap = new LinkedHashMap<>();
        for (String pair : parts[1].split(",")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                fieldMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        return fieldMap;
    }

    public Set<String> getIgnoredFields(Class<?> requestClass, Class<?> responseClass) {
        Set<String> ignored = new LinkedHashSet<>();
        ignored.add("password");

        String key = requestClass.getSimpleName() + ".ignore";
        String value = properties.getProperty(key);
        if (value != null && !value.isBlank()) {
            ignored.addAll(Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet()));
        }

        String responseIgnore = properties.getProperty(responseClass.getSimpleName() + ".ignore");
        if (responseIgnore != null && !responseIgnore.isBlank()) {
            ignored.addAll(Arrays.stream(responseIgnore.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet()));
        }
        return ignored;
    }

    private InputStream resolveStream() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream input = cl != null ? cl.getResourceAsStream("model-comparison.properties") : null;
        if (input == null) {
            input = ModelComparator.class.getClassLoader()
                    .getResourceAsStream("model-comparison.properties");
        }
        return input;
    }
}
