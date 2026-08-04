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
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;
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
    private final SelenideElement vcsRootNameInput = $("#vcsRootName, [name='vcsRootName']");
    private final SelenideElement vcsRootUrlInput = $x(
            "//label[contains(.,'Repository URL') or contains(.,'Fetch URL') or contains(.,'Fetch url')]"
                    + "/following::input[not(@type='hidden')][1]"
                    + " | //input[contains(@name,'prop:url') or @name='url']"
                    + " | //textarea[contains(@name,'prop:url') or @name='url']"
    );
    private final SelenideElement vcsRootBranchInput = $x(
            "//label[contains(.,'Default branch')]/following::input[not(@type='hidden')][1]"
                    + " | //input[contains(@name,'branch') or contains(@id,'branch')]"
    );
    private final SelenideElement gitTypeOption = $x(
            "//*[@data-test='ring-popup' or @data-test='ring-list' or contains(@class,'popup')"
                    + " or contains(@class,'Popup')]//*[normalize-space()='Git']"
                    + " | //li[@data-title='Git']"
                    + " | //*[@role='option' and normalize-space()='Git']"
                    + " | //*[@data-test='ring-list-item-label' and normalize-space()='Git']"
    );
    private final SelenideElement typeOfVcsControl = $x(
            "//button[contains(normalize-space(.),'Guess from repository URL')]"
                    + " | //*[@data-test='ring-select'][contains(.,'Guess')]"
                    + " | //label[contains(.,'Type of VCS')]/following::button[1]"
    );
    private final SelenideElement showAdvancedOptions = $x(
            "//*[self::a or self::button or self::span][contains(.,'Show advanced options')]"
    );
    private final SelenideElement createVcsRootLink = $x(
            "//a[contains(@href,'addVcsRoot') or contains(@href,'action=addVcsRoot')]"
                    + " | //a[contains(.,'Create VCS root') or contains(.,'Create new VCS root') "
                    + "or contains(.,'New VCS root')]"
                    + " | //button[contains(.,'Create VCS root') or contains(.,'New VCS root')]"
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
        assertThat(containsIgnoreCaseAny(source,
                UiTestData.ERROR_EMPTY_PROJECT_NAME_CODE,
                UiTestData.ERROR_EMPTY,
                UiTestData.ERROR_PROJECT_NAME_EMPTY_TEXT))
                .as("Empty project name validation")
                .isTrue();
        return this;
    }

    @Step("Assert duplicate project id validation error")
    public CreateProjectPage shouldShowDuplicateIdError() {
        String source = errorText();
        assertThat(containsIgnoreCaseAny(source,
                UiTestData.ERROR_DUPLICATE_PROJECT_ID_CODE,
                UiTestData.ERROR_ALREADY_USED,
                UiTestData.ERROR_PROJECT_ID_USED_TEXT))
                .as("Duplicate project id validation")
                .isTrue();
        return this;
    }

    private static boolean containsIgnoreCaseAny(String source, String... markers) {
        if (source == null) {
            return false;
        }
        String lower = source.toLowerCase();
        for (String marker : markers) {
            if (marker != null && lower.contains(marker.toLowerCase())) {
                return true;
            }
        }
        return false;
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
        vcsRootUrlInput.shouldBe(visible);
        return this;
    }

    @Step("Open VCS Root creation page for project: {projectId}")
    public CreateProjectPage openVcsRootCreation(String projectId) {
        open(UiRoutes.editProject(projectId));
        open(UiRoutes.projectVcsRoots(projectId));
        if (!(createVcsRootLink.exists() && createVcsRootLink.is(visible))) {
            open(UiRoutes.projectVcsRootsAlt(projectId));
        }
        if (createVcsRootLink.exists() && createVcsRootLink.is(visible)) {
            createVcsRootLink.click();
        } else {
            open(UiRoutes.createVcsRoot(projectId));
        }
        vcsRootUrlInput.shouldBe(visible);
        return this;
    }

    @Step("Open Git VCS Root creation via UI for project: {projectId}")
    public CreateProjectPage openGitVcsRootCreation(String projectId) {
        openVcsRootCreation(projectId);
        selectGitType();
        showAdvancedOptions();
        vcsRootNameInput.shouldBe(visible);
        vcsRootUrlInput.shouldBe(visible);
        return this;
    }

    @Step("Reveal advanced VCS root fields")
    public CreateProjectPage showAdvancedOptions() {
        if (showAdvancedOptions.exists() && showAdvancedOptions.is(visible)) {
            showAdvancedOptions.click();
        }
        return this;
    }

    @Step("Select Git VCS type")
    public CreateProjectPage selectGitType() {
        typeOfVcsControl.shouldBe(visible).click();
        if (!(gitTypeOption.exists() && gitTypeOption.is(visible))) {
            executeJavaScript("arguments[0].click();", typeOfVcsControl);
        }
        gitTypeOption.shouldBe(visible).click();
        vcsRootNameInput.shouldBe(visible);
        return this;
    }

    @Step("Set VCS Root name: {name}")
    public CreateProjectPage setVcsRootName(String name) {
        showAdvancedOptions();
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
        showAdvancedOptions();
        vcsRootBranchInput.shouldBe(visible).setValue(branch);
        return this;
    }

    @Step("Clear VCS Root branch")
    public CreateProjectPage clearBranch() {
        showAdvancedOptions();
        vcsRootBranchInput.shouldBe(visible).setValue("");
        return this;
    }

    @Step("Click create VCS Root button")
    public CreateProjectPage clickCreate() {
        Boolean clicked = executeJavaScript(
                "const isVisible = (n) => {"
                        + "  const style = window.getComputedStyle(n);"
                        + "  return style && style.visibility !== 'hidden' && style.display !== 'none'"
                        + "    && n.getClientRects().length > 0;"
                        + "};"
                        + "const nodes = [...document.querySelectorAll("
                        + "  'button, [role=\"button\"], input[type=\"button\"], input[type=\"submit\"]'"
                        + ")];"
                        + "const matches = (n, label) => {"
                        + "  const text = (n.innerText || n.textContent || '').replace(/\\s+/g, ' ').trim();"
                        + "  const value = (n.value || '').replace(/\\s+/g, ' ').trim();"
                        + "  return text === label || value === label;"
                        + "};"
                        + "const createBtn = nodes.find(n => matches(n, 'Create') && isVisible(n));"
                        + "const saveBtn = nodes.find(n => matches(n, 'Save') && isVisible(n));"
                        + "const btn = createBtn || saveBtn;"
                        + "if (!btn) return false;"
                        + "btn.click();"
                        + "return true;"
        );
        if (!Boolean.TRUE.equals(clicked)) {
            var creates = $$x(
                    "//button[normalize-space()='Create']"
                            + " | //*[@role='button'][normalize-space()='Create']"
                            + " | //input[(@type='button' or @type='submit') and @value='Create']"
            ).filter(visible);
            if (!creates.isEmpty()) {
                creates.first().click();
                return this;
            }
            var saves = $$x(
                    "//button[normalize-space()='Save']"
                            + " | //*[@role='button'][normalize-space()='Save']"
                            + " | //input[(@type='button' or @type='submit') and @value='Save']"
            ).filter(visible);
            assertThat(saves.size())
                    .as("Visible Create/Save control should be present on VCS root form")
                    .isGreaterThan(0);
            saves.first().click();
        }
        return this;
    }

    @Step("Check error message appears")
    public CreateProjectPage shouldHaveError() {
        waitUntilPageSourceContainsAny("error", "Error", "failed", "Failed", "cannot", "Cannot", "Unable");
        if (errorVcsMessage.exists()) {
            errorVcsMessage.shouldBe(visible);
        } else if (errorMessage.exists()) {
            errorMessage.shouldBe(visible);
        } else {
            body.shouldHave(partialText("error").or(partialText("Error")).or(partialText("fail"))
                    .or(partialText("Unable")).or(partialText("cannot")));
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
