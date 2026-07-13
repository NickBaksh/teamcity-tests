package com.teamcity.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматического создания пользователя перед тестом.
 * Аналогична AdminSession, но для обычного пользователя.
 *
 * Пример использования:
 * {@code @UserSession(prefix = "TestUser", accounts = 2, role = "USER")}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UserSession {

    /**
     * Префикс для имени пользователя
     */
    String prefix() default "User";

    /**
     * Количество аккаунтов для создания
     */
    int accounts() default 2;

    /**
     * Роль пользователя (USER, ADMIN)
     */
    String role() default "USER";

    /**
     * Создавать ли пользователя (если false - используем существующего)
     */
    boolean create() default true;
}