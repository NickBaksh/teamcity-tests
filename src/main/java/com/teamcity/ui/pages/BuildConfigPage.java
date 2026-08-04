package com.teamcity.ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.teamcity.core.config.ConfigManager;
import com.teamcity.ui.pages.elements.ConfirmDialog;
import com.teamcity.ui.testdata.UiTestData;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.appear;
import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.partialText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

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

    private final SelenideElement commandLineRunner = $("[data-key='simpleRunner']");
    private final SelenideElement commandLineByText = $x(
            "//*[contains(@class,'BuildStepSelectorItem') and .//span[contains(.,'"
                    + UiTestData.MARKER_COMMAND_LINE + "')]]"
    );
    private final SelenideElement stepNameInput = $("#buildStepName");
    private final SelenideElement scriptContent = $("#script\\.content, textarea[name='prop:script.content']");
    private final SelenideElement saveStepButton = $("input[name='save'], input[name='submitButton'].submitButton");
    private final SelenideElement title = $("h1, .buildTypeName, [data-test='build-config-title']");
    private final SelenideElement body = $("body");
    private final SelenideElement visibleError = $(".error, .errorMessage, [data-test='error'], error");
    private final SelenideElement overviewHeader = $("[data-test='overview-header']");
    private final ConfirmDialog confirmDialog = new ConfirmDialog();

    private final SelenideElement runBuildButton = $("[data-test='run-build']");
    private final SelenideElement buildStatus = $("[data-test='build-status']");
    private final SelenideElement notificationPopup =
            $("[data-test='ring-popup']");

    private final SelenideElement buildActionsButton =
            $("[data-test-title='Actions']")
                    .parent()
                    .$("button");
    private final SelenideElement buildQueueIndicator = $("[data-test='build-queue']");
    private final SelenideElement buildStatusText = $("[data-test='build-status']");

    @Step("Open create build config wizard for project: {projectId}")
    public BuildConfigPage openCreate(String projectId) {
        open(UiRoutes.createBuildConfig(projectId));
        setupPage.shouldBe(visible);
        return this;
    }

    @Step("Open classic create build config form for project: {projectId}")
    public BuildConfigPage openClassicCreate(String projectId) {
        open(UiRoutes.classicCreateBuildType(projectId));
        classicNameInput.shouldBe(visible);
        return this;
    }

    @Step("Open build config by id: {buildConfigId}")
    public BuildConfigPage openById(String buildConfigId) {
        open(UiRoutes.buildConfiguration(buildConfigId));
        return this;
    }

    @Step("Open edit build config general: {buildConfigId}")
    public BuildConfigPage openEdit(String buildConfigId) {
        open(UiRoutes.editBuild(buildConfigId));
        return this;
    }

    @Step("Open build steps page: {buildConfigId}")
    public BuildConfigPage openSteps(String buildConfigId) {
        open(UiRoutes.editBuildRunners(buildConfigId));
        return this;
    }

    @Step("Create build config via Sakura setup wizard name={name}")
    public BuildConfigPage create(String name) {
        setupPage.shouldBe(visible);
        SelenideElement nameField = setupNameInput;
        if (!nameField.exists() || !nameField.is(visible)) {
            nameField = $$("[data-test='setup-project-page'] div[data-test='ring-input'] input").get(1);
        }
        nameField.shouldBe(visible).setValue(name == null ? "" : name);
        setupCreateButton.shouldBe(visible).click();
        setupPage.should(disappear);
        return this;
    }

    @Step("Create build config via classic form expecting error")
    public BuildConfigPage createExpectingError(String name) {
        classicNameInput.shouldBe(visible).setValue(name == null ? "" : name);
        if (classicIdInput.exists() && classicIdInput.is(visible)) {
            classicIdInput.clear();
        }
        classicCreateButton.shouldBe(visible).click();
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

    @Step("Check error message appears: {expectedError}")
    public BuildConfigPage shouldHaveError(String expectedError) {
        $("[data-test='error-message'], .error")
                .shouldBe(visible)
                .shouldHave(text(expectedError));
        return this;
    }

    @Step("Check error message appears")
    public BuildConfigPage shouldHaveError() {
        $("[data-test='error-message'], .error")
                .shouldBe(visible);
        return this;
    }

    @Step("Get error text / page source for assertions")
    public String errorText() {
        SelenideElement error = $(".error, .errorMessage, [data-test='error'], error");
        if (error.exists() && error.is(visible)) {
            return error.getText();
        }
        return body.getText();
    }

    @Step("Assert empty build config name validation error")
    public BuildConfigPage shouldShowEmptyNameError() {
        if (visibleError.exists()) {
            visibleError.shouldBe(visible);
        } else {
            body.shouldHave(partialText(UiTestData.ERROR_EMPTY).or(partialText("<error")));
        }
        return this;
    }

    @Step("Add simple command-line build step")
    public BuildConfigPage addCommandLineStep(String buildConfigId, String stepName) {
        open(UiRoutes.editRunTypeNew(buildConfigId));
        ensureEditRunTypeOpened(buildConfigId);
        selectCommandLineRunner(buildConfigId);
        stepNameInput.should(appear);
        enableCustomScriptOption();
        if (stepNameInput.is(visible)) {
            stepNameInput.setValue(stepName);
        }
        fillCommandLineScript(UiTestData.COMMAND_LINE_SCRIPT);
        saveBuildStep();
        webdriver().shouldHave(urlContaining("editBuildRunners"));
        return this;
    }

    private void ensureEditRunTypeOpened(String buildConfigId) {
        followClassicXmlRedirectIfPresent();
        if (!WebDriverRunner.url().contains("editRunType")) {
            open(UiRoutes.editRunTypeNew(buildConfigId));
            followClassicXmlRedirectIfPresent();
        }
        webdriver().shouldHave(urlContaining("editRunType"));
        waitForRunnerSelector();
    }

    private void followClassicXmlRedirectIfPresent() {
        String source = WebDriverRunner.source();
        if (source == null || !source.contains("<redirect>")) {
            return;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("<redirect>([^<]+)</redirect>")
                .matcher(source);
        if (matcher.find()) {
            open(UiUrls.toRelative(matcher.group(1).trim()));
        }
    }

    @Step("Assert build steps page reflects added step: {stepName}")
    public BuildConfigPage shouldReflectAddedStep(String stepName) {
        $x("//*[contains(.,'" + stepName + "') or contains(.,'" + UiTestData.MARKER_COMMAND_LINE + "')]")
                .shouldBe(visible)
                .shouldHave(text(stepName).or(partialText(UiTestData.MARKER_COMMAND_LINE)));
        return this;
    }

    private void selectCommandLineRunner(String buildConfigId) {
        if (commandLineRunner.exists()) {
            commandLineRunner.shouldBe(visible).click();
        } else if (commandLineByText.exists()) {
            commandLineByText.shouldBe(visible).click();
        } else {
            open(UiRoutes.editRunTypeNew(buildConfigId));
            waitForRunnerSelector();
            if (commandLineRunner.exists()) {
                commandLineRunner.click();
            } else if (commandLineByText.exists()) {
                commandLineByText.click();
            }
        }
    }

    private void enableCustomScriptOption() {
        SelenideElement useCustomScript = $("#use\\.custom\\.script, select[name='prop:use.custom.script']");
        if (!useCustomScript.exists()) {
            return;
        }
        try {
            useCustomScript.selectOptionContainingText(UiTestData.CUSTOM_SCRIPT_OPTION);
        } catch (Exception ignored) {
            executeJavaScript(
                    "arguments[0].value='true';"
                            + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                    useCustomScript
            );
        }
    }

    private void fillCommandLineScript(String script) {
        if (scriptContent.exists()) {
            executeJavaScript(
                    "arguments[0].removeAttribute('readonly');"
                            + "arguments[0].removeAttribute('disabled');"
                            + "arguments[0].value = arguments[1];"
                            + "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));"
                            + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                    scriptContent,
                    script
            );
        }
        SelenideElement codeMirror = $(".CodeMirror");
        if (codeMirror.exists()) {
            codeMirror.should(appear);
            executeJavaScript(
                    "if (arguments[0].CodeMirror) { arguments[0].CodeMirror.setValue(arguments[1]); }",
                    codeMirror,
                    script
            );
        }
    }

    private void saveBuildStep() {
        if (saveStepButton.exists()) {
            saveStepButton.shouldBe(visible).click();
        } else {
            $x("//input[@value='Save'] | //button[contains(.,'Save')]").shouldBe(visible).click();
        }
    }

    private void waitForRunnerSelector() {
        SelenideElement anyRunner = $x(
                "//*[@data-key='simpleRunner']"
                        + " | //*[contains(@class,'BuildStepSelectorItem')]"
                        + " | //*[@data-test='build-step-selector-item']"
                        + " | //*[contains(normalize-space(.),'" + UiTestData.MARKER_COMMAND_LINE + "')]"
        );
        anyRunner.should(appear);
    }

    @Step("Check build config title contains: {name}")
    public BuildConfigPage shouldHaveName(String name) {
        title.shouldBe(visible).shouldHave(text(name));
        return this;
    }

    @Step("Run build")
    public BuildConfigPage runBuild() {
        runBuildButton.click();
        return this;
    }

    @Step("Check build configuration page is opened")
    public BuildConfigPage shouldBeOpened() {
        overviewHeader.shouldBe(visible);
        return this;
    }

    @Step("Wait until build is finished")
    public BuildConfigPage waitForBuildFinished() {
        notificationPopup.shouldHave(
                text("is finished"),
                Duration.ofSeconds(ConfigManager.getBuildTimeout()));
        return this;
    }

    @Step("Open latest build details")
    public BuildDetailsPage openLatestBuild() {
        $$("[data-test-build-number-link]")
                .first()
                .click();
        return new BuildDetailsPage();
    }

    @Step("Open build actions menu")
    public BuildConfigPage openBuildActionsMenu() {
        buildActionsButton.click();
        return this;
    }

    @Step("Check 'Remove' action is not available")
    public BuildConfigPage shouldNotHaveRemoveBuildAction() {
        $$(".ring-list-label")
                .findBy(text("Remove"))
                .shouldNot(exist);
        return this;
    }

    @Step("Check page contains 'Not found' text")
    public BuildConfigPage shouldContainNotFound() {
        body.shouldHave(partialText("Not found").or(partialText("does not exist")));
        return this;
    }

    @Step("Check build is in queue")
    public BuildConfigPage shouldHaveBuildInQueue() {
        buildQueueIndicator.shouldBe(visible)
                .shouldHave(text("queued"));
        return this;
    }

    @Step("Check build is running")
    public BuildConfigPage shouldHaveBuildRunning() {
        buildQueueIndicator.shouldBe(visible)
                .shouldHave(text("running"));
        return this;
    }

    @Step("Wait for build to start")
    public BuildConfigPage waitForBuildToStart() {
        buildQueueIndicator.shouldHave(text("running"), Duration.ofSeconds(30));
        return this;
    }

    @Step("Get current build status text")
    public String getBuildStatusText() {
        return buildStatusText.shouldBe(visible).getText();
    }
}
