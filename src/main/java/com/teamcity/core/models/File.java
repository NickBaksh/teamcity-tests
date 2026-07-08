package com.teamcity.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class File {
    private String name;
    private String fullName;
    private Long size;
    private String modificationTime;
    private String href;
}