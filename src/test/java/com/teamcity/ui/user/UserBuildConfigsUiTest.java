package com.teamcity.ui.user;

import com.teamcity.core.models.BuildConfig;
import com.teamcity.core.models.User;
import com.teamcity.ui.BaseUiTest;
import com.teamcity.ui.extensions.UserUiSessionExtension;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Feature("UI Build Configuration Management")
@Tag("ui")
@Tag("user")
@ExtendWith(UserUiSessionExtension.class)
public class UserBuildConfigsUiTest extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.NORMAL)
    void userCanViewBuildConfiguration(User user) {
        BuildConfig buildConfig = givenBuildConfig();

        buildConfigPage
                .openById(buildConfig.getId())
                .shouldBeOpened()
                .shouldHaveName(buildConfig.getName());
    }

}
