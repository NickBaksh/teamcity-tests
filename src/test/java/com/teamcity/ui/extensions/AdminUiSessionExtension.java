package com.teamcity.ui.extensions;

import com.teamcity.core.config.ConfigManager;
import com.teamcity.ui.config.SelenideConfig;
import com.teamcity.ui.pages.LoginPage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AdminUiSessionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SelenideConfig.apply();
        new LoginPage()
                .openPage()
                .loginSuccessfully(ConfigManager.getAdminLogin(), ConfigManager.getAdminPassword());
    }
}
