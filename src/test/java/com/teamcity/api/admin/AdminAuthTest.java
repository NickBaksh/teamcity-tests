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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@Epic("Admin API")
@Feature("User Management")
@Tag("admin")
@Tag("users")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminAuthTest extends BaseApiTest {

    @Test
    @Order(1)
    @DisplayName("✅ [SMOKE] Create user with valid credentials")
    void shouldCreateUserWithValidCredentials() {

        User created = userSteps(adminClient())
                .createRandomUser();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(created).isNotNull();
        softly.assertThat(created.getId()).isNotNull();
        softly.assertThat(created.getUsername()).isNotBlank();
        softly.assertThat(created.getHref()).isNotBlank();

        softly.assertAll();

        log.info("User created: {}", created.getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("✅ [SMOKE] Get user by username")
    void shouldGetUserByUsername() {

        User expected = userSteps(adminClient())
                .createRandomUser();

        User actual = userSteps(adminClient())
                .getUser(expected.getUsername());

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(actual).isNotNull();
        softly.assertThat(actual.getUsername())
                .isEqualTo(expected.getUsername());
        softly.assertThat(actual.getEmail())
                .isEqualTo(expected.getEmail());

        softly.assertAll();
    }

    @Test
    @Order(3)
    @DisplayName("✅ [SMOKE] Delete user")
    void shouldDeleteUser() {

        User user = userSteps(adminClient())
                .createRandomUser();

        userSteps(adminClient())
                .deleteUser(user.getUsername());

        assertThatThrownBy(() ->
                userSteps(adminClient())
                        .getUser(user.getUsername()))
                .isInstanceOf(ApiException.class)
                .extracting("statusCode")
                .isEqualTo(404);
    }

    @Test
    @Order(4)
    @DisplayName("Create user with minimal required fields")
    void shouldCreateUserWithMinimalFields() {

        String username = dataFactory.generateUniqueUsername();

        User user = User.builder()
                .username(username)
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .build();

        User created = userSteps(adminClient())
                .createUser(user);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(created.getUsername())
                .isEqualTo(username);
        softly.assertThat(created.getHref())
                .isNotBlank();
        softly.assertThat(created.getEmail())
                .isNullOrEmpty();
        softly.assertThat(created.getName())
                .isNullOrEmpty();

        softly.assertAll();
    }

    @Test
    @Order(5)
    @DisplayName("Create user with invalid email (TeamCity allows)")
    void shouldCreateUserWithInvalidEmail() {

        User user = dataFactory.createRandomUser();
        user.setEmail("invalid-email");

        User created = userSteps(adminClient())
                .createUser(user);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(created.getEmail())
                .isEqualTo("invalid-email");
        softly.assertThat(created.getId())
                .isNotNull();

        softly.assertAll();
    }

    @Test
    @Order(6)
    @DisplayName("Create user with short password (TeamCity allows)")
    void shouldCreateUserWithShortPassword() {

        User user = dataFactory.createRandomUser();
        user.setPassword("123");

        User created = userSteps(adminClient())
                .createUser(user);

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(created.getUsername())
                .isNotBlank();
        softly.assertThat(created.getId())
                .isNotNull();

        softly.assertAll();
    }

    @Test
    @Order(7)
    @DisplayName("Create user with duplicate username")
    void shouldNotCreateUserWithDuplicateUsername() {

        User user = dataFactory.createRandomUser();

        userSteps(adminClient())
                .createUser(user);

        assertThatThrownBy(() ->
                userSteps(adminClient())
                        .createUser(user))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @ParameterizedTest
    @Order(8)
    @ValueSource(strings = {"", " ", "\t"})
    @DisplayName("Create user with invalid username")
    void shouldNotCreateUserWithInvalidUsername(String invalidUsername) {

        User invalidUser = User.builder()
                .username(invalidUsername)
                .password(TestDataFactory.DEFAULT_PASSWORD)
                .email("test@test.com")
                .build();

        assertThatThrownBy(() ->
                userSteps(adminClient())
                        .createUser(invalidUser))
                .isInstanceOf(ValidationException.class);
    }

    @ParameterizedTest
    @Order(9)
    @MethodSource("emailProvider")
    @DisplayName("Create user with various email formats")
    void shouldCreateUserWithVariousEmails(String email, boolean shouldSucceed) {

        User user = dataFactory.createRandomUser();
        user.setEmail(email);

        if (shouldSucceed) {

            User created = userSteps(adminClient())
                    .createUser(user);

            assertThat(created.getEmail())
                    .isEqualTo(email);

        } else {

            assertThatThrownBy(() ->
                    userSteps(adminClient())
                            .createUser(user))
                    .isInstanceOf(Exception.class);
        }
    }

    static Stream<Arguments> emailProvider() {
        return Stream.of(
                Arguments.of("valid@example.com", true),
                Arguments.of("valid+test@example.com", true),
                Arguments.of("invalid-email", true),
                Arguments.of("", true),
                Arguments.of(null, true)
        );
    }

    @ParameterizedTest
    @Order(10)
    @MethodSource("passwordProvider")
    @DisplayName("Create user with various password lengths")
    void shouldCreateUserWithVariousPasswords(String password, boolean shouldSucceed) {

        User user = dataFactory.createRandomUser();
        user.setPassword(password);

        if (shouldSucceed) {

            User created = userSteps(adminClient())
                    .createUser(user);

            assertThat(created.getUsername())
                    .isNotBlank();

        } else {

            assertThatThrownBy(() ->
                    userSteps(adminClient())
                            .createUser(user))
                    .isInstanceOf(Exception.class);
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