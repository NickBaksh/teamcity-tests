package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildTagsResponse extends BaseModel {
    private String buildId;
    private List<String> tags;
    private Integer tagsCount;
    private Boolean appended;
    private String message;

    public boolean containsTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    public boolean containsAnyTag(List<String> searchTags) {
        if (tags == null || searchTags == null) return false;
        return tags.stream().anyMatch(searchTags::contains);
    }
}
