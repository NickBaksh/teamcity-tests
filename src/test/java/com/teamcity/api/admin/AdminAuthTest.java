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
@Feature("User Management")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminAuthTest extends BaseApiTest {

    private UserSteps userSteps;

    @BeforeEach
    void initSteps() {
        userSteps = new UserSteps(adminClient);
    }

    @Test
    @Description("Verifies that a user can be created with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
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

    }

    @Test
    @Description("Verifies that user can be retrieved by username")
    @Severity(SeverityLevel.BLOCKER)
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

    }

    @Test
    @DisplayName("[SMOKE] Delete user")
    @Severity(SeverityLevel.BLOCKER)
    void shouldDeleteUser() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);

        userSteps.deleteUser(created.getUsername());

        assertThatThrownBy(() -> userSteps.getUser(created.getUsername()))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);

    }

    @Test
    @Description("Verifies that a user can be created with only required fields")
    @Severity(SeverityLevel.NORMAL)
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
    @Description("Verifies that TeamCity allows invalid email format")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateUserWithInvalidEmail() {
        User user = dataFactory.createRandomUser();
        user.setEmail("invalid-email");

        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getEmail()).isEqualTo("invalid-email");
        softly.assertThat(created.getId()).isNotNull();
        softly.assertAll();

    }

    @Test
    @Description("Verifies that TeamCity allows short password")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateUserWithShortPassword() {
        User user = dataFactory.createRandomUser();
        user.setPassword("123");

        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(created.getUsername()).isNotBlank();
        softly.assertThat(created.getId()).isNotNull();
        softly.assertAll();

    }

    @Test
    @Description("Verifies that duplicate username creation is rejected")
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateUserWithDuplicateUsername() {
        User user = dataFactory.createRandomUser();
        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        assertThatThrownBy(() -> userSteps.createUser(user))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

    }

    @Test
    @Description("Verifies that empty username is rejected")
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateUserWithEmptyUsername() {

        User invalidUser = User.builder()
                .username("")
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .email("test@test.com")
                .build();
        assertThatThrownBy(() -> userSteps.createUser(invalidUser))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username must not be empty");

    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t"})
    @Disabled("TC-API-004: TeamCity returns HTTP 500 for whitespace-only usernames")
    @Description("Verifies current TeamCity behavior for whitespace-only usernames")
    @Severity(SeverityLevel.NORMAL)

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

        log.info("Whitespace username '{}' rejected with HTTP 500",
                printable(whitespaceUsername));

    }

    @ParameterizedTest
    @MethodSource("emailProvider")
    @Description("Verifies user creation with different email formats")
    @Severity(SeverityLevel.NORMAL)

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

        log.info("User created with email: {}", email);

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
    @MethodSource("passwordProvider")
    @Description("Verifies user creation with different password lengths")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateUserWithVariousPasswords(String password, boolean shouldSucceed) {
        User user = dataFactory.createRandomUser();
        user.setPassword(password);

        if (shouldSucceed) {
            User created = userSteps.createUser(user);
            trackUser(created.getUsername());
            assertThat(created.getUsername()).isNotBlank();
            log.info("User created with password length: {}", password != null ? password.length() : 0);
        } else {
            assertThatThrownBy(() -> userSteps.createUser(user))
                    .isInstanceOf(Exception.class);
            log.info("User creation with password length '{}' correctly rejected",
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