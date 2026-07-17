package com.teamcity.core.assertions;

import com.teamcity.core.comparison.ModelComparator;
import org.assertj.core.api.SoftAssertions;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public final class ModelAssertions {

    private ModelAssertions() {
    }

    public static void assertModelsMatch(Object expected, Object actual) {
        SoftAssertions softly = new SoftAssertions();
        assertModelsMatch(softly, expected, actual);
        softly.assertAll();
    }

    public static void assertModelsMatch(SoftAssertions softly, Object expected, Object actual) {
        if (expected == null || actual == null) {
            softly.assertThat(actual).as("Actual model").isEqualTo(expected);
            return;
        }

        ModelComparator comparator = ModelComparator.getInstance();
        Map<String, String> mapping = comparator.getFieldMapping(expected.getClass(), actual.getClass());
        Set<String> ignored = comparator.getIgnoredFields(expected.getClass(), actual.getClass());

        if (mapping.isEmpty()) {
            assertSameNamedFields(softly, expected, actual, ignored);
            return;
        }

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String expectedField = entry.getKey();
            String actualField = entry.getValue();
            if (ignored.contains(expectedField) || ignored.contains(actualField)) {
                continue;
            }
            Object expectedValue = readProperty(expected, expectedField);
            Object actualValue = readProperty(actual, actualField);
            softly.assertThat(actualValue)
                    .as("%s.%s should match %s.%s",
                            actual.getClass().getSimpleName(), actualField,
                            expected.getClass().getSimpleName(), expectedField)
                    .isEqualTo(expectedValue);
        }
    }

    private static void assertSameNamedFields(SoftAssertions softly, Object expected, Object actual,
                                              Set<String> ignored) {
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(expected.getClass(), Object.class)
                    .getPropertyDescriptors()) {
                String name = descriptor.getName();
                if (ignored.contains(name) || descriptor.getReadMethod() == null) {
                    continue;
                }
                Method actualGetter = findGetter(actual.getClass(), name);
                if (actualGetter == null) {
                    continue;
                }
                Object expectedValue = descriptor.getReadMethod().invoke(expected);
                if (expectedValue == null) {
                    continue;
                }
                Object actualValue = actualGetter.invoke(actual);
                softly.assertThat(actualValue)
                        .as("Field '%s' should match", name)
                        .isEqualTo(expectedValue);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compare models", e);
        }
    }

    private static Method findGetter(Class<?> type, String property) {
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(type, Object.class)
                    .getPropertyDescriptors()) {
                if (property.equals(descriptor.getName()) && descriptor.getReadMethod() != null) {
                    return descriptor.getReadMethod();
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private static Object readProperty(Object target, String property) {
        try {
            Method getter = findGetter(target.getClass(), property);
            if (getter == null) {
                throw new IllegalArgumentException(
                        "No getter for '" + property + "' on " + target.getClass().getSimpleName());
            }
            return getter.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read property: " + property, e);
        }
    }
}
