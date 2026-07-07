package com.teamcity.core.utils;

import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.User;

import java.util.UUID;

public class TestDataFactory {

    public static final String DEFAULT_PASSWORD = "TestPass123!";

    // ===== Генерация проектов =====
    public Project createRandomProject() {
        String name = "Project_" + System.currentTimeMillis();
        return Project.builder()
                .name(name)
                .description("Auto-generated project: " + name)
                .parentProjectId("_Root")
                .build();
    }

    public Project createRandomProject(String parentProjectId) {
        String name = "Project_" + System.currentTimeMillis();
        return Project.builder()
                .name(name)
                .description("Auto-generated project: " + name)
                .parentProjectId(parentProjectId)
                .build();
    }

    // ===== Генерация пользователей =====
    public User createRandomUser() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "testuser_" + timestamp;
        return User.builder()
                .username(username)
                .password(DEFAULT_PASSWORD)
                .email("test_" + timestamp + "@example.com")
                .name("Test User " + timestamp)
                .build();
    }

    // ===== Генерация Build Configs =====
    public BuildConfig createRandomBuildConfig(String projectId) {
        String name = "BuildConfig_" + System.currentTimeMillis();
        return BuildConfig.builder()
                .name(name)
                .projectId(projectId)
                .description("Auto-generated build config: " + name)
                .build();
    }

    // ===== НОВЫЕ МЕТОДЫ ДЛЯ ГЕНЕРАЦИИ УНИКАЛЬНЫХ ИМЕН =====

    /**
     * Генерирует уникальное имя с указанным префиксом.
     */
    public String generateUniqueName(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    /**
     * Генерирует уникальное имя проекта.
     */
    public String generateUniqueProjectName() {
        return "Project_" + System.currentTimeMillis();
    }

    /**
     * Генерирует уникальное имя для Build Config.
     */
    public String generateUniqueBuildConfigName() {
        return "BuildConfig_" + System.currentTimeMillis();
    }

    /**
     * Генерирует уникальное имя пользователя.
     */
    public String generateUniqueUsername() {
        return "user_" + System.currentTimeMillis();
    }

    /**
     * Генерирует уникальный email.
     */
    public String generateUniqueEmail() {
        return "test_" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * Генерирует случайную строку заданной длины.
     */
    public String randomString(int length) {
        return UUID.randomUUID().toString().substring(0, Math.min(length, 36));
    }

    /**
     * Генерирует случайный пароль.
     */
    public String randomPassword() {
        return "P@ssw0rd_" + randomString(6);
    }
}