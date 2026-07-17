package com.teamcity.core.testdata;


import com.teamcity.core.utils.TestDataFactory;

public final class TestDataValues {
private TestDataValues() {
}

public static final String NBANK_BUILD_CONFIG_ID = "NBank_BuildNBank";
public static final int BUILD_WAIT_TIMEOUT_SECONDS = 30;

public static final String ROOT_PROJECT_ID = "_Root";
public static final String NON_EXISTENT_ID = "non-existent-id-12345";
public static final String NON_EXISTENT_ID_V2 = "999999999999999999";
public static final String NON_EXISTENT_ID_RANDOM = String.valueOf(System.currentTimeMillis());
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
public static final String BUILD_STATUS_SUCCESS = "SUCCESS";
public static final String BUILD_STATUS_UNKNOWN = "UNKNOWN";
public static final String BUILD_STATUS_CANCEL = "cancel";
public static final String BUILD_STATUS_FAILED = "failed";

public static final String NON_EXISTENT_AGENT_ID = String.valueOf(Integer.MAX_VALUE);
public static final String NON_EXISTENT_BUILD_ID = String.valueOf(Integer.MAX_VALUE);
public static final String INVALID_AGENT_ID = "non-existent-id-12345";

public static final String TXT_ARTIFACT_EXTENSION = ".txt";
public static final String SUREFIRE_REPORT_MARKER = "Tests run:";
public static final String VCS_ROOT_RANDOM_NAME = "Updated VCS Root_" + System.currentTimeMillis();
public static final String VCS_ROOT_URL = "https://github.com/example/updated-repo.git";
public static final String NON_EXISTENT_ARTIFACT_NAME = "non-existing-artifact.txt";
public static final String ROOT_ARTIFACT_PATH = "";

public static String updatedDescription() {
    return UPDATED_DESCRIPTION_PREFIX + System.currentTimeMillis();
}
}