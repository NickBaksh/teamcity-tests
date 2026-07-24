package com.teamcity.core.storage;

import com.teamcity.core.models.Agent;

public final class AgentStorage {

    private static final ThreadLocal<Agent> AGENT = new ThreadLocal<>();

    private AgentStorage() {
    }

    public static void set(Agent agent) {
        AGENT.set(agent);
    }

    public static Agent get() {
        return AGENT.get();
    }

    public static void clear() {
        AGENT.remove();
    }
}