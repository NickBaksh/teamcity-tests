package com.teamcity.core.endpoints;

import lombok.Getter;

@Getter
public enum Endpoint {
    // ===== ROOT =====
    ROOT("/app/rest"),
    API_VERSION("/app/rest/apiVersion"),
    VERSION("/app/rest/version"),
    INFO("/app/rest/info"),

    // ===== SERVER =====
    SERVER("/app/rest/server"),
    SERVER_AUTH_SETTINGS("/app/rest/server/authSettings"),
    SERVER_GLOBAL_SETTINGS("/app/rest/server/globalSettings"),
    SERVER_BACKUP("/app/rest/server/backup"),
    SERVER_CLEANUP("/app/rest/server/cleanup"),
    SERVER_LICENSING_DATA("/app/rest/server/licensingData"),
    SERVER_LICENSE_KEYS("/app/rest/server/licensingData/licenseKeys"),
    SERVER_LICENSE_KEY("/app/rest/server/licensingData/licenseKeys/{licenseKey}"),
    SERVER_METRICS("/app/rest/server/metrics"),
    SERVER_PLUGINS("/app/rest/server/plugins"),
    SERVER_FILES("/app/rest/server/files/{areaId}"),
    SERVER_NODES("/app/rest/server/nodes"),
    SERVER_NODE("/app/rest/server/nodes/{nodeLocator}"),

    // ===== AGENTS =====
    AGENTS("/app/rest/agents"),
    AGENT("/app/rest/agents/{agentLocator}"),
    AGENT_AUTHORIZED_INFO("/app/rest/agents/{agentLocator}/authorizedInfo"),
    AGENT_ENABLED_INFO("/app/rest/agents/{agentLocator}/enabledInfo"),
    AGENT_POOL("/app/rest/agents/{agentLocator}/pool"),
    AGENT_COMPATIBLE_BUILD_TYPES("/app/rest/agents/{agentLocator}/compatibleBuildTypes"),
    AGENT_INCOMPATIBLE_BUILD_TYPES("/app/rest/agents/{agentLocator}/incompatibleBuildTypes"),
    AGENT_COMPATIBILITY_POLICY("/app/rest/agents/{agentLocator}/compatibilityPolicy"),
    AGENT_TYPES("/app/rest/agentTypes"),
    AGENT_TYPE("/app/rest/agentTypes/{agentTypeLocator}"),

    // ===== AGENT POOLS =====
    AGENT_POOLS("/app/rest/agentPools"),
    AGENT_POOL_LOCATOR("/app/rest/agentPools/{agentPoolLocator}"),
    AGENT_POOL_AGENTS("/app/rest/agentPools/{agentPoolLocator}/agents"),
    AGENT_POOL_PROJECTS("/app/rest/agentPools/{agentPoolLocator}/projects"),
    AGENT_POOL_AUTHORIZATION_TOKENS("/app/rest/agentPools/{agentPoolLocator}/authorizationTokens"),

    // ===== AUDIT =====
    AUDIT("/app/rest/audit"),
    AUDIT_EVENT("/app/rest/audit/{auditEventLocator}"),

    // ===== AVATAR =====
    AVATAR("/app/rest/avatars/{userLocator}"),
    AVATAR_IMAGE("/app/rest/avatars/{userLocator}/{size}/avatar.png"),

    // ===== BUILDS =====
    BUILDS("/app/rest/builds"),
    BUILD("/app/rest/builds/{buildLocator}"),
    BUILD_MULTIPLE("/app/rest/builds/multiple/{buildLocator}"),
    BUILD_AGGREGATED_STATUS("/app/rest/builds/aggregated/{buildLocator}/status"),
    BUILD_AGGREGATED_STATUS_ICON("/app/rest/builds/aggregated/{buildLocator}/statusIcon{suffix}"),
    BUILD_ARTIFACTS("/app/rest/builds/{buildLocator}/artifacts"),
    BUILD_ARTIFACT_ARCHIVE("/app/rest/builds/{buildLocator}/artifacts/archived{path}"),
    BUILD_ARTIFACTS_DIRECTORY("/app/rest/builds/{buildLocator}/artifactsDirectory"),
    BUILD_ARTIFACT_METADATA("/app/rest/builds/{buildLocator}/artifacts/metadata{path}"),
    BUILD_ARTIFACT_FILE("/app/rest/builds/{buildLocator}/artifacts/files{path}"),
    BUILD_STATISTICS("/app/rest/builds/{buildLocator}/statistics"),
    BUILD_STATISTIC_VALUE("/app/rest/builds/{buildLocator}/statistics/{name}"),
    BUILD_TAGS("/app/rest/builds/{buildLocator}/tags"),
    BUILD_COMMENT("/app/rest/builds/{buildLocator}/comment"),
    BUILD_PIN_INFO("/app/rest/builds/{buildLocator}/pinInfo"),
    BUILD_STATUS("/app/rest/builds/{buildLocator}/status"),
    BUILD_STATUS_TEXT("/app/rest/builds/{buildLocator}/statusText"),
    BUILD_STATUS_ICON("/app/rest/builds/{buildLocator}/statusIcon{suffix}"),
    BUILD_NUMBER("/app/rest/builds/{buildLocator}/number"),
    BUILD_CANCELED_INFO("/app/rest/builds/{buildLocator}/canceledInfo"),
    BUILD_FINISH_DATE("/app/rest/builds/{buildLocator}/finishDate"),
    BUILD_RUNNING_DATA("/app/rest/builds/{buildLocator}/runningData"),
    BUILD_PROBLEM_OCCURRENCES("/app/rest/builds/{buildLocator}/problemOccurrences"),
    BUILD_TEST_OCCURRENCES("/app/rest/builds/{buildLocator}/testOccurrences"),
    BUILD_VCS_LABELS("/app/rest/builds/{buildLocator}/vcsLabels"),
    BUILD_RELATED_ISSUES("/app/rest/builds/{buildLocator}/relatedIssues"),
    BUILD_RESULTING_PROPERTIES("/app/rest/builds/{buildLocator}/resulting-properties"),
    BUILD_OUTPUT_PARAMETERS("/app/rest/builds/{buildLocator}/output-parameters"),
    BUILD_SOURCE_FILE("/app/rest/builds/{buildLocator}/sources/files/{fileName}"),
    BUILD_LOG("/app/rest/builds/{buildLocator}/log"),
    BUILD_ARTIFACT_DEPENDENCY_CHANGES("/app/rest/builds/{buildLocator}/artifactDependencyChanges"),
    BUILD_CACHES_FINISH_PROPERTIES("/app/rest/builds/{buildLocator}/caches/finishProperties"),

    // ===== BUILD QUEUE =====
    BUILD_QUEUE("/app/rest/buildQueue"),
    BUILD_QUEUE_ITEM("/app/rest/buildQueue/{queuedBuildLocator}"),
    BUILD_QUEUE_ORDER("/app/rest/buildQueue/order"),
    BUILD_QUEUE_ORDER_POSITION("/app/rest/buildQueue/order/{queuePosition}"),
    BUILD_QUEUE_PAUSED_STATE("/app/rest/buildQueue/pausedState"),
    BUILD_QUEUE_APPROVAL_INFO("/app/rest/buildQueue/{buildLocator}/approvalInfo"),
    BUILD_QUEUE_APPROVE("/app/rest/buildQueue/{buildLocator}/approve"),
    BUILD_QUEUE_TAGS("/app/rest/buildQueue/{buildLocator}/tags"),
    BUILD_QUEUE_COMPATIBLE_AGENTS("/app/rest/buildQueue/{queuedBuildLocator}/compatibleAgents"),

    // ===== BUILD TYPES =====
    BUILD_TYPES("/app/rest/buildTypes"),
    BUILD_TYPE("/app/rest/buildTypes/{btLocator}"),
    BUILD_TYPE_NAME("/app/rest/buildTypes/{btLocator}/name"),
    BUILD_TYPE_PAUSED("/app/rest/buildTypes/{btLocator}/paused"),
    BUILD_TYPE_SETTINGS_FILE("/app/rest/buildTypes/{btLocator}/settingsFile"),
    BUILD_TYPE_BUILDS("/app/rest/buildTypes/{btLocator}/builds"),
    BUILD_TYPE_BRANCHES("/app/rest/buildTypes/{btLocator}/branches"),
    BUILD_TYPE_BUILD_TAGS("/app/rest/buildTypes/{btLocator}/buildTags"),
    BUILD_TYPE_STEPS("/app/rest/buildTypes/{btLocator}/steps"),
    BUILD_TYPE_STEP("/app/rest/buildTypes/{btLocator}/steps/{stepId}"),
    BUILD_TYPE_FEATURES("/app/rest/buildTypes/{btLocator}/features"),
    BUILD_TYPE_FEATURE("/app/rest/buildTypes/{btLocator}/features/{featureId}"),
    BUILD_TYPE_TRIGGERS("/app/rest/buildTypes/{btLocator}/triggers"),
    BUILD_TYPE_TRIGGER("/app/rest/buildTypes/{btLocator}/triggers/{triggerLocator}"),
    BUILD_TYPE_AGENT_REQUIREMENTS("/app/rest/buildTypes/{btLocator}/agent-requirements"),
    BUILD_TYPE_AGENT_REQUIREMENT("/app/rest/buildTypes/{btLocator}/agent-requirements/{agentRequirementLocator}"),
    BUILD_TYPE_SNAPSHOT_DEPENDENCIES("/app/rest/buildTypes/{btLocator}/snapshot-dependencies"),
    BUILD_TYPE_SNAPSHOT_DEPENDENCY("/app/rest/buildTypes/{btLocator}/snapshot-dependencies/{snapshotDepLocator}"),
    BUILD_TYPE_ARTIFACT_DEPENDENCIES("/app/rest/buildTypes/{btLocator}/artifact-dependencies"),
    BUILD_TYPE_ARTIFACT_DEPENDENCY("/app/rest/buildTypes/{btLocator}/artifact-dependencies/{artifactDepLocator}"),
    BUILD_TYPE_VCS_ROOT_ENTRIES("/app/rest/buildTypes/{btLocator}/vcs-root-entries"),
    BUILD_TYPE_VCS_ROOT_ENTRY("/app/rest/buildTypes/{btLocator}/vcs-root-entries/{vcsRootLocator}"),
    BUILD_TYPE_TEMPLATES("/app/rest/buildTypes/{btLocator}/templates"),
    BUILD_TYPE_TEMPLATE("/app/rest/buildTypes/{btLocator}/templates/{templateLocator}"),
    BUILD_TYPE_PARAMETERS("/app/rest/buildTypes/{btLocator}/parameters"),
    BUILD_TYPE_PARAMETER("/app/rest/buildTypes/{btLocator}/parameters/{name}"),
    BUILD_TYPE_OUTPUT_PARAMETERS("/app/rest/buildTypes/{btLocator}/output-parameters"),
    BUILD_TYPE_OUTPUT_PARAMETER("/app/rest/buildTypes/{btLocator}/output-parameters/{name}"),
    BUILD_TYPE_ALIASES("/app/rest/buildTypes/{btLocator}/aliases"),
    BUILD_TYPE_INVESTIGATIONS("/app/rest/buildTypes/{btLocator}/investigations"),
    BUILD_TYPE_MOVE("/app/rest/buildTypes/{btLocator}/move"),
    BUILD_TYPE_VCS_ROOT_INSTANCES("/app/rest/buildTypes/{btLocator}/vcsRootInstances"),
    BUILD_TYPE_VCS_FILES_LATEST("/app/rest/buildTypes/{btLocator}/vcs/files/latest"),

    // ===== CHANGES =====
    CHANGES("/app/rest/changes"),
    CHANGE("/app/rest/changes/{changeLocator}"),
    CHANGE_ATTRIBUTES("/app/rest/changes/{changeLocator}/attributes"),
    CHANGE_DUPLICATES("/app/rest/changes/{changeLocator}/duplicates"),
    CHANGE_FIRST_BUILDS("/app/rest/changes/{changeLocator}/firstBuilds"),
    CHANGE_ISSUES("/app/rest/changes/{changeLocator}/issues"),
    CHANGE_PARENT_CHANGES("/app/rest/changes/{changeLocator}/parentChanges"),
    CHANGE_PARENT_REVISIONS("/app/rest/changes/{changeLocator}/parentRevisions"),
    CHANGE_VCS_ROOT_INSTANCE("/app/rest/changes/{changeLocator}/vcsRootInstance"),

    // ===== CLOUD =====
    CLOUD_IMAGES("/app/rest/cloud/images"),
    CLOUD_IMAGE("/app/rest/cloud/images/{imageLocator}"),
    CLOUD_INSTANCES("/app/rest/cloud/instances"),
    CLOUD_INSTANCE("/app/rest/cloud/instances/{instanceLocator}"),
    CLOUD_PROFILES("/app/rest/cloud/profiles"),
    CLOUD_PROFILE("/app/rest/cloud/profiles/{profileLocator}"),

    // ===== DEPLOYMENT DASHBOARD =====
    DEPLOYMENT_DASHBOARDS("/app/rest/deploymentDashboards"),
    DEPLOYMENT_DASHBOARD("/app/rest/deploymentDashboards/{deploymentDashboardLocator}"),
    DEPLOYMENT_INSTANCES("/app/rest/deploymentDashboards/{deploymentDashboardLocator}/instances"),
    DEPLOYMENT_INSTANCE("/app/rest/deploymentDashboards/{deploymentDashboardLocator}/instances/{deploymentInstanceLocator}"),

    // ===== USER GROUPS (все группы, их свойства и роли) =====
    GROUPS("/app/rest/userGroups"),
    GROUP("/app/rest/userGroups/{groupLocator}"),
    GROUP_PARENT_GROUPS("/app/rest/userGroups/{groupLocator}/parent-groups"),
    GROUP_PROPERTIES("/app/rest/userGroups/{groupLocator}/properties"),
    GROUP_PROPERTY("/app/rest/userGroups/{groupLocator}/properties/{name}"),
    GROUP_ROLES("/app/rest/userGroups/{groupLocator}/roles"),
    GROUP_ROLE("/app/rest/userGroups/{groupLocator}/roles/{roleId}/{scope}"),

    // ===== HEALTH =====
    HEALTH("/app/rest/health"),
    HEALTH_CATEGORY("/app/rest/health/category"),
    HEALTH_CATEGORY_SINGLE("/app/rest/health/category/{locator}"),
    HEALTH_ITEM("/app/rest/health/{locator}"),

    // ===== INVESTIGATIONS =====
    INVESTIGATIONS("/app/rest/investigations"),
    INVESTIGATION("/app/rest/investigations/{investigationLocator}"),
    INVESTIGATIONS_MULTIPLE("/app/rest/investigations/multiple"),

    // ===== MUTES =====
    MUTES("/app/rest/mutes"),
    MUTE("/app/rest/mutes/{muteLocator}"),
    MUTES_MULTIPLE("/app/rest/mutes/multiple"),

    // ===== PROBLEMS =====
    PROBLEMS("/app/rest/problems"),
    PROBLEM("/app/rest/problems/{problemLocator}"),
    PROBLEM_OCCURRENCES("/app/rest/problemOccurrences"),
    PROBLEM_OCCURRENCE("/app/rest/problemOccurrences/{problemLocator}"),

    // ===== PROJECTS =====
    PROJECTS("/app/rest/projects"),
    PROJECT("/app/rest/projects/{projectLocator}"),
    PROJECT_NAME("/app/rest/projects/{projectLocator}/name"),
    PROJECT_DESCRIPTION("/app/rest/projects/{projectLocator}/description"),
    PROJECT_PARENT_PROJECT("/app/rest/projects/{projectLocator}/parentProject"),
    PROJECT_BUILD_TYPES("/app/rest/projects/{projectLocator}/buildTypes"),
    PROJECT_TEMPLATES("/app/rest/projects/{projectLocator}/templates"),
    PROJECT_DEFAULT_TEMPLATE("/app/rest/projects/{projectLocator}/defaultTemplate"),
    PROJECT_AGENT_POOLS("/app/rest/projects/{projectLocator}/agentPools"),
    PROJECT_AGENT_POOL("/app/rest/projects/{projectLocator}/agentPools/{agentPoolLocator}"),
    PROJECT_PARAMETERS("/app/rest/projects/{projectLocator}/parameters"),
    PROJECT_PARAMETER("/app/rest/projects/{projectLocator}/parameters/{name}"),
    PROJECT_FEATURES("/app/rest/projects/{projectLocator}/projectFeatures"),
    PROJECT_FEATURE("/app/rest/projects/{projectLocator}/projectFeatures/{featureLocator}"),
    PROJECT_VCS_ROOTS("/app/rest/projects/{projectLocator}/vcsRoots"),
    PROJECT_BRANCHES("/app/rest/projects/{projectLocator}/branches"),
    PROJECT_SETTINGS_FILE("/app/rest/projects/{projectLocator}/settingsFile"),
    PROJECT_SECURE_TOKENS("/app/rest/projects/{projectLocator}/secure/tokens"),
    PROJECT_SECURE_VALUE("/app/rest/projects/{projectLocator}/secure/values/{token}"),
    PROJECT_VERSIONED_SETTINGS_CONFIG("/app/rest/projects/{locator}/versionedSettings/config"),
    PROJECT_VERSIONED_SETTINGS_STATUS("/app/rest/projects/{locator}/versionedSettings/status"),
    PROJECT_VERSIONED_SETTINGS_LOAD("/app/rest/projects/{locator}/versionedSettings/loadSettings"),
    PROJECT_VERSIONED_SETTINGS_COMMIT("/app/rest/projects/{locator}/versionedSettings/commitCurrentSettings"),
    PROJECT_VERSIONED_SETTINGS_CHECK("/app/rest/projects/{locator}/versionedSettings/checkForChanges"),

    // ===== ROLES =====
    ROLES("/app/rest/roles"),
    ROLE("/app/rest/roles/id:{id}"),
    ROLE_INCLUDED("/app/rest/roles/id:{roleId}/included/{includedId}"),
    ROLE_PERMISSION("/app/rest/roles/id:{roleId}/permissions/{permissionId}"),

    // ===== STATISTICS =====
    STATISTICS("/app/rest/statistics"),

    // ===== TESTS =====
    TESTS("/app/rest/tests"),
    TEST("/app/rest/tests/{testLocator}"),
    TEST_OCCURRENCES("/app/rest/testOccurrences"),
    TEST_OCCURRENCE("/app/rest/testOccurrences/{testLocator}"),

    // ===== USERS =====
    USERS("/app/rest/users"),
    USER("/app/rest/users/{userLocator}"),
    USER_NAME("/app/rest/users/{userLocator}/name"),
    USER_EMAIL("/app/rest/users/{userLocator}/email"),
    USER_PASSWORD("/app/rest/users/{userLocator}/password"),
    USER_PROPERTIES("/app/rest/users/{userLocator}/properties"),
    USER_PROPERTY("/app/rest/users/{userLocator}/properties/{name}"),
    USER_ROLES("/app/rest/users/{userLocator}/roles"),
    USER_ROLE("/app/rest/users/{userLocator}/roles/{roleId}/{scope}"),
    USER_GROUPS("/app/rest/users/{userLocator}/groups"),
    USER_GROUP("/app/rest/users/{userLocator}/groups/{groupLocator}"),
    USER_TOKENS("/app/rest/users/{userLocator}/tokens"),
    USER_TOKEN("/app/rest/users/{userLocator}/tokens/{name}"),
    USER_PERMISSIONS("/app/rest/users/{userLocator}/permissions"),
    USER_LOGOUT("/app/rest/users/{userLocator}/logout"),
    USER_DEBUG_REMEMBER_ME("/app/rest/users/{userLocator}/debug/rememberMe"),

    // ===== VCS ROOTS =====
    VCS_ROOTS("/app/rest/vcs-roots"),
    VCS_ROOT("/app/rest/vcs-roots/{vcsRootLocator}"),
    VCS_ROOT_NAME("/app/rest/vcs-roots/{vcsRootLocator}/name"),
    VCS_ROOT_PROPERTIES("/app/rest/vcs-roots/{vcsRootLocator}/properties"),
    VCS_ROOT_PROPERTY("/app/rest/vcs-roots/{vcsRootLocator}/properties/{name}"),
    VCS_ROOT_INSTANCES("/app/rest/vcs-roots/{vcsRootLocator}/instances"),
    VCS_ROOT_SETTINGS_FILE("/app/rest/vcs-roots/{vcsRootLocator}/settingsFile"),

    // ===== VCS ROOT INSTANCES =====
    VCS_ROOT_INSTANCES_ALL("/app/rest/vcs-root-instances"),
    VCS_ROOT_INSTANCE("/app/rest/vcs-root-instances/{vcsRootInstanceLocator}"),
    VCS_ROOT_INSTANCE_PROPERTIES("/app/rest/vcs-root-instances/{vcsRootInstanceLocator}/properties"),
    VCS_ROOT_INSTANCE_REPOSITORY_STATE("/app/rest/vcs-root-instances/{vcsRootInstanceLocator}/repositoryState"),
    VCS_ROOT_INSTANCE_FILES_LATEST("/app/rest/vcs-root-instances/{vcsRootInstanceLocator}/files/latest"),
    VCS_ROOT_INSTANCE_CHECKING_CHANGES("/app/rest/vcs-root-instances/checkingForChangesQueue"),
    VCS_ROOT_INSTANCE_COMMIT_HOOK("/app/rest/vcs-root-instances/commitHookNotification");

    private final String path;

    Endpoint(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String format(Object... params) {
        String result = path;
        for (Object param : params) {
            result = result.replaceFirst("\\{[^}]+\\}", param.toString());
        }
        return result;
    }
}