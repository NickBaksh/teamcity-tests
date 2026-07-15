package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Agent {
    private Integer id;
    private String name;
    private String typeId;
    private Boolean connected;
    private Boolean enabled;
    private Boolean authorized;
    private String href;
    private String webUrl;
    private String locator;
}