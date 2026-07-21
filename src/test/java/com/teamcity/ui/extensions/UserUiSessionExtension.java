package com.teamcity.ui.extensions;

import com.teamcity.core.client.ClientFactory;
import com.teamcity.core.generators.RandomData;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.UserSteps;
import com.teamcity.core.testdata.TestDataValues;
import com.teamcity.core.utils.TestDataFactory;
import com.teamcity.ui.config.SelenideConfig;
import com.teamcity.ui.pages.LoginPage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static com.teamcity.core.generators.RandomModelGenerator.generate;

public class UserUiSessionExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(UserUiSessionExtension.class);
    private static final String USER_KEY = "uiUser";
    private static final String USER_STEPS_KEY = "userSteps";

    @Override
    public void beforeEach(ExtensionContext context) {
        SelenideConfig.apply();

        UserSteps userSteps = new UserSteps(ClientFactory.createAdminClient());
        User request = generate(User.class);
        request.setId(null);
        request.setHref(null);
        request.setPassword(TestDataFactory.DEFAULT_PASSWORD);
        request.setName(TestDataValues.USER_NAME_PREFIX + RandomData.shortId());

        User created = userSteps.createUser(request);
        created.setPassword(request.getPassword());

        context.getStore(NAMESPACE).put(USER_KEY, created);
        context.getStore(NAMESPACE).put(USER_STEPS_KEY, userSteps);

        new LoginPage()
                .openPage()
                .loginSuccessfully(created.getUsername(), created.getPassword());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        User user = context.getStore(NAMESPACE).get(USER_KEY, User.class);
        UserSteps userSteps = context.getStore(NAMESPACE).get(USER_STEPS_KEY, UserSteps.class);
        if (user != null && userSteps != null) {
            try {
                userSteps.deleteUserIfExists(user.getUsername());
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(User.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(USER_KEY, User.class);
    }
}
