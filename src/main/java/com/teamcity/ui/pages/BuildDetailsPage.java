package com.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.teamcity.core.models.Build;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class BuildDetailsPage {

    private final SelenideElement title = $("h1");
    private final SelenideElement artifactsTab =
            $("[data-test='tab'][data-tab-title='Artifacts']");
    private final SelenideElement buildNumberElement = $("[data-test='build-number']");
    private final SelenideElement buildStatusElement = $("[data-test='build-status']");
    private final SelenideElement buildStatus = $("[data-test='build-status']");

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

    @Step("Wait for build to finish")
    public BuildDetailsPage waitForBuildFinished() {
        buildStatus.shouldHave(text("Finished"), Duration.ofMinutes(3));
        return this;
    }

    @Step("Get build number from UI")
    public String getBuildNumber() {
        return buildNumberElement.shouldBe(visible).getText();
    }

    @Step("Get build status from UI")
    public String getBuildStatus() {
        return buildStatusElement.shouldBe(visible).getText();
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
