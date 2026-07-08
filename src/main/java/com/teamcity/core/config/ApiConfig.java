package com.teamcity.core.config;

import org.aeonbits.owner.Config;

@Config.Sources({
        "file:src/test/resources/config/local.properties",
        "file:src/test/resources/config/ci.properties",
        "system:properties"
})
public interface ApiConfig extends Config {
    @Key("api.base.url")
    @DefaultValue("http://localhost:8111")
    String baseUrl();

    @Key("api.timeout")
    @DefaultValue("30")
    int timeout();

    @Key("api.retry.count")
    @DefaultValue("3")
    int retryCount();

    @Key("api.retry.delay")
    @DefaultValue("1000")
    long retryDelay();

    @Key("api.retry.exponential")
    @DefaultValue("true")
    boolean retryExponential();

    @Key("auth.admin.username")
    @DefaultValue("admin")
    String adminUsername();

    @Key("auth.admin.password")
    @DefaultValue("admin")
    String adminPassword();

    @Key("auth.user.username")
    String userUsername();

    @Key("auth.user.password")
    String userPassword();

    @Key("auth.token")
    String authToken();

    @Key("selenium.headless")
    @DefaultValue("false")
    boolean headless();

    @Key("selenium.browser")
    @DefaultValue("chrome")
    String browser();

    @Key("allure.enabled")
    @DefaultValue("true")
    boolean allureEnabled();

    @Key("log.level")
    @DefaultValue("INFO")
    String logLevel();
}