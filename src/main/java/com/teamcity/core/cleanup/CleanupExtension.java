package com.teamcity.core.cleanup;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CleanupExtension implements AfterEachCallback {

    private static final int CLEANUP_DELAY_MS = 2000; // 2 секунды

    @Override
    public void afterEach(ExtensionContext context) {
        // Выполняем очистку
        CleanupRegistry.get().cleanup();

        // Ждем после очистки
        try {
            Thread.sleep(CLEANUP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}