package com.teamcity.ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.teamcity.ui.pages.elements.ConfirmDialog;
import io.qameta.allure.Step;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.Selenide.$x;

public class BuildConfigPage {

    private final SelenideElement setupPage = $("[data-test='setup-project-page']");
    private final SelenideElement setupNameInput = $(
            "[data-test='setup-project-page'] div[data-test='ring-input'] input:not([placeholder])"
    );
    private final SelenideElement setupCreateButton = $x(
            "//div[@data-test='setup-project-page']//button[normalize-space()='Create']"
    );

    private final SelenideElement classicNameInput = $("#buildTypeName");
    private final SelenideElement classicIdInput = $("#buildTypeExternalId");
    private final SelenideElement classicCreateButton = $("input[name='createBuildType']");

    private final SelenideElement addStepButton = $(
            "a[href*='editRunType'][href*='__NEW_RUNNER__'], a[href*='editRunType.html']"
    );
    private final SelenideElement addStepByText = $x(
            "//a[.//span[contains(.,'Add build step')] or contains(.,'Add build step')]"
    );
    private final SelenideElement commandLineRunner = $("[data-key='simpleRunner']");
    private final SelenideElement commandLineByText = $x(
            "//*[contains(@class,'BuildStepSelectorItem') and .//span[contains(.,'Command Line')]]"
    );
    private final SelenideElement stepNameInput = $("#buildStepName");
    private final SelenideElement scriptContent = $("#script\\.content, textarea[name='prop:script.content']");
    private final SelenideElement saveStepButton = $("input[name='save'], input[name='submitButton'].submitButton");
    private final SelenideElement title = $("h1, .buildTypeName, [data-test='build-config-title']");
    private final ConfirmDialog confirmDialog = new ConfirmDialog();

    @Step("Open create build config wizard for project: {projectId}")
    public BuildConfigPage openCreate(String projectId) {
        open("/projects/create?projectId=" + projectId + "&setup=build");
        setupPage.shouldBe(visible);
        return this;
    }

    @Step("Open classic create build config form for project: {projectId}")
    public BuildConfigPage openClassicCreate(String projectId) {
        open("/admin/createBuildType.html?projectId=" + projectId);
        classicNameInput.shouldBe(visible);
        return this;
    }

    @Step("Open build config by id: {buildConfigId}")
    public BuildConfigPage openById(String buildConfigId) {
        open("/buildConfiguration/" + buildConfigId);
        return this;
    }

    @Step("Open edit build config general: {buildConfigId}")
    public BuildConfigPage openEdit(String buildConfigId) {
        open("/admin/editBuild.html?id=buildType:" + buildConfigId);
        return this;
    }

    @Step("Open build steps page: {buildConfigId}")
    public BuildConfigPage openSteps(String buildConfigId) {
        open("/admin/editBuildRunners.html?id=buildType:" + buildConfigId);
        return this;
    }

    @Step("Create build config via Sakura setup wizard name={name}")
    public BuildConfigPage create(String name, String id) {
        setupPage.shouldBe(visible);
        SelenideElement nameField = setupNameInput;
        if (!nameField.exists() || !nameField.is(visible)) {
            nameField = $$("[data-test='setup-project-page'] div[data-test='ring-input'] input").get(1);
        }
        nameField.shouldBe(visible).setValue(name == null ? "" : name);
        if (id != null) {
            SelenideElement showMore = $x(
                    "//div[@data-test='setup-project-page']//button[contains(.,'Show more')]"
            );
            if (showMore.exists() && showMore.is(visible)) {
                showMore.click();
                sleep(500);
            }
            SelenideElement idField = $x(
                    "//div[@data-test='setup-project-page']"
                            + "//label[contains(.,'ID') or contains(.,'Id')]/following::input[1]"
            );
            if (idField.exists() && idField.is(visible)) {
                idField.clear();
                idField.setValue(id);
            }
        }
        setupCreateButton.shouldBe(visible).click();
        sleep(3000);
        return this;
    }

    @Step("Read created build config id from page/url/source")
    public String readCreatedBuildConfigId() {
        String url = WebDriverRunner.url();
        String decoded = url.replace("%3A", ":").replace("%3a", ":");
        Matcher matcher = Pattern.compile("buildType:([A-Za-z0-9_]+)").matcher(decoded);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = Pattern.compile("/buildConfiguration/([A-Za-z0-9_]+)").matcher(decoded);
        if (matcher.find()) {
            return matcher.group(1);
        }

        String source = WebDriverRunner.source();
        matcher = Pattern.compile("buildType:([A-Za-z0-9_]+)").matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Step("Create build config via classic form expecting error")
    public BuildConfigPage createExpectingError(String name) {
        classicNameInput.shouldBe(visible).setValue(name == null ? "" : name);
        if (classicIdInput.exists() && classicIdInput.is(visible)) {
            classicIdInput.clear();
        }
        classicCreateButton.shouldBe(visible).click();
        sleep(1000);
        return this;
    }

    @Step("Check validation error is present")
    public boolean hasValidationError() {
        String source = WebDriverRunner.source();
        return source.contains("empty")
                || source.contains("Error")
                || source.contains("<error")
                || $(".error, .errorMessage, error").exists();
    }

    @Step("Get error text / page source for assertions")
    public String errorText() {
        SelenideElement error = $(".error, .errorMessage, [data-test='error'], error");
        if (error.exists() && error.is(visible)) {
            return error.getText();
        }
        return WebDriverRunner.source();
    }

    @Step("Add simple command-line build step")
    public BuildConfigPage addCommandLineStep(String buildConfigId, String stepName) {
        openSteps(buildConfigId);
        if (addStepButton.exists()) {
            addStepButton.shouldBe(visible).click();
        } else {
            addStepByText.shouldBe(visible).click();
        }
        sleep(1500);

        if (commandLineRunner.exists()) {
            commandLineRunner.shouldBe(visible).click();
        } else if (commandLineByText.exists()) {
            commandLineByText.shouldBe(visible).click();
        } else {
            open("/admin/editRunType.html?id=buildType:" + buildConfigId
                    + "&runnerId=__NEW_RUNNER__&init=1");
            sleep(1000);
            if (commandLineRunner.exists()) {
                commandLineRunner.click();
            } else if (commandLineByText.exists()) {
                commandLineByText.click();
            }
        }
        sleep(1500);

        SelenideElement useCustomScript = $("#use\\.custom\\.script, select[name='prop:use.custom.script']");
        if (useCustomScript.exists()) {
            try {
                useCustomScript.selectOptionContainingText("Custom script");
            } catch (Exception ignored) {
                executeJavaScript(
                        "arguments[0].value='true';"
                                + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                        useCustomScript
                );
            }
            sleep(500);
        }

        if (stepNameInput.exists() && stepNameInput.is(visible)) {
            stepNameInput.setValue(stepName);
        }

        if (scriptContent.exists()) {
            executeJavaScript(
                    "arguments[0].removeAttribute('readonly');"
                            + "arguments[0].removeAttribute('disabled');"
                            + "arguments[0].value = arguments[1];"
                            + "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));"
                            + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                    scriptContent,
                    "echo hello"
            );
        }
        SelenideElement codeMirror = $(".CodeMirror");
        if (codeMirror.exists()) {
            executeJavaScript(
                    "if (arguments[0].CodeMirror) { arguments[0].CodeMirror.setValue(arguments[1]); }",
                    codeMirror,
                    "echo hello"
            );
        }

        if (saveStepButton.exists()) {
            saveStepButton.shouldBe(visible).click();
        } else {
            $x("//input[@value='Save'] | //button[contains(.,'Save')]").shouldBe(visible).click();
        }
        sleep(2000);
        return this;
    }

    @Step("Pause build configuration via UI: {buildConfigId}")
    public BuildConfigPage pause(String buildConfigId) {
        openEdit(buildConfigId);
        submitPauseBuildTypeForm(buildConfigId, true);
        sleep(1500);
        return this;
    }

    @Step("Resume/activate build configuration via UI: {buildConfigId}")
    public BuildConfigPage resume(String buildConfigId) {
        openEdit(buildConfigId);
        submitPauseBuildTypeForm(buildConfigId, false);
        sleep(1500);
        return this;
    }

    private void submitPauseBuildTypeForm(String buildConfigId, boolean pause) {
        Boolean submitted = executeJavaScript(
                "try {"
                        + "  var form = document.getElementById('pauseBuildTypeForm');"
                        + "  if (!form) {"
                        + "    if (window.BS && BS.PauseBuildTypeDialog && BS.PauseBuildTypeDialog.showPauseBuildTypeDialog) {"
                        + "      BS.PauseBuildTypeDialog.showPauseBuildTypeDialog(arguments[0]);"
                        + "      form = document.getElementById('pauseBuildTypeForm');"
                        + "    }"
                        + "  }"
                        + "  if (!form) return false;"
                        + "  var comment = form.querySelector('[name=pauseComment]');"
                        + "  if (comment) comment.value = arguments[1] ? 'paused by ui test' : 'activated by ui test';"
                        + "  if (window.BS && BS.PauseBuildTypeForm && BS.PauseBuildTypeForm.submit) {"
                        + "    BS.PauseBuildTypeForm.submit(); return true;"
                        + "  }"
                        + "  form.submit(); return true;"
                        + "} catch (e) { return false; }",
                buildConfigId,
                pause
        );
        if (!Boolean.TRUE.equals(submitted)) {
            SelenideElement button = $x(pause
                    ? "//input[@value='Pause'] | //button[contains(.,'Pause')]"
                    : "//input[@value='Activate'] | //button[contains(.,'Activate')]");
            if (button.exists()) {
                executeJavaScript("arguments[0].click();", button);
            }
        }
    }

    @Step("Check build config title contains: {name}")
    public BuildConfigPage shouldHaveName(String name) {
        title.shouldBe(visible).shouldHave(com.codeborne.selenide.Condition.text(name));
        return this;
    }
}
