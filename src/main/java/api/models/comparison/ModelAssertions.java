package api.models.comparison;

import api.models.BaseModel;
import org.assertj.core.api.AbstractAssert;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ModelAssertions extends AbstractAssert<ModelAssertions, BaseModel> {

    private final BaseModel request;
    private final BaseModel response;
    private final ModelComparator comparator;

    private ModelAssertions(BaseModel request, BaseModel response) {
        super(response, ModelAssertions.class);
        this.request = request;
        this.response = response;
        this.comparator = ModelComparator.getInstance();
    }

    public static ModelAssertions assertThatModels(BaseModel request, BaseModel response) {
        return new ModelAssertions(request, response);
    }

    /**
     * Сравнивает все поля запроса и ответа согласно маппингу в properties.
     * Пропускает поля, помеченные как игнорируемые (password).
     */
    public void match() {
        isNotNull();

        Map<String, String> mapping = comparator.getFieldMapping(
                request.getClass(), response.getClass()
        );

        if (mapping.isEmpty()) {
            failWithMessage("No mapping found for %s → %s in model-comparison.properties",
                    request.getClass().getSimpleName(),
                    response.getClass().getSimpleName());
            return;
        }

        Set<String> ignoredFields = comparator.getIgnoredFields(
                request.getClass(), response.getClass()
        );

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String requestField = entry.getKey();
            String responsePath = entry.getValue();

            if (ignoredFields.contains(requestField)) {
                continue;
            }

            Object requestValue = getFieldValue(request, requestField);
            Object responseValue = getNestedFieldValue(response, responsePath);

            if (!Objects.equals(requestValue, responseValue)) {
                failWithActualExpectedAndMessage(
                        responseValue,
                        requestValue,
                        "\nField mismatch: request.%s → response.%s".formatted(requestField, responsePath)
                );
            }
        }
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field == null) {
                failWithMessage("Field '%s' not found in %s", fieldName, obj.getClass().getSimpleName());
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
}