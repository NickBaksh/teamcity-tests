package com.teamcity.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.$x;

public class ProjectsPage {

    private final SelenideElement searchInput = $(
            "input[placeholder*='Search'], input[aria-label*='Search'], #search-projects, .search-field input"
    );
    private final SelenideElement createProjectButton = $x(
            "//a[contains(.,'Create project') or contains(.,'New project')] | //button[contains(.,'Create project')]"
    );
    private final ElementsCollection projectLinks = $$("a[href*='/project/'], .projectName a, [data-test='project-link']");

    @Step("Open projects overview")
    public ProjectsPage openPage() {
        open(UiRoutes.OVERVIEW);
        return this;
    }

    @Step("Open create project form")
    public CreateProjectPage openCreateProject() {
        createProjectButton.shouldBe(visible).click();
        return new CreateProjectPage();
    }

    @Step("Search projects by name: {name}")
    public ProjectsPage search(String name) {
        searchInput.shouldBe(visible).setValue(name);
        return this;
    }

    @Step("Check project is visible in list: {name}")
    public ProjectsPage shouldContainProject(String name) {
        $x("//a[contains(.,'" + name + "')] | //*[contains(@class,'project') and contains(.,'" + name + "')]")
                .shouldBe(visible)
                .shouldHave(text(name));
        return this;
    }

    @Step("Check project is not visible in list: {name}")
    public ProjectsPage shouldNotContainProject(String name) {
        $x("//a[contains(.,'" + name + "')]").shouldNotBe(visible);
        return this;
    }

    @Step("Open project by name: {name}")
    public ProjectPage openProject(String name) {
        $x("//a[contains(.,'" + name + "')]").shouldBe(visible).click();
        return new ProjectPage();
    }

    @Step("Get visible projects count")
    public int visibleProjectsCount() {
        return projectLinks.filter(visible).size();
    }
}
