package com.teamcity.api.requests.skelethon;

import com.teamcity.api.models.BaseModel;
import com.teamcity.api.models.dto_models.builds.*;
import com.teamcity.api.models.dto_models.issue.RelatedIssuesResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {

    // ===== Builds (GET только для чтения) =====
    BUILDS_GET(
            "/app/rest/builds/{buildLocator}",
            null,
            BuildResponse.class,
            "buildLocator"
    ),

    BUILDS_STATISTICS(
            "/app/rest/builds/{buildLocator}/statistics",
            null,
            BuildStatistic.class,
            "buildLocator"
    ),

    BUILDS_DELETE(
            "/app/rest/builds/{buildLocator}",
            null,
            DeleteBuildResponse.class,
            "buildLocator"
    ),

    BUILDS_RELATED_ISSUES(
            "/app/rest/builds/{buildLocator}/relatedIssues",
            null,
            RelatedIssuesResponse.class,
            "buildLocator"
    ),

    BUILDS_TAGS_UPDATE(
            "/app/rest/builds/{buildLocator}/tags",
            BuildTagsRequest.class,
            BuildTagsResponse.class,
            "buildLocator"
    ),

    // ===== Build Queue (POST для создания/запуска билда) =====
    BUILD_QUEUE_ADD(
            "/app/rest/buildQueue",
            BuildQueueRequest.class,
            BuildQueueResponse.class,
            null
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
    private final String pathParam;
}