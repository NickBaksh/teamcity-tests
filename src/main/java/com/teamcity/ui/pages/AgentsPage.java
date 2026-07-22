package com.teamcity.ui.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class AgentsPage {
    private final SelenideElement pageTitle = $("h1");
    private final ElementsCollection agents = $$("[data-test='agent']");
    private final SelenideElement allAgentsTab =
            $("[data-test='ring-link'][data-test-selected='true']");
    private final SelenideElement disableButton = $$("button").findBy(text("Disable"));

    @Step("Open agents page")
    public AgentsPage openPage() {
        open("/agents/overview");
        return this;
    }

    @Step("Check agents page is opened")
    public AgentsPage shouldBeOpened() {
        allAgentsTab.shouldHave(text("All Agents"));
        return this;
    }

    @Step("Check agents are displayed")
    public AgentsPage shouldHaveAgents() {
        agents.shouldBe(CollectionCondition.sizeGreaterThan(0));
        return this;
    }

    @Step("Get displayed agents count")
    public int getAgentsCount() {
        return agents.size();
    }

    @Step("Disable agent {agentName}")
    public AgentsPage disableAgent(String agentName) {
        $$("[data-test='agent']")
                .findBy(text(agentName))
                .$("[data-test='ring-toggle']")
                .click();

        return this;
    }

    @Step("Confirm disabling agent")
    public AgentsPage confirmDisableAgent() {
        disableButton.shouldBe(enabled)
                .click();
        disableButton.should(disappear);

        return this;
    }
}
