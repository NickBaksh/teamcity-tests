package com.teamcity.core.extensions;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.models.Agent;
import com.teamcity.core.storage.AgentStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RestoreAgentExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {

        Agent agent = AgentStorage.get();

        if (agent == null) {
            return;
        }

        try {
            BaseApiTest test = (BaseApiTest) context.getRequiredTestInstance();
            test.getAgentSteps().enableAgent(agent.getId().toString());
        } finally {
            AgentStorage.clear();
        }
    }
}