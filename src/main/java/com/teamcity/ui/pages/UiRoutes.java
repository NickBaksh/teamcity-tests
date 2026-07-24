package com.teamcity.ui.pages;

import com.teamcity.core.testdata.TestDataValues;

/**
 * Relative UI paths for TeamCity pages used by Page Objects.
 */
public final class UiRoutes {

    private UiRoutes() {
    }

    public static final String LOGIN = "/login.html";
    public static final String OVERVIEW = "/overview.html";

    public static String createProjectUnderRoot() {
        return "/admin/createProject.html?projectId=" + TestDataValues.ROOT_PROJECT_ID;
    }

    public static String project(String projectId) {
        return "/project/" + projectId;
    }

    public static String editProject(String projectId) {
        return "/admin/editProject.html?projectId=" + projectId;
    }

    public static String classicCreateBuildType(String projectId) {
        return "/admin/createBuildType.html?projectId=" + projectId;
    }

    public static String createBuildConfig(String projectId) {
        return "/projects/create?projectId=" + projectId + "&setup=build";
    }

    public static String buildConfiguration(String buildConfigId) {
        return "/buildConfiguration/" + buildConfigId;
    }

    public static String editBuild(String buildConfigId) {
        return "/admin/editBuild.html?id=buildType:" + buildConfigId;
    }

    public static String editBuildRunners(String buildConfigId) {
        return "/admin/editBuildRunners.html?id=buildType:" + buildConfigId;
    }

    public static String editRunTypeNew(String buildConfigId) {
        return "/admin/editRunType.html?id=buildType:" + buildConfigId
                + "&runnerId=__NEW_RUNNER__&init=1";
    }
}
