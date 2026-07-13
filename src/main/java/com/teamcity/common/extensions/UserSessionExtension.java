package com.teamcity.common.extensions;

import com.teamcity.BaseTest;
import com.teamcity.api.requests.steps.TestUserContext;
import com.teamcity.api.requests.steps.UserSteps;
import com.teamcity.common.annotations.UserSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class UserSessionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getTestMethod()
                .orElseThrow()
                .getAnnotation(UserSession.class);

        if (annotation != null && annotation.create()) {
            // Получаем экземпляр теста
            BaseTest testInstance = (BaseTest) extensionContext.getRequiredTestInstance();

            // Создаём пользователя с аккаунтами через API
            TestUserContext userContext = UserSteps.createUserWithAccounts(
                    annotation.prefix(),
                    annotation.role(),
                    annotation.accounts()
            );

//            // Сохраняем пользователя в контекст теста
//            testInstance.setCurrentUser(userContext);
//
//            System.out.println("🔐 User created via API for thread " +
//                    Thread.currentThread().getName() + ": " + userContext.getDisplayName());
        }
    }
}