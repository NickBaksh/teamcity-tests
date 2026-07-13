package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteBuildResponse extends BaseModel {
    private String buildId;
    private Boolean deleted;
    private String message;
    private String status;

    @Builder.Default
    private Integer relatedDataDeleted = 0;  // количество удаленных связанных данных

    public boolean isDeletedSuccessfully() {
        return deleted != null && deleted;
    }

    public String getSuccessMessage() {
        if (deleted) {
            return "Build " + buildId + " deleted successfully";
        }
        return "Failed to delete build " + buildId + ": " + message;
    }
}
