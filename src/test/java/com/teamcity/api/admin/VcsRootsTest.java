package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.models.VcsRoot;
import com.teamcity.core.models.VcsRootConfig;
import com.teamcity.core.models.dto.VcsRootUpdateRequest;
import com.teamcity.core.testdata.TestDataValues;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("VCS Root Management")
@Tag("admin")
public class VcsRootsTest extends BaseApiTest {

    private String testProjectId;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        testProjectId = givenProject().getId();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateVcsRoot() {
        VcsRoot vcsRoot = givenVcsRoot(testProjectId);

        ApiAssertions.assertVcsRootCreated(vcsRoot);
        assertThat(vcsRoot.getProjectId())
                .isEqualTo(testProjectId);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateVcsRootWithEmptyUrl() {
        VcsRoot vcsRoot = givenVcsRootWithEmptyUrl(testProjectId);

        ApiAssertions.assertVcsRootCreated(vcsRoot);
        assertThat(vcsRoot.getProjectId()).isEqualTo(testProjectId);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldNotCreateVcsRootWithInvalidConfig() {
        VcsRootConfig config = dataFactory.createInvalidVcsRootConfig(testProjectId);

        ApiAssertions.assertBadRequest(
                () -> vcsRootSteps.createVcsRoot(config)
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldNotCreateVcsRootInNonExistentProject() {
        VcsRootConfig config = dataFactory.createRandomVcsRootConfig(TestDataValues.NON_EXISTENT_ID_RANDOM);

        ApiAssertions.assertNotFound(
                () -> vcsRootSteps.createVcsRoot(config)
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldGetVcsRootById() {
        VcsRoot createdVcsRoot = givenVcsRoot(testProjectId);

        VcsRoot vcsRoot = vcsRootSteps.getVcsRoot(createdVcsRoot.getId());

        ApiAssertions.assertVcsRootExists(vcsRoot);
        assertThat(vcsRoot.getId()).isEqualTo(createdVcsRoot.getId());
        assertThat(vcsRoot.getName()).isEqualTo(createdVcsRoot.getName());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404ForNonExistentVcsRoot() {
        ApiAssertions.assertNotFound(
                () -> vcsRootSteps.getVcsRoot(TestDataValues.NON_EXISTENT_ID_RANDOM)
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldDeleteVcsRoot() {
        VcsRoot createdVcsRoot = givenVcsRoot(testProjectId);

        vcsRootSteps.deleteVcsRoot(createdVcsRoot.getId());

        ApiAssertions.assertNotFound(
                () -> vcsRootSteps.getVcsRoot(createdVcsRoot.getId())
        );
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Disabled
    void shouldUpdateVcsRoot() {
        // TODO: Поправить ошибку 405
        VcsRoot createdVcsRoot = givenVcsRoot(testProjectId);

        String newName = TestDataValues.VCS_ROOT_RANDOM_NAME;
        String newUrl = TestDataValues.VCS_ROOT_URL;

        VcsRootUpdateRequest updateRequest = VcsRootUpdateRequest.builder()
                .name(newName)
                .url(newUrl)
                .build();

        VcsRoot updatedVcsRoot = vcsRootSteps.updateVcsRoot(
                createdVcsRoot.getId(),
                updateRequest
        );

        ApiAssertions.assertVcsRootUpdated(updatedVcsRoot);
        assertThat(updatedVcsRoot.getName()).isEqualTo(newName);
        assertThat(updatedVcsRoot.getUrl()).isEqualTo(newUrl);
    }
}