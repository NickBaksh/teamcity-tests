package com.teamcity.api.models.comparison;

import org.assertj.core.api.AbstractAssert;

import java.lang.reflect.Field;
import java.util.*;

public class ModelAssertions extends AbstractAssert<ModelAssertions, Object> {

    private final Object source;
    private final Object target;
    private final ModelComparator comparator;

    private ModelAssertions(Object source, Object target) {
        super(target, ModelAssertions.class);
        this.source = source;
        this.target = target;
        this.comparator = ModelComparator.getInstance();
    }

    /**
     * Статический метод для сравнения любых моделей
     */
    public static ModelAssertions assertThatModels(Object source, Object target) {
        return new ModelAssertions(source, target);
    }

    /**
     * Сравнивает все поля согласно маппингу в properties
     */
    public ModelAssertions match() {
        isNotNull();

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        Map<String, String> mapping = comparator.getFieldMapping(sourceClass, targetClass);

        if (mapping.isEmpty()) {
            failWithMessage(
                    "No mapping found for %s → %s in model-comparison.properties",
                    sourceClass.getSimpleName(),
                    targetClass.getSimpleName()
            );
            return this;
        }

        Set<String> ignoredFields = comparator.getIgnoredFields(sourceClass, targetClass);

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String sourceField = entry.getKey();
            String targetPath = entry.getValue();

            if (ignoredFields.contains(sourceField)) {
                debug("Ignoring field: " + sourceField);
                continue;
            }

            Object sourceValue = getFieldValue(source, sourceField);
            Object targetValue = getNestedFieldValue(target, targetPath);

            // 🆕 Используем улучшенное сравнение
            if (!comparator.compareValues(sourceValue, targetValue)) {
                failWithActualExpectedAndMessage(
                        targetValue,
                        sourceValue,
                        "\nField mismatch: %s.%s → %s.%s\n" +
                                "Expected: %s\n" +
                                "Actual:   %s",
                        sourceClass.getSimpleName(), sourceField,
                        targetClass.getSimpleName(), targetPath,
                        sourceValue, targetValue
                );
            }
        }

        return this;
    }

    /**
     * Сравнить только указанные поля
     */
    public ModelAssertions matchOnly(String... fields) {
        isNotNull();

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        Map<String, String> mapping = comparator.getFieldMapping(sourceClass, targetClass);
        Set<String> fieldSet = new HashSet<>(Arrays.asList(fields));

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String sourceField = entry.getKey();
            if (!fieldSet.contains(sourceField)) {
                continue;
            }

            Object sourceValue = getFieldValue(source, sourceField);
            Object targetValue = getNestedFieldValue(target, entry.getValue());

            if (!comparator.compareValues(sourceValue, targetValue)) {
                failWithActualExpectedAndMessage(
                        targetValue,
                        sourceValue,
                        "\nField mismatch: %s → %s",
                        sourceField, entry.getValue()
                );
            }
        }

        return this;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field == null) {
                debug("Field '" + fieldName + "' not found in " + obj.getClass().getSimpleName());
                return null;
            }
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field: " + fieldName, e);
        }
    }

    private Object getNestedFieldValue(Object obj, String path) {
        String[] parts = path.split("\\.");
        Object current = obj;
        for (String part : parts) {
            current = getFieldValue(current, part);
        }
        return current;
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private void debug(String message) {
        if (ModelComparator.DEBUG) {
            System.out.println("[ModelAssertions] " + message);
        }
    }
}