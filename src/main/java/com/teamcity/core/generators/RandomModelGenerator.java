package com.teamcity.core.generators;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Автоматическое создание моделей на основе {@link GeneratingRule} и простых правил по имени поля.
 */
@Slf4j
public final class RandomModelGenerator {

    private RandomModelGenerator() {
    }

    public static <T> T generate(Class<T> type) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                if (field.get(instance) != null) {
                    continue;
                }
                Object value = resolveValue(field);
                if (value != null) {
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to generate model: " + type.getSimpleName(), e);
        }
    }

    private static Object resolveValue(Field field) {
        GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
        if (rule != null) {
            String generated = RandomData.fromRegex(rule.regex());
            return convert(generated, field.getType());
        }

        Class<?> type = field.getType();
        String name = field.getName().toLowerCase();

        if (type == String.class) {
            if (name.contains("email")) {
                return RandomData.email();
            }
            if (name.contains("password")) {
                return RandomData.password();
            }
            if (name.contains("username") || name.equals("login")) {
                return RandomData.unique("user");
            }
            if (name.contains("name")) {
                return RandomData.unique(capitalize(name.replace("name", "").isEmpty() ? "Name" : name));
            }
            if (name.contains("description")) {
                return "Auto-generated " + RandomData.shortId();
            }
            if (name.contains("id") && !name.equals("id")) {
                return null;
            }
            return RandomData.unique(field.getName());
        }

        if (type == Boolean.class || type == boolean.class) {
            return Boolean.FALSE;
        }
        if (type == Long.class || type == long.class) {
            return (long) RandomData.number(1, 1_000_000);
        }
        if (type == Integer.class || type == int.class) {
            return RandomData.number(1, 1_000_000);
        }

        return null;
    }

    private static Object convert(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        }
        if (type == Long.class || type == long.class) {
            return Long.parseLong(value.replaceAll("\\D", "").isEmpty() ? "1" : value.replaceAll("\\D", ""));
        }
        if (type == Integer.class || type == int.class) {
            String digits = value.replaceAll("\\D", "");
            return Integer.parseInt(digits.isEmpty() ? "1" : digits);
        }
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "Value";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
