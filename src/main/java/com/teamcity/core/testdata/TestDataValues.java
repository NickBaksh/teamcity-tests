package com.teamcity.core.testdata;

public final class TestDataValues {

    private TestDataValues() {
    }

    public static final String ROOT_PROJECT_ID = "_Root";
    public static final String NON_EXISTENT_ID = "non-existent-id-12345";
    public static final String INVALID_PROJECT_ID = "invalid-project-id";

    public static final String PROJECT_DESCRIPTION = "Test project description";
    public static final String BUILD_CONFIG_DESCRIPTION = "This is a test build config";
    public static final String UPDATED_DESCRIPTION_PREFIX = "Updated description ";
    public static final String UPDATED_PROJECT_NAME = "New Name";

    public static final String INVALID_EMAIL = "invalid-email";
    public static final String VALID_EMAIL = "valid@example.com";
    public static final String VALID_PLUS_EMAIL = "valid+test@example.com";
    public static final String SHORT_PASSWORD = "123";
    public static final String LONG_PASSWORD = "a".repeat(100);

    public static final String MSG_USERNAME_EMPTY = "Username must not be empty";
    public static final String MSG_PROJECT_NAME_EMPTY = "Project name cannot be empty";
    public static final String MSG_PROJECT_NOT_FOUND = "Cannot find project";

    public static final String BUILD_STATE_QUEUED = "queued";
    public static final String BUILD_STATE_RUNNING = "running";
    public static final String BUILD_STATE_FINISHED = "finished";

    public static String updatedDescription() {
        return UPDATED_DESCRIPTION_PREFIX + System.currentTimeMillis();
    }
}
