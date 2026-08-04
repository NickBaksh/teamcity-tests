package com.teamcity.ui.pages;

import com.codeborne.selenide.ClickOptions;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class AgentsPage {
    private final ElementsCollection agents = $$("[data-test='agent']");
    private final SelenideElement allAgentsTab =
            $("[data-test='ring-link'][data-test-selected='true']");

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
        agents.findBy(text(agentName))
                .$("[data-test='ring-toggle']")
                .shouldBe(visible)
                .click();
        return this;
    }

    @Step("Confirm disabling agent")
    public AgentsPage confirmDisableAgent() {
        SelenideElement confirm = $$("button")
                .filter(visible)
                .findBy(text("Disable"))
                .shouldBe(visible, enabled);
        confirm.scrollIntoView("{block: 'center'}")
                .click(ClickOptions.usingJavaScript());
        confirm.should(disappear);
        return this;
    }
}
