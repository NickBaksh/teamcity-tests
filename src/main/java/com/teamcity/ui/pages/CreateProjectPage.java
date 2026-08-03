package com.teamcity.ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.teamcity.ui.testdata.UiTestData;
import io.qameta.allure.Step;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.partialText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateProjectPage {

    private static final Pattern REDIRECT = Pattern.compile("<redirect>([^<]+)</redirect>");

    private final SelenideElement nameInput = $("#name, input[name='name'], input[data-test='create-project-name']");
    private final SelenideElement idInput = $("#externalId, #id, input[name='externalId'], input[name='id']");
    private final SelenideElement createButton = $x(
            "//input[@value='Create'] | //button[contains(.,'Create')]"
    );
    private final SelenideElement errorMessage = $(
            ".error, .errorMessage, [data-test='error'], .ring-error-message"
    );
    private final SelenideElement vcsRootNameInput = $(
            "[data-test='vcs-root-name-input'], #vcsRootName, [name='vcsRootName'], input[name='name']"
    );
    private final SelenideElement vcsRootUrlInput = $(
            "[data-test='vcs-root-url-input'], #url, [name='url'], input[name='prop:url'], #repositoryUrl"
    );
    private final SelenideElement vcsRootBranchInput = $(
            "[data-test='vcs-root-branch-input'], #branch, [name='branch'], input[name='prop:branch']"
    );
    private final SelenideElement vcsRootCreateButton = $(
            "[data-test='create-vcs-root-button'], .saveButton, input[name='submitButton'], "
                    + "input[value='Create'], input[value='Save']"
    );
    private final SelenideElement gitVcsType = $x(
            "//a[contains(.,'Git')] | //*[contains(@class,'vcsName') and contains(.,'Git')] "
                    + "| //input[@value='jetbrains.git']/ancestor::a[1]"
    );
    private final SelenideElement errorVcsMessage = $("[data-test='error-message'], .error, .field-error");
    private final SelenideElement body = $("body");

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
        followClassicXmlRedirectIfPresent();
        return new ProjectPage();
    }

    @Step("Submit create project form expecting validation error")
    public CreateProjectPage createExpectingError(String name, String id) {
        fill(name, id);
        createButton.shouldBe(visible).click();
        return this;
    }

    @Step("Get create project error text")
    public String errorText() {
        waitUntilPageSourceContainsAny(
                UiTestData.ERROR_EMPTY_PROJECT_NAME_CODE,
                UiTestData.ERROR_DUPLICATE_PROJECT_ID_CODE,
                UiTestData.ERROR_EMPTY,
                UiTestData.ERROR_ALREADY_USED,
                UiTestData.ERROR_PROJECT_NAME_EMPTY_TEXT,
                UiTestData.ERROR_PROJECT_ID_USED_TEXT
        );
        if (errorMessage.exists() && errorMessage.is(visible)) {
            return errorMessage.getText();
        }
        return pageSource();
    }

    @Step("Assert empty project name validation error")
    public CreateProjectPage shouldShowEmptyNameError() {
        String source = errorText();
        assertThat(source)
                .as("Empty project name validation")
                .satisfiesAnyOf(
                        s -> assertThat(s).containsIgnoringCase(UiTestData.ERROR_EMPTY_PROJECT_NAME_CODE),
                        s -> assertThat(s).containsIgnoringCase(UiTestData.ERROR_EMPTY),
                        s -> assertThat(s).containsIgnoringCase(UiTestData.ERROR_PROJECT_NAME_EMPTY_TEXT)
                );
        return this;
    }

    @Step("Assert duplicate project id validation error")
    public CreateProjectPage shouldShowDuplicateIdError() {
        String source = errorText();
        assertThat(source)
                .as("Duplicate project id validation")
                .satisfiesAnyOf(
                        s -> assertThat(s).containsIgnoringCase(UiTestData.ERROR_DUPLICATE_PROJECT_ID_CODE),
                        s -> assertThat(s).containsIgnoringCase(UiTestData.ERROR_ALREADY_USED),
                        s -> assertThat(s).containsIgnoringCase(UiTestData.ERROR_PROJECT_ID_USED_TEXT)
                );
        return this;
    }

    private void followClassicXmlRedirectIfPresent() {
        waitUntilPageSourceContainsAny("<redirect>", "editProject");
        String source = pageSource();
        Matcher matcher = REDIRECT.matcher(source);
        if (matcher.find()) {
            open(UiUrls.toRelative(matcher.group(1).trim()));
        }
    }

    private void waitUntilPageSourceContainsAny(String... markers) {
        new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(UiTestData.UI_LONG_TIMEOUT_SECONDS))
                .until(driver -> {
                    String source = driver.getPageSource();
                    String url = driver.getCurrentUrl();
                    for (String marker : markers) {
                        if (source != null && source.contains(marker)) {
                            return true;
                        }
                        if (url != null && url.contains(marker)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    private String pageSource() {
        return WebDriverRunner.source();
    }

    private void fill(String name, String id) {
        nameInput.shouldBe(visible).setValue(name == null ? "" : name);
        if (id != null && idInput.exists()) {
            idInput.clear();
            idInput.setValue(id);
        }
    }

    @Step("VCS Root creation page should be opened")
    public CreateProjectPage shouldBeOpened() {
        vcsRootNameInput.shouldBe(visible);
        return this;
    }

    @Step("Open VCS Root creation page for project: {projectId}")
    public CreateProjectPage openVcsRootCreation(String projectId) {
        open(UiRoutes.createVcsRoot(projectId));
        if (gitVcsType.exists() && gitVcsType.is(visible)) {
            gitVcsType.click();
        }
        return this;
    }

    @Step("Set VCS Root name: {name}")
    public CreateProjectPage setVcsRootName(String name) {
        vcsRootNameInput.shouldBe(visible).setValue(name);
        return this;
    }

    @Step("Set VCS Root URL: {url}")
    public CreateProjectPage setVcsRootUrl(String url) {
        vcsRootUrlInput.shouldBe(visible).setValue(url);
        return this;
    }

    @Step("Set VCS Root branch: {branch}")
    public CreateProjectPage setVcsRootBranch(String branch) {
        vcsRootBranchInput.shouldBe(visible).setValue(branch);
        return this;
    }

    @Step("Clear VCS Root branch")
    public CreateProjectPage clearBranch() {
        vcsRootBranchInput.shouldBe(visible).clear();
        return this;
    }

    @Step("Click create VCS Root button")
    public CreateProjectPage clickCreate() {
        vcsRootCreateButton.shouldBe(visible).click();
        return this;
    }

    @Step("Check error message appears")
    public CreateProjectPage shouldHaveError() {
        waitUntilPageSourceContainsAny("error", "Error", "failed", "Failed", "cannot", "Cannot");
        if (errorVcsMessage.exists()) {
            errorVcsMessage.shouldBe(visible);
        } else if (errorMessage.exists()) {
            errorMessage.shouldBe(visible);
        } else {
            body.shouldHave(partialText("error").or(partialText("Error")).or(partialText("fail")));
        }
        return this;
    }

    @Step("Check error message appears: {expectedText}")
    public CreateProjectPage shouldHaveError(String expectedText) {
        waitUntilPageSourceContainsAny(expectedText);
        assertThat(pageSource()).containsIgnoringCase(expectedText);
        return this;
    }
}
