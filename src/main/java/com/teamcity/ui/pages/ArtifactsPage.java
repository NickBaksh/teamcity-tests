package com.teamcity.ui.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

public class ArtifactsPage {
    private final SelenideElement totalSize = $$("div").findBy(text("Total size"));
    private final ElementsCollection artifacts = $$("li[role='treeitem']");

    @Step("Artifacts page should be opened")
    public ArtifactsPage shouldBeOpened() {
        totalSize.shouldBe(visible);
        return this;
    }

    @Step("Artifacts should be displayed")
    public ArtifactsPage shouldHaveArtifacts() {
        artifacts.shouldHave(CollectionCondition.sizeGreaterThan(0));
        return this;
    }
}