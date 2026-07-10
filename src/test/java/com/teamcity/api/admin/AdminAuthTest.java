package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.exceptions.ApiException;
import com.teamcity.core.exceptions.DuplicateResourceException;
import com.teamcity.core.exceptions.ValidationException;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.UserSteps;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.*;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.teamcity.core.utils.LogUtils.printable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@Epic("Admin API")
@Feature("User Management")
@Tag("admin")
@Tag("users")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminAuthTest extends BaseApiTest {

    private UserSteps userSteps;

    @BeforeEach
    void initSteps() {
        userSteps = new UserSteps(adminClient);
    }

    @Test
    @Order(1)
    @Tag("smoke")
    @Tag("critical")
    @Tag("auth")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Create user with valid credentials")
    @Description("Verifies that a user can be created with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Create user")
    void shouldCreateUserWithValidCredentials() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created).isNotNull();
        softly.assertThat(created.getUsername()).isEqualTo(user.getUsername());
        softly.assertThat(created.getEmail()).isEqualTo(user.getEmail());
        softly.assertThat(created.getName()).isEqualTo(user.getName());
        softly.assertThat(created.getId()).isNotNull();
        softly.assertThat(created.getHref()).isNotBlank();
        softly.assertAll();

        log.info("✅ User created successfully: {}", created.getUsername());
    }

    @Test
    @Order(2)
    @Tag("smoke")
    @Tag("critical")
    @Tag("auth")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Get user by username")
    @Description("Verifies that user can be retrieved by username")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Get user")
    void shouldGetUserByUsername() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        User retrieved = userSteps.getUser(created.getUsername());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(retrieved).isNotNull();
        softly.assertThat(retrieved.getUsername()).isEqualTo(created.getUsername());
        softly.assertThat(retrieved.getEmail()).isEqualTo(created.getEmail());
        softly.assertAll();

        log.info("✅ User retrieved: {}", retrieved.getUsername());
    }

    @Test
    @Order(3)
    @Tag("smoke")
    @Tag("critical")
    @Tag("auth")
    @Tag("positive")
    @DisplayName("✅ [SMOKE] Delete user")
    @Description("Verifies that user can be deleted")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Delete user")
    void shouldDeleteUser() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);

        userSteps.deleteUser(created.getUsername());

        assertThatThrownBy(() -> userSteps.getUser(created.getUsername()))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

        log.info("✅ User deleted: {}", created.getUsername());
    }

    @Test
    @Order(4)
    @Tag("positive")
    @Tag("normal")
    @Tag("auth")
    @DisplayName("✅ Create user with minimal required fields")
    @Description("Verifies that a user can be created with only required fields")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create user")
    void shouldCreateUserWithMinimalFields() {
        String username = dataFactory.generateUniqueUsername();
        User user = User.builder()
                .username(username)
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .build();

        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getUsername()).isEqualTo(username);
        softly.assertThat(created.getHref()).isNotBlank();
        softly.assertThat(created.getEmail()).isNullOrEmpty();
        softly.assertThat(created.getName()).isNullOrEmpty();
        softly.assertAll();
    }

    @Test
    @Order(5)
    @Tag("positive")
    @Tag("normal")
    @Tag("validation")
    @DisplayName("✅ Create user with invalid email (TeamCity allows)")
    @Description("Verifies that TeamCity allows invalid email format")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create user validation")
    void shouldCreateUserWithInvalidEmail() {
        User user = dataFactory.createRandomUser();
        user.setEmail("invalid-email");

        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getEmail()).isEqualTo("invalid-email");
        softly.assertThat(created.getId()).isNotNull();
        softly.assertAll();

        log.info("✅ User created with invalid email (TeamCity does not validate email format)");
    }

    @Test
    @Order(6)
    @Tag("positive")
    @Tag("normal")
    @Tag("validation")
    @DisplayName("✅ Create user with short password (TeamCity allows)")
    @Description("Verifies that TeamCity allows short password")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create user validation")
    void shouldCreateUserWithShortPassword() {
        User user = dataFactory.createRandomUser();
        user.setPassword("123");

        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getUsername()).isNotBlank();
        softly.assertThat(created.getId()).isNotNull();
        softly.assertAll();

        log.info("✅ User created with short password (TeamCity does not validate password length)");
    }

    @Test
    @Order(7)
    @Tag("negative")
    @Tag("critical")
    @Tag("conflict")
    @DisplayName("❌ Create user with duplicate username → 409")
    @Description("Verifies that duplicate username creation is rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create user validation")
    void shouldNotCreateUserWithDuplicateUsername() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        assertThatThrownBy(() -> userSteps.createUser(user))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        log.info("✅ Duplicate user creation correctly rejected");
    }

    @Test
    @Order(8)
    @Tag("negative")
    @Tag("critical")
    @Tag("validation")
    @DisplayName("❌ Create user with empty username → 400")
    @Description("Verifies that empty username is rejected")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create user validation")

    void shouldNotCreateUserWithEmptyUsername() {

        User invalidUser = User.builder()
                .username("")
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .email("test@test.com")
                .build();
        assertThatThrownBy(() -> userSteps.createUser(invalidUser))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username must not be empty");

        log.info("✅ Empty username correctly rejected");

    }

    @ParameterizedTest
    @Order(9)
    @Tag("negative")
    @Tag("normal")
    @Tag("system-behavior")
    @Tag("known-issue")
    @ValueSource(strings = {" ", "\t"})
    @Disabled("TC-API-004: TeamCity returns HTTP 500 for whitespace-only usernames")
    @DisplayName("⚠️ Create user with whitespace username → 500 (known API issue)")
    @Description("Verifies current TeamCity behavior for whitespace-only usernames")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create user validation")

    void shouldRejectWhitespaceUsernameWithServerError(String whitespaceUsername) {

        User user = User.builder()
                .username(whitespaceUsername)
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .email("test@test.com")
                .build();

        assertThatThrownBy(() -> userSteps.createUser(user))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(500);

        log.info("⚠️ Whitespace username '{}' rejected with HTTP 500",
                printable(whitespaceUsername));

    }


    @ParameterizedTest
    @Order(10)
    @Tag("positive")
    @Tag("parameterized")
    @Tag("validation")
    @MethodSource("emailProvider")
    @DisplayName("🔄 Create user with various email formats")
    @Description("Verifies user creation with different email formats")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create user validation")

    void shouldCreateUserWithVariousEmails(String email) {
        User user = dataFactory.createRandomUser();
        user.setEmail(email);
        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        if (email == null || email.isEmpty()) {
            assertThat(created.getEmail())

                    .as("Empty email should be normalized to null by TeamCity")
                    .isNull();

        } else {

            assertThat(created.getEmail())
                    .as("Email should match provided value")
                    .isEqualTo(email);

        }

        log.info("✅ User created with email: {}", email);

    }

    static Stream<Arguments> emailProvider() {
        return Stream.of(

                Arguments.of("valid@example.com"),
                Arguments.of("valid+test@example.com"),
                Arguments.of("invalid-email"),
                Arguments.of(""),
                Arguments.of((String) null)

        );

    }


    @ParameterizedTest
    @Order(11)
    @Tag("positive")
    @Tag("parameterized")
    @Tag("validation")
    @MethodSource("passwordProvider")
    @DisplayName("🔄 Create user with various password lengths")
    @Description("Verifies user creation with different password lengths")
    @Severity(SeverityLevel.NORMAL)
    @Story("Create user validation")
    void shouldCreateUserWithVariousPasswords(String password, boolean shouldSucceed) {
        User user = dataFactory.createRandomUser();
        user.setPassword(password);

        if (shouldSucceed) {
            User created = userSteps.createUser(user);
            trackUser(created.getUsername());
            assertThat(created.getUsername()).isNotBlank();
            log.info("✅ User created with password length: {}", password != null ? password.length() : 0);
        } else {
            assertThatThrownBy(() -> userSteps.createUser(user))
                    .isInstanceOf(Exception.class);
            log.info("✅ User creation with password length '{}' correctly rejected",
                    password != null ? password.length() : 0);
        }
    }

    static Stream<Arguments> passwordProvider() {
        return Stream.of(
                Arguments.of("123", true),
                Arguments.of("TestPass123!", true),
                Arguments.of("a".repeat(100), true),
                Arguments.of("", true),
                Arguments.of(null, true)
        );
    }
}