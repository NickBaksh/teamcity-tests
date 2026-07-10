package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.ResourceNotFoundException;
import com.teamcity.core.models.Project;
import com.teamcity.core.models.VcsRoot;
import com.teamcity.core.models.VcsRoot.VcsRootInstance;
import io.qameta.allure.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Epic("TeamCity API")
@Feature("VCS Roots Management")
@Tag("api")
@Tag("vcs")
@Tag("admin")
@Tag("regression")
@DisplayName("Admin API Tests for VCS Roots Module")
public class VcsRootTest extends BaseApiTest {

    private String projectId;
    private String vcsRootId;

    // =========================================================================
    // SETUP / TEARDOWN
    // =========================================================================

    @BeforeEach
    @Override
    @Step("Setup VCS Root test environment")
    public void setUp() {
        super.setUp();

        // Создаем только проект, без билд-конфига
        Project project = dataFactory.createRandomProject();
        projectId = projectSteps.createProject(project).getId();
        trackProject(projectId);

        log.info("✅ Test project created: {}", projectId);
    }

    @AfterEach
    @Override
    @Step("Cleanup VCS Root test resources")
    public void cleanUp() {
        try {
            if (vcsRootId != null) {
                adminSteps.deleteVcsRootIfExists(vcsRootId);
                log.info("🧹 Cleaned up VCS root: {}", vcsRootId);
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to cleanup VCS root: {}", e.getMessage());
        }
        super.cleanUp();
    }

    // =========================================================================
    // 1. CREATE — Создание VCS Roots
    // =========================================================================

    @Test
    @DisplayName("Should create Git VCS root successfully")
    @Severity(SeverityLevel.BLOCKER)
    @Story("VCS Root Creation")
    @Description("Test that a new Git VCS root can be created with valid data")
    @Tag("smoke")
    public void shouldCreateGitVcsRoot() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");

        VcsRoot created = adminSteps.createVcsRoot(vcsRoot);
        vcsRootId = created.getId();

        assertAll("VCS root should be created correctly",
                () -> assertNotNull(created.getId()),
                () -> assertEquals(vcsRoot.getId(), created.getId()),
                () -> assertEquals(vcsRoot.getName(), created.getName()),
                () -> assertEquals("jetbrains.git", created.getVcsName()),
                () -> assertEquals(projectId, created.getProject().getId()),
                () -> assertNotNull(created.getHref())
        );

        VcsRoot retrieved = adminSteps.getVcsRoot(vcsRootId);
        assertNotNull(retrieved);
        assertEquals(vcsRootId, retrieved.getId());
    }

    @Test
    @DisplayName("Should create SVN VCS root successfully")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Creation")
    @Description("Test that a new SVN VCS root can be created with valid data")
    public void shouldCreateSvnVcsRoot() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "svn");
        VcsRoot created = adminSteps.createVcsRoot(vcsRoot);
        vcsRootId = created.getId();

        assertAll("SVN VCS root should be created",
                () -> assertNotNull(created.getId()),
                () -> assertEquals("svn", created.getVcsName()),
                () -> assertEquals(projectId, created.getProject().getId())
        );
    }

    @ParameterizedTest
    @MethodSource("supportedVcsTypesProvider")
    @DisplayName("Should create VCS root for supported types")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Creation")
    @Description("Test that VCS root can be created for supported types")
    @Tag("parameterized")
    public void shouldCreateVcsRootForType(String vcsType) {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, vcsType);

        VcsRoot created = adminSteps.createVcsRoot(vcsRoot);
        vcsRootId = created.getId();

        assertNotNull(created, "VCS root should be created");
        assertEquals(vcsType, created.getVcsName(), "VCS type should match");
        assertNotNull(created.getId(), "ID should not be null");
        assertNotNull(created.getName(), "Name should not be null");
    }

    static Stream<Arguments> supportedVcsTypesProvider() {
        return Stream.of(
                Arguments.of("jetbrains.git"),
                Arguments.of("svn")
        );
    }

    @Test
    @DisplayName("Should create VCS root with custom properties")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Creation")
    @Description("Test that VCS root can be created with custom properties")
    public void shouldCreateVcsRootWithCustomProperties() {
        Map<String, String> customProps = new HashMap<>();
        customProps.put("authMethod", "PASSWORD");
        customProps.put("branch", "refs/heads/custom-branch");
        customProps.put("url", "https://github.com/teamcity-tests/custom-repo.git");
        customProps.put("username", "custom_user");
        customProps.put("password", "CustomPass123!");

        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git", customProps);

        VcsRoot created = adminSteps.createVcsRoot(vcsRoot);
        vcsRootId = created.getId();

        assertAll("VCS root should be created",
                () -> assertNotNull(created.getId()),
                () -> assertEquals("jetbrains.git", created.getVcsName()),
                () -> assertNotNull(created.getHref())
        );
    }

    // =========================================================================
    // 2. READ — Получение VCS Roots
    // =========================================================================

    @Test
    @DisplayName("Should get VCS root by ID")
    @Severity(SeverityLevel.CRITICAL)
    @Story("VCS Root Retrieval")
    @Description("Test that an existing VCS root can be retrieved by ID")
    @Tag("smoke")
    public void shouldGetVcsRoot() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        VcsRoot created = adminSteps.createVcsRoot(vcsRoot);
        vcsRootId = created.getId();

        VcsRoot retrieved = adminSteps.getVcsRoot(vcsRootId);

        assertAll("VCS root should be retrieved correctly",
                () -> assertNotNull(retrieved),
                () -> assertEquals(vcsRootId, retrieved.getId()),
                () -> assertEquals(created.getName(), retrieved.getName()),
                () -> assertEquals(projectId, retrieved.getProject().getId())
        );
    }

    @Test
    @DisplayName("Should get all VCS roots")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root List")
    @Description("Test that all VCS roots can be retrieved")
    public void shouldGetAllVcsRoots() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        List<VcsRoot> roots = adminSteps.getAllVcsRoots();

        assertNotNull(roots);
        assertTrue(roots.size() > 0);

        boolean found = roots.stream().anyMatch(r -> vcsRootId.equals(r.getId()));
        assertTrue(found);
    }

    // =========================================================================
    // 3. UPDATE — Обновление VCS Roots
    // =========================================================================

    @Test
    @DisplayName("Should update VCS root name")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Update")
    @Description("Test that VCS root name can be updated")
    public void shouldUpdateVcsRootName() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        String newName = dataFactory.generateVcsRootName();

        adminSteps.updateVcsRootName(vcsRootId, newName);

        VcsRoot updated = adminSteps.getVcsRoot(vcsRootId);
        assertEquals(newName, updated.getName());
    }

    @Test
    @DisplayName("Should update VCS root property")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Update")
    @Description("Test that VCS root property can be updated")
    public void shouldUpdateVcsRootProperty() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        String newBranch = "refs/heads/develop-" + System.currentTimeMillis();

        adminSteps.updateVcsRootProperty(vcsRootId, "branch", newBranch);

        VcsRoot updated = adminSteps.getVcsRoot(vcsRootId);
        assertNotNull(updated, "VCS root should be retrievable");
    }

    // =========================================================================
    // 4. DELETE — Удаление VCS Roots
    // =========================================================================

    @Test
    @DisplayName("Should delete VCS root")
    @Severity(SeverityLevel.BLOCKER)
    @Story("VCS Root Deletion")
    @Description("Test that VCS root can be deleted")
    @Tag("smoke")
    public void shouldDeleteVcsRoot() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        assertTrue(adminSteps.vcsRootExists(vcsRootId));

        adminSteps.deleteVcsRoot(vcsRootId);

        assertFalse(adminSteps.vcsRootExists(vcsRootId));
        vcsRootId = null;
    }

    @Test
    @DisplayName("Should delete VCS root if exists (idempotent)")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Deletion")
    @Description("Test idempotent deletion of VCS root")
    public void shouldDeleteVcsRootIfExists() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        boolean deletedFirst = adminSteps.deleteVcsRootIfExists(vcsRootId);
        assertTrue(deletedFirst);

        boolean deletedSecond = adminSteps.deleteVcsRootIfExists(vcsRootId);
        assertFalse(deletedSecond);
        vcsRootId = null;
    }

    // =========================================================================
    // 5. NEGATIVE TESTS
    // =========================================================================

    @Test
    @DisplayName("Should not create VCS root with duplicate ID")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that creating VCS root with duplicate ID fails")
    public void shouldNotCreateDuplicateVcsRoot() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        VcsRoot duplicate = VcsRoot.builder()
                .id(vcsRoot.getId())
                .name(dataFactory.generateVcsRootName())
                .vcsName("jetbrains.git")
                .project(Project.builder().id(projectId).build())
                .properties(dataFactory.generateGitVcsProperties())
                .build();

        ApiException exception = assertThrows(ApiException.class,
                () -> adminSteps.createVcsRoot(duplicate)
        );

        assertTrue(exception.getStatusCode() == 400 || exception.getStatusCode() == 409);
    }

    @Test
    @DisplayName("Should not get non-existent VCS root")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that getting non-existent VCS root throws exception")
    public void shouldNotGetNonExistentVcsRoot() {
        String nonExistentId = dataFactory.generateVcsRootId();

        assertThrows(ResourceNotFoundException.class,
                () -> adminSteps.getVcsRoot(nonExistentId)
        );
    }

    @Test
    @DisplayName("Should not delete non-existent VCS root")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Tests")
    @Description("Test that deleting non-existent VCS root throws exception")
    public void shouldNotDeleteNonExistentVcsRoot() {
        String nonExistentId = dataFactory.generateVcsRootId();

        assertThrows(ApiException.class,
                () -> adminSteps.deleteVcsRoot(nonExistentId)
        );
    }

    // =========================================================================
    // 6. SEARCH & FILTER TESTS
    // =========================================================================

    @Test
    @DisplayName("Should find VCS root by name")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Search")
    @Description("Test that VCS root can be found by name")
    public void shouldFindVcsRootByName() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        String expectedName = vcsRoot.getName();
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        var found = adminSteps.findVcsRootByName(expectedName);

        assertTrue(found.isPresent());
        assertEquals(vcsRootId, found.get().getId());
    }

    @Test
    @DisplayName("Should find VCS roots by type")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Search")
    @Description("Test that VCS roots can be filtered by type")
    public void shouldFindVcsRootsByType() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        List<VcsRoot> gitRoots = adminSteps.getVcsRootsByType("jetbrains.git");

        assertNotNull(gitRoots);
        assertTrue(gitRoots.stream().anyMatch(r -> vcsRootId.equals(r.getId())));
        assertTrue(gitRoots.stream().allMatch(r -> "jetbrains.git".equals(r.getVcsName())));
    }

    @Test
    @DisplayName("Should find VCS roots by name prefix")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Search")
    @Description("Test that VCS roots can be found by name prefix")
    public void shouldFindVcsRootsByNamePrefix() {
        String prefix = dataFactory.generateVcsRootId();
        for (int i = 0; i < 3; i++) {
            VcsRoot root = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
            root.setId(prefix + "_" + i);
            root.setName(prefix + " VCS Root " + i);
            adminSteps.createVcsRoot(root);
        }

        List<VcsRoot> found = adminSteps.findVcsRootsByNamePrefix(prefix);

        assertNotNull(found);
        assertTrue(found.size() >= 3);
        assertTrue(found.stream().allMatch(r ->
                r.getName() != null && r.getName().startsWith(prefix)));
    }

    // =========================================================================
    // 7. CLEANUP TESTS
    // =========================================================================

    @Test
    @DisplayName("Should cleanup test VCS roots by prefix")
    @Severity(SeverityLevel.NORMAL)
    @Story("Cleanup")
    @Description("Test cleanup of test VCS roots by prefix")
    public void shouldCleanupTestVcsRoots() {
        String prefix = "CleanupVcsTest_" + System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            VcsRoot root = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
            root.setId(prefix + "_" + i);
            root.setName(prefix + " VCS Root " + i);
            adminSteps.createVcsRoot(root);
        }

        int deleted = adminSteps.cleanupTestVcsRoots(prefix);
        assertEquals(5, deleted);

        List<VcsRoot> remaining = adminSteps.findVcsRootsByNamePrefix(prefix);
        assertEquals(0, remaining.size());
    }

    // =========================================================================
    // 8. VCS ROOT INSTANCES
    // =========================================================================

    @Test
    @DisplayName("Should get VCS root instances")
    @Severity(SeverityLevel.NORMAL)
    @Story("VCS Root Instances")
    @Description("Test that VCS root instances can be retrieved")
    public void shouldGetVcsRootInstances() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        List<VcsRootInstance> instances = adminSteps.getVcsRootInstances(vcsRootId);
        assertNotNull(instances);
    }

    @Test
    @DisplayName("Should send commit hook notification")
    @Severity(SeverityLevel.NORMAL)
    @Story("Commit Hook")
    @Description("Test that commit hook notification can be sent")
    public void shouldSendCommitHookNotification() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        String result = adminSteps.sendCommitHookNotification(vcsRootId);
        assertNotNull(result);
    }

    // =========================================================================
    // 9. UTILITY METHODS
    // =========================================================================

    @Test
    @DisplayName("Should check if VCS root exists")
    @Severity(SeverityLevel.NORMAL)
    @Story("Utility Methods")
    @Description("Test vcsRootExists method")
    public void shouldCheckVcsRootExists() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        assertTrue(adminSteps.vcsRootExists(vcsRootId));

        String nonExistentId = dataFactory.generateVcsRootId();
        assertFalse(adminSteps.vcsRootExists(nonExistentId));
    }

    @Test
    @DisplayName("Should get VCS root web URL")
    @Severity(SeverityLevel.MINOR)
    @Story("Utility Methods")
    @Description("Test getting web URL for VCS root")
    public void shouldGetVcsRootWebUrl() {
        VcsRoot vcsRoot = dataFactory.generateVcsRoot(projectId, "jetbrains.git");
        vcsRootId = adminSteps.createVcsRoot(vcsRoot).getId();

        String url = adminSteps.getVcsRootWebUrl(vcsRootId);

        assertNotNull(url);
        assertTrue(url.contains(vcsRootId));
        assertTrue(url.contains("admin/editVcsRoot"));
    }
}