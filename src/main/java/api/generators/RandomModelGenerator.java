package api.generators;

import api.models.BaseModel;
import com.github.curiousoddman.rgxgen.RgxGen;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Random;

public class RandomModelGenerator {

    private static final Random random = new Random();

    public static <T extends BaseModel> T generate(Class<T> modelClass) {
        try {
            Constructor<T> constructor = modelClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            fillFields(instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate model: " + modelClass.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends BaseModel> T generateWithBuilder(Class<T> modelClass) {
        try {
            Class<?> builderClass = Class.forName(modelClass.getName() + "$" + modelClass.getSimpleName() + "Builder");
            Object builder = builderClass.getDeclaredConstructor().newInstance();

            for (Field field : modelClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(GeneratingRule.class)) {
                    GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                    String value = generateFromRegex(rule.regex());  // ← новый метод
                    setBuilderField(builder, field.getName(), value, field.getType());
                }
            }

            return (T) builderClass.getMethod("build").invoke(builder);
        } catch (Exception e) {
            return generate(modelClass);
        }
    }

    /**
     * Генерирует строку по регулярному выражению.
     */
    public static String generateFromRegex(String regex) {
        RgxGen rgxGen = new RgxGen(regex);  // ← создаётся на каждый regex
        return rgxGen.generate();
    }

    private static <T> void fillFields(T instance) throws IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(GeneratingRule.class)) {
                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                String value = generateFromRegex(rule.regex());  // ← новый метод
                setFieldValue(instance, field, value);
            }
        }
    }

    private static void setFieldValue(Object instance, Field field, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(instance, value);
    }

    private static void setFieldValue(Object instance, Field field, String value) throws IllegalAccessException {
        field.setAccessible(true);
        Class<?> type = field.getType();

        if (type == String.class) {
            field.set(instance, value);
        } else if (type == Integer.class || type == int.class) {
            field.set(instance, Integer.parseInt(value));
        } else if (type == Long.class || type == long.class) {
            field.set(instance, Long.parseLong(value));
        } else if (type == Double.class || type == double.class) {
            field.set(instance, Double.parseDouble(value));
        } else if (type == Boolean.class || type == boolean.class) {
            field.set(instance, Boolean.parseBoolean(value));
        } else {
            field.set(instance, value);
        }
    }

    private static void setBuilderField(Object builder, String fieldName, Object value, Class<?> fieldType) {
        try {
            builder.getClass()
                    .getMethod(fieldName, fieldType)
                    .invoke(builder, value);
        } catch (Exception e) {
            // поле не поддерживается builder'ом — пропускаем
        }
    }
}