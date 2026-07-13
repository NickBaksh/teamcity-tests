package com.teamcity.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания допустимых окружений для выполнения теста.
 * Тест будет выполняться только если текущее окружение соответствует указанному.
 *
 * Пример использования:
 * {@code @Environment({"stage", "prod"})}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Environment {
    String[] value() default {"stage"};
}
