package com.teamcity.api.models.dto_models.builds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildQueueResponse extends BaseModel {
    private String id;           // ID билда в очереди
    private String buildTypeId;
    private String branchName;
    private String state;        // QUEUED, RUNNING, FINISHED
    private String status;       // SUCCESS, FAILURE, CANCELLED
    private String href;
    private String webUrl;
}