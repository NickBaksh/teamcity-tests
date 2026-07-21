package com.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class BuildDetailsPage {
    private final SelenideElement title = $("h1");

    public BuildDetailsPage shouldBeOpened() {
        title.shouldHave(text("Build #"));
        return this;
    }

    public BuildDetailsPage shouldHaveStatus(String status) {
        $$("div")
                .findBy(Condition.exactText(status))
                .shouldBe(visible);
        return this;
    }

}
