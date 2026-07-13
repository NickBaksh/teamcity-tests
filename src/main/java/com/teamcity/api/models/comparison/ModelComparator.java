package com.teamcity.api.models.comparison;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

public class ModelComparator {
    static final boolean DEBUG = true;  // включи для отладки
    private static final double MONEY_DELTA = 0.01;

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
     * Возвращает маппинг полей: sourceField → targetField.
     * Формат в properties: SourceClass → TargetClass: field1=field2,field3=field4
     */
    public Map<String, String> getFieldMapping(Class<?> sourceClass, Class<?> targetClass) {
        String key = sourceClass.getSimpleName() + " → " + targetClass.getSimpleName();
        String fullValue = properties.getProperty(key);

        debug("Looking for: " + key + " → found: " + fullValue);

        if (fullValue == null) {
            // Пробуем старый формат (без →)
            fullValue = properties.getProperty(sourceClass.getSimpleName());
            debug("Trying old format: " + sourceClass.getSimpleName() + " → found: " + fullValue);
        }

        if (fullValue == null) {
            return Collections.emptyMap();
        }

        String[] parts = fullValue.split(":", 2);
        if (parts.length < 2) {
            return Collections.emptyMap();
        }

        String expectedTarget = parts[0].trim();
        if (!expectedTarget.equals(targetClass.getSimpleName())) {
            debug("Target mismatch: expected " + expectedTarget + " but got " + targetClass.getSimpleName());
            return Collections.emptyMap();
        }

        Map<String, String> fieldMap = new LinkedHashMap<>();
        for (String pair : parts[1].split(",")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                fieldMap.put(kv[0].trim(), kv[1].trim());
            } else if (kv.length == 1 && !kv[0].trim().isEmpty()) {
                // Если поля называются одинаково: field → field
                fieldMap.put(kv[0].trim(), kv[0].trim());
            }
        }
        debug("Field mapping: " + fieldMap);
        return fieldMap;
    }

    /**
     * Поля, которые игнорируются при сравнении
     */
    public Set<String> getIgnoredFields(Class<?> sourceClass, Class<?> targetClass) {
        String key = sourceClass.getSimpleName() + " → " + targetClass.getSimpleName() + ".ignore";
        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            // Дефолтные игнорируемые поля
            return Set.of("password");
        }

        return new HashSet<>(Arrays.asList(value.split("\\s*,\\s*")));
    }

    /**
     * 🆕 Сравнение значений с поддержкой разных типов
     */
    public boolean compareValues(Object sourceValue, Object targetValue) {
        if (sourceValue == null && targetValue == null) return true;
        if (sourceValue == null || targetValue == null) return false;

        // === Сравнение чисел (Double, BigDecimal, Integer, Long) ===
        if (sourceValue instanceof Number && targetValue instanceof Number) {
            double source = ((Number) sourceValue).doubleValue();
            double target = ((Number) targetValue).doubleValue();

            // Если оба числа — сравниваем с дельтой (для денег)
            return Math.abs(source - target) < MONEY_DELTA;
        }

        // === Сравнение BigDecimal ===
        if (sourceValue instanceof BigDecimal && targetValue instanceof BigDecimal) {
            return ((BigDecimal) sourceValue).compareTo((BigDecimal) targetValue) == 0;
        }

        // === Сравнение BigDecimal и Number ===
        if (sourceValue instanceof BigDecimal && targetValue instanceof Number) {
            BigDecimal source = (BigDecimal) sourceValue;
            BigDecimal target = BigDecimal.valueOf(((Number) targetValue).doubleValue())
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            return source.compareTo(target) == 0;
        }

        if (sourceValue instanceof Number && targetValue instanceof BigDecimal) {
            BigDecimal source = BigDecimal.valueOf(((Number) sourceValue).doubleValue())
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal target = (BigDecimal) targetValue;
            return source.compareTo(target) == 0;
        }

        // === Стандартное сравнение ===
        return Objects.equals(sourceValue, targetValue);
    }

    private static void debug(String message) {
        if (DEBUG) {
            System.out.println("[ModelComparator] " + message);
        }
    }
}