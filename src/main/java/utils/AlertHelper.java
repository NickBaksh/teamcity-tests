package utils;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Универсальный помощник для работы с alert'ами
 * Поддерживает:
 * - Ожидание alert с таймаутом
 * - Повторные попытки при клике
 * - Проверку текста alert
 * - Кастомные действия перед ожиданием
 */
public class AlertHelper {

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 500;

    /**
     * Ожидание появления alert
     */
    public static Alert waitForAlert(int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(timeoutSeconds));
            return wait.until(ExpectedConditions.alertIsPresent());
        } catch (TimeoutException e) {
            throw new AssertionError("Alert not present after " + timeoutSeconds + " seconds");
        }
    }

    public static Alert waitForAlert() {
        return waitForAlert(DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Проверка, что alert существует
     */
    public static boolean isAlertPresent() {
        try {
            WebDriverRunner.getWebDriver().switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    /**
     * Получить текст alert и принять его
     */
    public static String getAlertTextAndAccept(int timeoutSeconds) {
        Alert alert = waitForAlert(timeoutSeconds);
        String text = alert.getText();
        alert.accept();
        return text;
    }

    public static String getAlertTextAndAccept() {
        return getAlertTextAndAccept(DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Получить текст alert и отменить его
     */
    public static String getAlertTextAndDismiss(int timeoutSeconds) {
        Alert alert = waitForAlert(timeoutSeconds);
        String text = alert.getText();
        alert.dismiss();
        return text;
    }

    /**
     * Проверить текст alert и принять его
     */
    public static void verifyAlertAndAccept(String expectedMessage, int timeoutSeconds) {
        String actualMessage = getAlertTextAndAccept(timeoutSeconds);
        if (expectedMessage != null && !expectedMessage.isEmpty()) {
            assert actualMessage.equals(expectedMessage) :
                    String.format("Alert text mismatch. Expected: '%s', Actual: '%s'", expectedMessage, actualMessage);
        }
    }

    public static void verifyAlertAndAccept(String expectedMessage) {
        verifyAlertAndAccept(expectedMessage, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Выполнить действие и дождаться alert (с повторными попытками)
     */
    public static String performActionAndWaitForAlert(Runnable action, int timeoutSeconds, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                action.run();
                return getAlertTextAndAccept(timeoutSeconds);
            } catch (AssertionError e) {
                if (attempt == maxRetries) {
                    throw new AssertionError(
                            String.format("Alert not appeared after %d attempts. Last error: %s", maxRetries, e.getMessage()), e);
                }
                System.out.println("Attempt " + attempt + "/" + maxRetries + " failed, retrying...");
                sleep(RETRY_DELAY_MS);
            }
        }
        throw new AssertionError("Failed to get alert after " + maxRetries + " attempts");
    }

    public static String performActionAndWaitForAlert(Runnable action) {
        return performActionAndWaitForAlert(action, DEFAULT_TIMEOUT_SECONDS, DEFAULT_RETRY_COUNT);
    }

    /**
     * Выполнить действие и проверить alert (с повторными попытками)
     */
    public static void performActionAndVerifyAlert(Runnable action, String expectedMessage, int timeoutSeconds, int maxRetries) {
        String actualMessage = performActionAndWaitForAlert(action, timeoutSeconds, maxRetries);
        if (expectedMessage != null && !expectedMessage.isEmpty()) {
            assert actualMessage.equals(expectedMessage) :
                    String.format("Alert text mismatch. Expected: '%s', Actual: '%s'", expectedMessage, actualMessage);
        }
    }

    public static void performActionAndVerifyAlert(Runnable action, String expectedMessage) {
        performActionAndVerifyAlert(action, expectedMessage, DEFAULT_TIMEOUT_SECONDS, DEFAULT_RETRY_COUNT);
    }

    /**
     * Для SelenideElement - кликнуть и дождаться alert
     */
    public static String clickAndWaitForAlert(SelenideElement element, int timeoutSeconds, int maxRetries) {
        return performActionAndWaitForAlert(() -> {
            element.shouldBe(com.codeborne.selenide.Condition.visible)
                    .shouldBe(com.codeborne.selenide.Condition.enabled)
                    .click();
        }, timeoutSeconds, maxRetries);
    }

    public static String clickAndWaitForAlert(SelenideElement element) {
        return clickAndWaitForAlert(element, DEFAULT_TIMEOUT_SECONDS, DEFAULT_RETRY_COUNT);
    }

    public static void clickAndVerifyAlert(SelenideElement element, String expectedMessage) {
        String actualMessage = clickAndWaitForAlert(element);
        assert actualMessage.equals(expectedMessage) :
                String.format("Alert text mismatch. Expected: '%s', Actual: '%s'", expectedMessage, actualMessage);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}