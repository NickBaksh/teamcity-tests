package com.teamcity.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAvatars {
    private String urlToSize20;
    private String urlToSize28;
    private String urlToSize32;
    private String urlToSize40;
    private String urlToSize56;
    private String urlToSize64;
    private String urlToSize80;
}
