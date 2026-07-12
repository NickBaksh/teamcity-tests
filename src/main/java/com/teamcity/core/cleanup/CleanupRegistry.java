package com.teamcity.core.cleanup;

import java.util.ArrayList;
import java.util.List;

public final class CleanupRegistry {
    private static final ThreadLocal<CleanupRegistry> INSTANCE =
            ThreadLocal.withInitial(CleanupRegistry::new);

    private final List<Runnable> actions = new ArrayList<>();

    private CleanupRegistry() {
    }

    public static CleanupRegistry get() {
        return INSTANCE.get();
    }

    public void register(Runnable action) {
        actions.add(action);
    }

    public void cleanup() {
        for (Runnable action : actions) {
            try {
                action.run();
            } catch (Exception ignored) {
            }
        }

        actions.clear();
    }

    public void clear() {
        actions.clear();
    }
}