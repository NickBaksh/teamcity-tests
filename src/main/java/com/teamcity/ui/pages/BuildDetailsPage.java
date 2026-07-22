package com.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.teamcity.core.models.Build;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class BuildDetailsPage {

    private final SelenideElement title = $("h1");
    private final SelenideElement artifactsTab =
            $("[data-test='tab'][data-tab-title='Artifacts']");

    @Step("Build details page should be opened")
    public BuildDetailsPage shouldBeOpened() {
        title.shouldHave(text("Build #"));
        return this;
    }

    @Step("Build status should be '{status}'")
    public BuildDetailsPage shouldHaveStatus(String status) {
        $$("div")
                .findBy(Condition.exactText(status))
                .shouldBe(visible);
        return this;
    }

    @Step("Open build details page for build #{build.id}")
    public BuildDetailsPage openBuild(Build build) {
        open(build.getWebUrl());
        return shouldBeOpened();
    }

    @Step("Open Artifacts tab")
    public ArtifactsPage openArtifacts() {
        artifactsTab.shouldBe(visible).click();
        return new ArtifactsPage();
    }

}
