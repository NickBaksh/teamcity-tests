package com.teamcity.ui;

import com.codeborne.selenide.Selenide;
import com.teamcity.api.BaseApiTest;
import com.teamcity.ui.config.SelenideConfig;
import com.teamcity.ui.pages.*;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Tag("ui")
@Execution(ExecutionMode.SAME_THREAD)
public abstract class BaseUiTest extends BaseApiTest {

    protected final LoginPage loginPage = new LoginPage();
    protected final ProjectsPage projectsPage = new ProjectsPage();
    protected final CreateProjectPage createProjectPage = new CreateProjectPage();
    protected final ProjectPage projectPage = new ProjectPage();
    protected final BuildConfigPage buildConfigPage = new BuildConfigPage();
    protected final AgentsPage agentsPage = new AgentsPage();
    protected final BuildDetailsPage buildDetailsPage = new BuildDetailsPage();

    @BeforeEach
    @Step("Initialize UI test environment")
    public void setUpUi() {
        SelenideConfig.apply();
    }

    @AfterEach
    @Step("Close browser after UI test")
    public void tearDownUi() {
        Selenide.closeWebDriver();
    }
}
