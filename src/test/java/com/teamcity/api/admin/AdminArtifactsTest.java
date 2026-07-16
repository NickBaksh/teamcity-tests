package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.*;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Artifact Management")
@Tag("admin")
public class AdminArtifactsTest extends BaseApiTest {

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCanGetEmptyArtifactsListTest() {
        BuildConfig buildConfig = givenBuildConfig();
        Build finished = givenFinishedBuild(buildConfig.getId());

        Files artifacts = artifactSteps.getArtifacts(finished.getId());

        ApiAssertions.assertArtifactsEmpty(artifacts);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void adminCanGetBuildArtifactsListTest() {
        Build finished = givenFinishedNBankBuild();

        Files artifacts = artifactSteps.getArtifacts(finished.getId());

        ApiAssertions.assertArtifactsExist(artifacts);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCannotGetArtifactsFromNonExistingBuildTest() {
        ApiAssertions.assertNotFound(() ->
                artifactSteps.getArtifacts(TestDataValues.NON_EXISTENT_BUILD_ID)
        );
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void adminCanDownloadArtifactTest() {
        Build finished = givenFinishedNBankBuild();
        Files artifacts = givenArtifacts(finished);

        File artifact = artifactSteps.getFirstArtifactByExtension(
                artifacts,
                TestDataValues.TXT_ARTIFACT_EXTENSION);

        ApiAssertions.assertArtifact(artifact);

        byte[] content = artifactSteps.downloadArtifact(
                finished.getId(),
                artifact.getName());

        ApiAssertions.assertArtifactContent(content);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCanNotDownloadNonExistingArtifactTest() {
        Build finished = givenFinishedNBankBuild();
        ApiAssertions.assertNotFound(() ->
                artifactSteps.downloadArtifact(
                        finished.getId(),
                        TestDataValues.NON_EXISTENT_ARTIFACT_NAME));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void adminCanDownloadAllArtifactsAsArchiveTest() {
        Build finished = givenFinishedNBankBuild();

        byte[] archive = artifactSteps.downloadArtifactsArchive(finished.getId(),
                TestDataValues.ROOT_ARTIFACT_PATH);
        ApiAssertions.assertArchiveContent(archive);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void adminCanGetArtifactMetadataTest() {
        Build finished = givenFinishedNBankBuild();
        Files artifacts = givenArtifacts(finished);

        File artifact = artifactSteps.getFirstArtifactByExtension(
                artifacts, TestDataValues.TXT_ARTIFACT_EXTENSION);

        File metadata = artifactSteps.getArtifactMetadata(
                finished.getId(),
                artifact.getName());
        ApiAssertions.assertArtifactMetadata(artifact, metadata);
    }
}
