package com.teamcity.ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.teamcity.ui.testdata.UiTestData;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.Selenide.$x;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateProjectPage {

    private final SelenideElement nameInput = $("#name, input[name='name'], input[data-test='create-project-name']");
    private final SelenideElement idInput = $("#externalId, #id, input[name='externalId'], input[name='id']");
    private final SelenideElement createButton = $x(
            "//input[@value='Create'] | //button[contains(.,'Create')]"
    );
    private final SelenideElement errorMessage = $(
            ".error, .errorMessage, [data-test='error'], .ring-error-message, error"
    );

    @Step("Open create project page under Root")
    public CreateProjectPage openPage() {
        open(UiRoutes.createProjectUnderRoot());
        nameInput.shouldBe(visible);
        return this;
    }

    @Step("Create project name={name}, id={id}")
    public ProjectPage create(String name, String id) {
        fill(name, id);
        createButton.shouldBe(visible).click();
        return new ProjectPage();
    }

    @Step("Submit create project form expecting validation error")
    public CreateProjectPage createExpectingError(String name, String id) {
        fill(name, id);
        createButton.shouldBe(visible).click();
        sleep(1000);
        return this;
    }

    @Step("Get create project error text")
    public String errorText() {
        if (errorMessage.exists() && errorMessage.is(visible)) {
            return errorMessage.getText();
        }
        return WebDriverRunner.source();
    }

    @Step("Check validation error is present")
    public boolean hasValidationError() {
        String source = WebDriverRunner.source();
        return source.contains(UiTestData.ERROR_EMPTY_PROJECT_NAME_CODE)
                || source.contains(UiTestData.ERROR_DUPLICATE_PROJECT_ID_CODE)
                || source.contains(UiTestData.ERROR_PROJECT_NAME_EMPTY_TEXT)
                || source.contains(UiTestData.ERROR_PROJECT_ID_USED_TEXT)
                || (errorMessage.exists() && errorMessage.is(visible));
    }

    @Step("Assert empty project name validation error")
    public CreateProjectPage shouldShowEmptyNameError() {
        assertThat(hasValidationError()).isTrue();
        assertThat(errorText()).containsIgnoringCase(UiTestData.ERROR_EMPTY);
        return this;
    }

    @Step("Assert duplicate project id validation error")
    public CreateProjectPage shouldShowDuplicateIdError() {
        assertThat(hasValidationError()).isTrue();
        assertThat(errorText()).containsIgnoringCase(UiTestData.ERROR_ALREADY_USED);
        return this;
    }

    private void fill(String name, String id) {
        nameInput.shouldBe(visible).setValue(name == null ? "" : name);
        if (id != null && idInput.exists()) {
            idInput.clear();
            idInput.setValue(id);
        }
    }
}
