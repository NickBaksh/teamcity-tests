package com.teamcity.core.cleanup;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CleanupExtension implements AfterEachCallback {
    @Override
    public void afterEach(ExtensionContext context) {
        CleanupRegistry.get().cleanup();
    }
}