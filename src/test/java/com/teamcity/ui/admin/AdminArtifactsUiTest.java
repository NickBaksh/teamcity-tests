package com.teamcity.ui.admin;

import com.teamcity.core.models.Build;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.AdminUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Feature("UI Artifact Management")
@Tag("ui")
@Tag("admin")
@ExtendWith(AdminUiSessionExtension.class)
public class AdminArtifactsUiTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCanViewBuildArtifactsTest() {
        Build finished = givenFinishedNBankBuild();

        buildDetailsPage
                .openBuild(finished)
                .openArtifacts()
                .shouldBeOpened()
                .shouldHaveArtifacts();
    }

}
