package com.teamcity.ui.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class LoginPage {

    private final SelenideElement usernameInput = $("#username, input[name='username']");
    private final SelenideElement passwordInput = $("#password, input[name='password']");
    private final SelenideElement loginButton = $(".loginButton, input[name='submitLogin'], button[type='submit']");
    private final SelenideElement errorMessage = $(
            ".error, .loginPage__error, [data-test='error'], .ring-error-message, "
                    + ".error-message, #errorMessage, .login-error, .attentionComment"
    );

    @Step("Open login page")
    public LoginPage openPage() {
        open("/login.html");
        usernameInput.shouldBe(visible);
        return this;
    }

    @Step("Login as {username}")
    public void login(String username, String password) {
        usernameInput.shouldBe(visible).setValue(username);
        passwordInput.setValue(password);
        loginButton.click();
    }

    @Step("Login and expect success")
    public void loginSuccessfully(String username, String password) {
        login(username, password);
        com.codeborne.selenide.Selenide.Wait()
                .until(driver -> !driver.getCurrentUrl().contains("/login.html"));
    }

    @Step("Get login error text")
    public String errorText() {
        return errorMessage.shouldBe(visible).getText();
    }

    @Step("Check login failed and form is still shown")
    public boolean isLoginFailed() {
        return usernameInput.is(visible)
                && (errorMessage.exists() && errorMessage.is(visible) || loginButton.is(visible));
    }
}
