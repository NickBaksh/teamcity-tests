package com.teamcity.ui.config;

import com.codeborne.selenide.Configuration;
import com.teamcity.core.config.ConfigManager;
import org.openqa.selenium.remote.DesiredCapabilities;

public final class SelenideConfig {

    private SelenideConfig() {
    }

    public static void apply() {
        Configuration.baseUrl = ConfigManager.getUiBaseUrl();
        Configuration.browser = ConfigManager.getBrowser();
        Configuration.headless = ConfigManager.isHeadless();
        Configuration.timeout = ConfigManager.getApiTimeout();
        Configuration.pageLoadTimeout = Math.max(ConfigManager.getApiTimeout(), 60_000);
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
        Configuration.browserSize = "1920x1080";
        Configuration.pageLoadStrategy = "eager";

        String selenoidUrl = System.getProperty("selenoid.url");

        if (selenoidUrl == null || selenoidUrl.isEmpty()) {
            selenoidUrl = System.getenv("SELENOID_URL");
        }

        if (selenoidUrl == null || selenoidUrl.isEmpty()) {
            selenoidUrl = System.getProperty("selenide.remote");
        }

        if (selenoidUrl == null || selenoidUrl.isEmpty()) {
            selenoidUrl = System.getenv("SELENIDE_REMOTE_URL");
        }

        if (selenoidUrl != null && !selenoidUrl.isEmpty() && !"null".equalsIgnoreCase(selenoidUrl)) {
            String remote = selenoidUrl.trim();
            while (remote.endsWith("/")) {
                remote = remote.substring(0, remote.length() - 1);
            }
            if (!remote.endsWith("/wd/hub")) {
                remote = remote + "/wd/hub";
            }
            Configuration.remote = remote;

            DesiredCapabilities capabilities = getDesiredCapabilities();
            Configuration.browserCapabilities = capabilities;

            System.out.println("Using Selenoid at: " + remote);
            System.out.println("Browser: " + ConfigManager.getBrowser());
            System.out.println("Headless: " + ConfigManager.isHeadless());
        } else {
            System.out.println("Using local browser: " + ConfigManager.getBrowser());
        }
    }

    private static DesiredCapabilities getDesiredCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", ConfigManager.getBrowser());

        java.util.Map<String, Object> selenoidOptions = new java.util.HashMap<>();
        selenoidOptions.put("enableVNC", true);
        selenoidOptions.put("enableVideo", false);
        selenoidOptions.put("enableLog", true);
        selenoidOptions.put("sessionTimeout", "5m");

        capabilities.setCapability("selenoid:options", selenoidOptions);
        return capabilities;
    }
}