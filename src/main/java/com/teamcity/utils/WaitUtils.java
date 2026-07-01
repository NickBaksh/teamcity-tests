package com.teamcity.utils;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Универсальный помощник для ожиданий с повторными попытками
 */
public class WaitUtils {

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 500;

    // ========== Базовые методы с повторными попытками ==========

    public static <T> T doWithRetry(Supplier<T> action, int maxRetries, int delayMs) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return action.get();
            } catch (AssertionError | TimeoutException e) {
                if (attempt == maxRetries) {
                    throw new AssertionError(
                            String.format("Action failed after %d attempts. Last error: %s", maxRetries, e.getMessage()), e);
                }
                System.out.println("Attempt " + attempt + "/" + maxRetries + " failed, retrying...");
                sleep(delayMs);
            }
        }
        throw new AssertionError("Action failed after " + maxRetries + " attempts");
    }

    public static <T> T doWithRetry(Supplier<T> action) {
        return doWithRetry(action, DEFAULT_RETRY_COUNT, RETRY_DELAY_MS);
    }

    public static void doWithRetry(Runnable action, int maxRetries, int delayMs) {
        doWithRetry(() -> {
            action.run();
            return null;
        }, maxRetries, delayMs);
    }

    public static void doWithRetry(Runnable action) {
        doWithRetry(action, DEFAULT_RETRY_COUNT, RETRY_DELAY_MS);
    }

    // ========== Ожидания элементов ==========

    public static WebElement waitForElement(By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForElement(By locator) {
        return waitForElement(locator, DEFAULT_TIMEOUT_SECONDS);
    }

    public static boolean waitForText(By locator, String expectedText, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
    }

    // ========== Ожидания для Selenide ==========

    public static SelenideElement waitForVisible(SelenideElement element, int timeoutSeconds) {
        return doWithRetry(() -> {
            element.shouldBe(com.codeborne.selenide.Condition.visible);
            return element;
        });
    }

    public static SelenideElement waitForEnabled(SelenideElement element, int timeoutSeconds) {
        return doWithRetry(() -> {
            element.shouldBe(com.codeborne.selenide.Condition.enabled);
            return element;
        });
    }

    public static SelenideElement waitForClickable(SelenideElement element, int timeoutSeconds) {
        return doWithRetry(() -> {
            element.shouldBe(com.codeborne.selenide.Condition.visible)
                    .shouldBe(com.codeborne.selenide.Condition.enabled);
            return element;
        });
    }

    public static void waitForText(SelenideElement element, String expectedText, int timeoutSeconds) {
        doWithRetry(() -> element.shouldHave(com.codeborne.selenide.Condition.text(expectedText)));
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}