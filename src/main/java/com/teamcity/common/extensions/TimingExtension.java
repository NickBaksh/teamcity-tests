package com.teamcity.common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private Map<String, Long> startTimes = new ConcurrentHashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        String testName = context.getRequiredTestClass().getPackageName() + "." + context.getDisplayName();
        startTimes.put(testName, System.currentTimeMillis());
        System.out.println("Thread " + Thread.currentThread().getName() + ": Test started " + testName);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        String testName = context.getRequiredTestClass().getPackageName() + "." + context.getDisplayName();
        Long testDuration = System.currentTimeMillis() - startTimes.get(testName);
        System.out.println("Thread " + Thread.currentThread().getName() + ": Test finished " + testName + ", test duration " + testDuration + " ms");
    }
}
