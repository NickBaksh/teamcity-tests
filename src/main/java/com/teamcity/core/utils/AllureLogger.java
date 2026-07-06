package com.teamcity.core.utils;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllureLogger {

    public static void log(String message) {
        log.info(message);
        Allure.addAttachment("Log", "text/plain", message);
    }

    public static void logStep(String step) {
        log.info("🔹 {}", step);
        Allure.step(step);
    }

    public static void logError(String error) {
        log.error("❌ {}", error);
        Allure.addAttachment("Error", "text/plain", error);
    }
}