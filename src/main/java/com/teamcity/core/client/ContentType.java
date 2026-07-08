package com.teamcity.core.client;

import lombok.Getter;

@Getter
public enum ContentType {
    // Стандартные типы
    JSON("application/json"),
    TEXT_PLAIN("text/plain"),
    XML("application/xml"),
    HTML("text/html"),
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART_FORM("multipart/form-data"),

    // TeamCity специфичные
    TEAMCITY_JSON("application/json"),
    TEAMCITY_XML("application/xml"),
    TEAMCITY_PLAIN("text/plain");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    /**
     * Получить ContentType по строковому значению
     */
    public static ContentType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return JSON;
        }

        String normalized = value.toLowerCase().trim();

        // Убираем параметры из Content-Type (например, charset)
        if (normalized.contains(";")) {
            normalized = normalized.split(";")[0].trim();
        }

        for (ContentType type : ContentType.values()) {
            if (type.getValue().equalsIgnoreCase(normalized)) {
                return type;
            }
        }

        // Определяем по ключевым словам
        if (normalized.contains("json")) {
            return JSON;
        }
        if (normalized.contains("xml")) {
            return XML;
        }
        if (normalized.contains("text/plain") || normalized.contains("txt")) {
            return TEXT_PLAIN;
        }
        if (normalized.contains("html")) {
            return HTML;
        }
        if (normalized.contains("form-urlencoded")) {
            return FORM_URLENCODED;
        }
        if (normalized.contains("multipart")) {
            return MULTIPART_FORM;
        }

        return JSON;
    }

    /**
     * Проверка, является ли тип JSON
     */
    public boolean isJson() {
        return this == JSON || this == TEAMCITY_JSON;
    }

    /**
     * Проверка, является ли тип XML
     */
    public boolean isXml() {
        return this == XML || this == TEAMCITY_XML;
    }

    /**
     * Проверка, является ли тип текстовым
     */
    public boolean isText() {
        return this == TEXT_PLAIN || this == TEAMCITY_PLAIN;
    }

    /**
     * Получить RestAssured ContentType
     */
    public io.restassured.http.ContentType toRestAssured() {
        switch (this) {
            case JSON:
            case TEAMCITY_JSON:
                return io.restassured.http.ContentType.JSON;
            case TEXT_PLAIN:
            case TEAMCITY_PLAIN:
                return io.restassured.http.ContentType.TEXT;
            case XML:
            case TEAMCITY_XML:
                return io.restassured.http.ContentType.XML;
            case FORM_URLENCODED:
                return io.restassured.http.ContentType.URLENC;
            case MULTIPART_FORM:
                return io.restassured.http.ContentType.MULTIPART;
            default:
                return io.restassured.http.ContentType.JSON;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}