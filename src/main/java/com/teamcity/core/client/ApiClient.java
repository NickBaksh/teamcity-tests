package com.teamcity.core.client;

import io.restassured.response.Response;
import java.util.Map;

public interface ApiClient {
    // Базовые HTTP методы
    Response get(String endpoint, Object... pathParams);
    Response post(String endpoint, Object body);
    Response post(String endpoint, Object body, Object... pathParams);
    Response put(String endpoint, Object body, Object... pathParams);
    Response delete(String endpoint, Object... pathParams);

    // Расширенные методы с обработкой
    <T> T execute(ApiRequest request, Class<T> responseType);
    <T> T execute(ApiRequest request, ResponseHandler<T> handler);

    // === Существующие методы ===
    Response putText(String endpoint, String body, Object... pathParams);
    Response putBoolean(String endpoint, boolean body, Object... pathParams);

    /**
     * GET запрос с текстовым ответом (для эндпоинтов, возвращающих text/plain)
     */
    default Response getText(String endpoint, Object... pathParams) {
        return get(endpoint, pathParams);
    }

    // ===== НОВЫЕ МЕТОДЫ ДЛЯ ИСПРАВЛЕНИЯ ОШИБКИ 406 =====

    /**
     * GET запрос с кастомными заголовками
     *
     * @param endpoint путь запроса
     * @param headers карта заголовков (например, {"Accept": "application/json"})
     * @param pathParams параметры для подстановки в путь
     * @return Response объект
     */
    Response get(String endpoint, Map<String, String> headers, Object... pathParams);

    /**
     * GET запрос с явным указанием типа Accept
     *
     * @param endpoint путь запроса
     * @param requestType тип запроса (JSON, TEXT, PLAIN)
     * @param pathParams параметры для подстановки в путь
     * @return Response объект
     */
    default Response get(String endpoint, RequestType requestType, Object... pathParams) {
        return get(endpoint, Map.of("Accept", requestType.getContentType()), pathParams);
    }

    /**
     * POST запрос с кастомными заголовками
     */
    Response post(String endpoint, Object body, Map<String, String> headers, Object... pathParams);

    /**
     * PUT запрос с кастомными заголовками
     */
    Response put(String endpoint, Object body, Map<String, String> headers, Object... pathParams);

    /**
     * DELETE запрос с кастомными заголовками
     */
    Response delete(String endpoint, Map<String, String> headers, Object... pathParams);
}