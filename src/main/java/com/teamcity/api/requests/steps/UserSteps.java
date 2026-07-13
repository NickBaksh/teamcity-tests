package com.teamcity.api.requests.steps;

import com.teamcity.api.models.dto_models.builds.BuildResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserSteps {

    private static final ThreadLocal<List<TestUserContext>> testUsers = ThreadLocal.withInitial(ArrayList::new);

    /**
     * Создать контекст пользователя с Basic Auth
     */
    public static TestUserContext createUserWithAccounts(String prefix, String role, int accountCount) {
        String username = prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "TestPass123!";

        // Для TeamCity используем Basic Auth
        String credentials = username + ":" + password;
        String token = "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        TestUserContext context = TestUserContext.builder()
                .username(username)
                .password(password)
                .token(token)
                .userId(System.currentTimeMillis())
                .role(role)
                .accounts(new ConcurrentHashMap<>())
                .build();

        testUsers.get().add(context);

        // Создаем аккаунты (билды) для пользователя
        for (int i = 0; i < accountCount; i++) {
            String buildTypeId = BuildSteps.generateBuildTypeId(prefix + "_ACCOUNT");
            BuildResponse build = BuildSteps.createBuild(buildTypeId, "main");
            context.addAccount(Long.parseLong(build.getId()), build.getId());
        }

        System.out.println("✅ User context created: " + context.getDisplayName());
        return context;
    }

    public static void cleanupTestUsers() {
        testUsers.get().clear();
    }

    public static void cleanupTestUser(TestUserContext user) {
        // Очистка не требуется
    }
}