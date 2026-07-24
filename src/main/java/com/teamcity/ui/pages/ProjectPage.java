package com.teamcity.ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.teamcity.ui.pages.elements.ConfirmDialog;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.$x;

public class ProjectPage {

    private final SelenideElement actionsTrigger = $(
            "[id^='sp_span_prjActions'], .action-links a, a.actionsLink"
    );
    private final SelenideElement deleteProjectLink = $(
            "a[title='Delete project'], a[onclick*='deleteProject']"
    );
    private final SelenideElement createBuildConfigButton = $x(
            "//a[contains(.,'Create build configuration')] | //button[contains(.,'Create build configuration')]"
    );
    private final SelenideElement projectTitle = $("h1, .projectName, [data-test='project-title']");
    private final ConfirmDialog confirmDialog = new ConfirmDialog();

    @Step("Open project by id: {projectId}")
    public ProjectPage openById(String projectId) {
        open(UiRoutes.project(projectId));
        return this;
    }

    @Step("Open project edit settings: {projectId}")
    public ProjectPage openEdit(String projectId) {
        open(UiRoutes.editProject(projectId));
        return this;
    }

    @Step("Delete project via UI Actions menu: {projectId}")
    public void deleteProject(String projectId) {
        openEdit(projectId);
        executeJavaScript("window.confirm = function () { return true; };");
        Boolean invoked = executeJavaScript(
                "try {"
                        + "  if (window.BS && BS.AdminActions && BS.AdminActions.deleteProject) {"
                        + "    BS.AdminActions.deleteProject(arguments[0], arguments[0]);"
                        + "    return true;"
                        + "  }"
                        + "  return false;"
                        + "} catch (e) { return false; }",
                projectId
        );
        if (!Boolean.TRUE.equals(invoked)) {
            openActionsMenu(projectId);
            deleteProjectLink.should(exist);
            executeJavaScript("arguments[0].click();", deleteProjectLink);
        }
        SelenideElement deleteSubmit = $(
                "#deleteProjectForm input.submitButton, #deleteProjectForm input[type='submit'], "
                        + "input[value='Delete'], button[type='submit']"
        );
        if (deleteSubmit.exists()) {
            deleteSubmit.shouldBe(visible).click();
        } else if (confirmDialog.isVisible()) {
            confirmDialog.confirm();
        }
    }

    @Step("Open create build configuration")
    public BuildConfigPage openCreateBuildConfig() {
        if (createBuildConfigButton.exists()) {
            createBuildConfigButton.shouldBe(visible).click();
        } else {
            String url = WebDriverRunner.url();
            String projectId = url.contains("projectId=")
                    ? url.substring(url.indexOf("projectId=") + "projectId=".length()).split("&")[0]
                    : "";
            open(UiRoutes.classicCreateBuildType(projectId));
        }
        return new BuildConfigPage();
    }

    @Step("Check project title contains: {name}")
    public ProjectPage shouldHaveName(String name) {
        projectTitle.shouldBe(visible).shouldHave(com.codeborne.selenide.Condition.text(name));
        return this;
    }

    @Step("Check create project action is not available")
    public boolean isCreateProjectAvailable() {
        return $x("//a[contains(.,'Create project')] | //button[contains(.,'Create project')]").exists();
    }

    private void openActionsMenu(String projectId) {
        SelenideElement byId = $("#sp_span_prjActions" + projectId);
        if (byId.exists()) {
            byId.click();
            return;
        }
        if (actionsTrigger.exists()) {
            actionsTrigger.click();
            return;
        }
        executeJavaScript(
                "if (window.BS && BS.AdminActions && BS.AdminActions.deleteProject) { return true; }"
        );
        if (!deleteProjectLink.exists()) {
            $x("//a[contains(.,'Actions')] | //span[contains(.,'Actions')]").click();
        }
        WebDriverRunner.getWebDriver();
    }
}
