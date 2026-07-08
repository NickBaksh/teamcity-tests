package com.teamcity.core.client;

import com.teamcity.core.models.Build;
import com.teamcity.core.models.BuildConfig;
import java.util.List;

public interface BuildClient {
    BuildConfig createBuildConfig(BuildConfig config);
    BuildConfig getBuildConfig(String configId);
    List<BuildConfig> getAllBuildConfigs();
    BuildConfig updateBuildConfig(String configId, String newName);
    void deleteBuildConfig(String configId);
    boolean buildConfigExists(String configId);

    Build runBuild(String buildTypeId);
    Build getBuild(String buildId);
    void cancelBuild(String buildId, String comment);
    void pauseBuildConfig(String configId, boolean paused);
}