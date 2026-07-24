package com.teamcity.ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.Selenide.$x;

public class CreateProjectPage {

    private final SelenideElement nameInput = $("#name, input[name='name'], input[data-test='create-project-name']");
    private final SelenideElement idInput = $("#externalId, #id, input[name='externalId'], input[name='id']");
    private final SelenideElement createButton = $x(
            "//input[@value='Create'] | //button[contains(.,'Create')]"
    );
    private final SelenideElement errorMessage = $(
            ".error, .errorMessage, [data-test='error'], .ring-error-message, error"
    );
    private final SelenideElement vcsRootNameInput = $("[data-test='vcs-root-name-input'], #vcsRootName, [name='vcsRootName']");
    private final SelenideElement vcsRootUrlInput = $("[data-test='vcs-root-url-input'], #url, [name='url']");
    private final SelenideElement vcsRootBranchInput = $("[data-test='vcs-root-branch-input'], #branch, [name='branch']");
    private final SelenideElement vcsRootCreateButton = $("[data-test='create-vcs-root-button'], .saveButton");
    private final SelenideElement errorVcsMessage = $("[data-test='error-message'], .error, .field-error");

    @Step("Open create project page under Root")
    public CreateProjectPage openPage() {
        open("/admin/createProject.html?projectId=_Root");
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
        return source.contains("emptyProjectName")
                || source.contains("duplicateProjectId")
                || source.contains("Project name is empty")
                || source.contains("already used by another project")
                || (errorMessage.exists() && errorMessage.is(visible));
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
        // Проверяем, что виден хотя бы один ключевой элемент страницы
        vcsRootNameInput.shouldBe(visible);
        return this;
    }

    @Step("Open VCS Root creation page for project: {projectId}")
    public CreateProjectPage openVcsRootCreation(String projectId) {
        open("/admin/createVcsRoot.html?projectId=" + projectId);
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
        errorMessage.shouldBe(visible);
        return this;
    }

    @Step("Check error message appears: {expectedText}")
    public CreateProjectPage shouldHaveError(String expectedText) {
        errorMessage.shouldBe(visible)
                .shouldHave(text(expectedText));
        return this;
    }
}
