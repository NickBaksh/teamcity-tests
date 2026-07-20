package com.teamcity.ui.testdata;

import com.teamcity.core.generators.RandomData;

/**
 * UI test data: name prefixes, expected messages, scripts, timeouts.
 * Locators and routes stay in Page Objects / {@link com.teamcity.ui.pages.UiRoutes}.
 */
public final class UiTestData {

    private UiTestData() {
    }

    public static final String PROJECT_NAME_PREFIX = "ui_project_";
    public static final String PROJECT_ID_PREFIX = "UIProject";
    public static final String SMOKE_PROJECT_NAME_PREFIX = "ui_smoke_";
    public static final String SMOKE_PROJECT_ID_PREFIX = "UISmoke";
    public static final String BUILD_CONFIG_NAME_PREFIX = "ui_bc_";
    public static final String BUILD_STEP_NAME_PREFIX = "echo_step_";
    public static final String EMPTY_PROJECT_ID_PREFIX = "EmptyName";
    public static final String DUPLICATE_PROJECT_NAME_PREFIX = "Duplicate ";

    public static final String INVALID_PASSWORD_PREFIX = "invalid-password-";

    public static final String COMMAND_LINE_SCRIPT = "echo hello";
    public static final String CUSTOM_SCRIPT_OPTION = "Custom script";
    public static final String PAUSE_COMMENT = "paused by ui test";
    public static final String RESUME_COMMENT = "activated by ui test";

    public static final String ERROR_EMPTY = "empty";
    public static final String ERROR_ALREADY_USED = "already used";
    public static final String ERROR_EMPTY_PROJECT_NAME_CODE = "emptyProjectName";
    public static final String ERROR_DUPLICATE_PROJECT_ID_CODE = "duplicateProjectId";
    public static final String ERROR_PROJECT_NAME_EMPTY_TEXT = "Project name is empty";
    public static final String ERROR_PROJECT_ID_USED_TEXT = "already used by another project";

    public static final String MARKER_COMMAND_LINE = "Command Line";
    public static final String MARKER_SIMPLE_RUNNER = "simpleRunner";
    public static final String MARKER_ECHO = "echo";

    public static final int UI_SHORT_TIMEOUT_SECONDS = 20;
    public static final int UI_DEFAULT_TIMEOUT_SECONDS = 30;
    public static final int UI_LONG_TIMEOUT_SECONDS = 45;
    public static final int UI_POLL_INTERVAL_SECONDS = 1;
    public static final int UI_POLL_INTERVAL_LONG_SECONDS = 2;

    public static String projectName() {
        return PROJECT_NAME_PREFIX + RandomData.shortId();
    }

    public static String projectId() {
        return PROJECT_ID_PREFIX + RandomData.shortId();
    }

    public static String smokeProjectName() {
        return SMOKE_PROJECT_NAME_PREFIX + RandomData.shortId();
    }

    public static String smokeProjectId() {
        return SMOKE_PROJECT_ID_PREFIX + RandomData.shortId();
    }

    public static String buildConfigName() {
        return BUILD_CONFIG_NAME_PREFIX + RandomData.shortId();
    }

    public static String buildStepName() {
        return BUILD_STEP_NAME_PREFIX + RandomData.shortId();
    }

    public static String emptyNameProjectId() {
        return EMPTY_PROJECT_ID_PREFIX + RandomData.shortId();
    }

    public static String duplicateProjectName() {
        return DUPLICATE_PROJECT_NAME_PREFIX + RandomData.shortId();
    }

    public static String invalidPassword() {
        return INVALID_PASSWORD_PREFIX + System.currentTimeMillis();
    }
}
