package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.generators.GeneratingRule;
import com.teamcity.api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildTagsRequest extends BaseModel {
    @GeneratingRule(regex = "^(release|v[0-9]+\\.[0-9]+\\.[0-9]+|production|staging|hotfix|feature|beta|stable)$")
    private List<String> tags;

    @Builder.Default
    private Boolean append = false;  // true - добавить к существующим, false - заменить

    public static BuildTagsRequest from(String... tags) {
        return BuildTagsRequest.builder()
                .tags(List.of(tags))
                .append(false)
                .build();
    }

    public static BuildTagsRequest appendTags(String... tags) {
        return BuildTagsRequest.builder()
                .tags(List.of(tags))
                .append(true)
                .build();
    }
}
