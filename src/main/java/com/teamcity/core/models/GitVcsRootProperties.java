package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitVcsRootProperties {
    private String url;
    private String branch;
    private String branchSpec;
    private String usernameStyle;
    private String authMethod;
    private String userName;
    private String password;
    private String privateKey;
    private String passphrase;
}