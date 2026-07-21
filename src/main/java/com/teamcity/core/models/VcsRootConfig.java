package com.teamcity.core.models;

import com.teamcity.core.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VcsRootConfig {

    @GeneratingRule(regex = "VcsRoot_[A-Za-z0-9]{8}")
    private String id;

    @GeneratingRule(regex = "VCS Root_[A-Za-z]{6,10}")
    private String name;

    @GeneratingRule(regex = "https://github\\.com/[A-Za-z0-9_-]+/[A-Za-z0-9_-]+\\.git")
    private String url;

    @GeneratingRule(regex = "refs/heads/(main|develop|master)")
    private String branch;

    private String projectId;
    private String description;
    private String username;
    private String password;
    private String vcsName;
}
