package com.teamcity.api.base;

import com.teamcity.api.configs.Config;
import com.teamcity.core.client.RestClient;
import com.teamcity.core.steps.BuildSteps;
import com.teamcity.core.steps.ProjectSteps;
import com.teamcity.core.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;

public class BaseApiTest {
    protected ProjectSteps adminProjectSteps;
    protected BuildSteps adminBuildSteps;
    protected ProjectSteps userProjectSteps;
    protected BuildSteps userBuildSteps;
    protected TestDataFactory dataFactory;
    protected RestClient adminClient;
    protected RestClient userClient;

    protected RestClient createClient(String login, String password) {
        return RestClient.builder()
                .baseUrl(Config.getProperty(Config.API_BASE_URL))
                .basicAuth(login, password)
                .build();
    }

    @BeforeEach
    void setUp() {
        dataFactory = new TestDataFactory();

        adminClient = createClient(
                Config.getProperty(Config.ADMIN_LOGIN),
                Config.getProperty(Config.ADMIN_PASSWORD)
        );

        // Заглушка, пока нет тестового пользователя
        userClient = createClient(
                Config.getProperty(Config.ADMIN_LOGIN),
                Config.getProperty(Config.ADMIN_PASSWORD)
        );

        adminProjectSteps = new ProjectSteps(adminClient);
        adminBuildSteps = new BuildSteps(adminClient);

        userProjectSteps = new ProjectSteps(userClient);
        userBuildSteps = new BuildSteps(userClient);
    }
}