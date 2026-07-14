package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.assertions.ApiAssertions;
import com.teamcity.core.exceptions.ValidationException;
import com.teamcity.core.models.User;
import com.teamcity.core.testdata.InvalidTestData;
import com.teamcity.core.testdata.TestDataValues;
import com.teamcity.core.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("admin")
public class AdminUsersTest extends BaseApiTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldCreateUserWithValidCredentials() {
        User request = dataFactory.createRandomUser();

        User created = givenUser(request);

        ApiAssertions.assertUserCreated(request, created);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldGetUserByUsername() {
        User created = givenUser();

        User retrieved = userSteps.getUser(created.getUsername());

        ApiAssertions.assertUsersEqual(created, retrieved);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    void shouldDeleteUser() {
        User created = userSteps.createUser(dataFactory.createRandomUser());

        userSteps.deleteUser(created.getUsername());

        ApiAssertions.assertNotFound(() -> userSteps.getUser(created.getUsername()));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateUserWithMinimalFields() {
        User request = dataFactory.createMinimalUser();

        User created = givenUser(request);

        assertThat(created.getUsername()).isEqualTo(request.getUsername());
        assertThat(created.getHref()).isNotBlank();
        assertThat(created.getEmail()).isNullOrEmpty();
        assertThat(created.getName()).isNullOrEmpty();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("Documents TeamCity behavior: invalid email format is accepted")
    void shouldCreateUserWithInvalidEmail() {
        User request = dataFactory.createUserWithEmail(TestDataValues.INVALID_EMAIL);

        User created = givenUser(request);

        assertThat(created.getEmail()).isEqualTo(TestDataValues.INVALID_EMAIL);
        assertThat(created.getId()).isNotNull();
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateUserWithShortPassword() {
        User request = dataFactory.createUserWithPassword(TestDataValues.SHORT_PASSWORD);

        User created = givenUser(request);

        assertThat(created.getUsername()).isNotBlank();
        assertThat(created.getId()).isNotNull();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateUserWithDuplicateUsername() {
        User request = dataFactory.createRandomUser();
        givenUser(request);

        ApiAssertions.assertDuplicate(() -> userSteps.createUser(request));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    void shouldNotCreateUserWithEmptyUsername() {
        User invalidUser = InvalidTestData.userWithEmptyUsername();

        assertThatThrownBy(() -> userSteps.createUser(invalidUser))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(TestDataValues.MSG_USERNAME_EMPTY);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t"})
    @Disabled("TC-API-004: TeamCity returns HTTP 500 for whitespace-only usernames")
    @Severity(SeverityLevel.NORMAL)
    void shouldRejectWhitespaceUsernameWithServerError(String whitespaceUsername) {
        User user = InvalidTestData.userWithUsername(whitespaceUsername);

        ApiAssertions.assertStatus(() -> userSteps.createUser(user), 500);
    }

    @ParameterizedTest
    @MethodSource("emailCases")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateUserWithEmail(String inputEmail, String expectedEmail) {
        User request = dataFactory.createUserWithEmail(inputEmail);

        User created = givenUser(request);

        assertThat(created.getEmail()).isEqualTo(expectedEmail);
    }

    @ParameterizedTest
    @MethodSource("acceptedPasswords")
    @Severity(SeverityLevel.NORMAL)
    void shouldCreateUserWithPassword(String password) {
        User request = dataFactory.createUserWithPassword(password);

        User created = givenUser(request);

        assertThat(created.getUsername()).isNotBlank();
        assertThat(created.getId()).isNotNull();
    }

    static Stream<Arguments> emailCases() {
        return Stream.of(
                Arguments.of(TestDataValues.VALID_EMAIL, TestDataValues.VALID_EMAIL),
                Arguments.of(TestDataValues.VALID_PLUS_EMAIL, TestDataValues.VALID_PLUS_EMAIL),
                Arguments.of(TestDataValues.INVALID_EMAIL, TestDataValues.INVALID_EMAIL),
                Arguments.of("", null),
                Arguments.of(null, null)
        );
    }

    static Stream<String> acceptedPasswords() {
        return Stream.of(
                TestDataValues.SHORT_PASSWORD,
                TestDataFactory.DEFAULT_PASSWORD,
                TestDataValues.LONG_PASSWORD,
                "",
                null
        );
    }
}
