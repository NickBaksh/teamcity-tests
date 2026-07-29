package com.teamcity.ui.admin;

import com.teamcity.core.models.Project;
import com.teamcity.core.testdata.TestDataValues;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("UI Artifact Management")
@Tag("ui")
@Tag("admin")
@ExtendWith(AdminUiSessionExtension.class)
public class AdminVcsRootUiTest extends BaseUiTest {
    private String projectId;

    @BeforeEach
    void prepareProject() {
        Project project = givenProject();
        projectId = project.getId();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCannotCreateVcsRootWithInvalidUrl() {
        String invalidUrl = TestDataValues.VCS_ROOT_URL;
        String vcsRootName = TestDataValues.VCS_ROOT_RANDOM_NAME;

        createProjectPage
                .openVcsRootCreation(projectId)
                .shouldBeOpened()
                .setVcsRootName(vcsRootName)
                .setVcsRootUrl(invalidUrl)
                .clickCreate()
                .shouldHaveError();

        assertThat(vcsRootSteps.getVcsRootsByProject(projectId))
                .as("VCS Root should not be created with invalid URL")
                .noneMatch(vcsRoot -> vcsRootName.equals(vcsRoot.getName()));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCannotCreateVcsRootWithoutBranch() {
        String vcsRootName = "VCS Without Branch";

        createProjectPage
                .openVcsRootCreation(projectId)
                .shouldBeOpened()
                .setVcsRootName(vcsRootName)
                .setVcsRootUrl(TestDataValues.VCS_ROOT_URL)
                .clearBranch()
                .clickCreate()
                .shouldHaveError();

        assertThat(vcsRootSteps.getVcsRootsByProject(projectId))
                .as("VCS Root should not be created without branch")
                .noneMatch(vcsRoot -> vcsRootName.equals(vcsRoot.getName()));
    }
}
