package com.teamcity.ui.config;

import com.codeborne.selenide.Configuration;
import com.teamcity.core.config.ConfigManager;

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
    }
}
