package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

class ValidateEmailServiceTest {

    private final ValidateEmailService validateEmailService = new ValidateEmailService();

    private static final String ERROR_MESSAGE = "Enter an email address in the correct format, "
        + "for example john.smith@example.com";

    @ParameterizedTest
    @MethodSource("validEmailAddresses")
    void shouldNotReturnAnErrorMessageIfEmailIsValid(String email) {
        assertThat(validateEmailService.validate(email)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEmailAddresses")
    void shouldReturnAnErrorMessageIfEmailIsInvalid(String email) {
        assertThat(validateEmailService.validate(email)).contains(ERROR_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnErrorForNullOrEmptyEmail(String email) {
        assertThat(validateEmailService.validate(email)).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldHandleEmailWithWhitespace() {
        assertThat(validateEmailService.validate("  email@domain.com  ")).isEmpty();
    }

    @Test
    void shouldRejectEmailWithHostnamePartTooLong() {
        // Create a hostname part that's 64 characters (limit is 63)
        String longPart = "a".repeat(64);
        String email = format("email@%s.com", longPart);
        assertThat(validateEmailService.validate(email)).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldRejectEmailWithHostnameTooLong() {
        // Create a hostname that's 254 characters (limit is 253)
        String longHostname = "a".repeat(60) + "." + "b".repeat(60) + "." + "c".repeat(60) + "." + "d".repeat(60) + "." + "e".repeat(10) + ".com";
        String email = "email@" + longHostname;
        assertThat(validateEmailService.validate(email)).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldHandleInternationalDomainNames() {
        // Test IDN conversion
        assertThat(validateEmailService.validate("test@münchen.de")).isEmpty();
    }

    @Test
    void shouldRejectInvalidIDNConversion() {
        // This should fail IDN conversion
        String email = "test@" + "\u0000invalid.com";
        assertThat(validateEmailService.validate(email)).contains(ERROR_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test@xn--mnchen-3ya.de",  // IDN encoded domain
        "test@example.xn--jxalpdlp", // Greek TLD
        "test@xn--a-ecp.com"        // Domain with IDN prefix
    })
    void shouldAcceptValidIDNDomains(String email) {
        assertThat(validateEmailService.validate(email)).isEmpty();
    }

    @Test
    void shouldRejectEmailWithSingleHostPart() {
        assertThat(validateEmailService.validate("email@localhost")).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldAcceptMaxLengthEmail() {
        // Create an email exactly 320 characters long
        // Local part: 64 chars, @ = 1 char, domain needs to be 255 chars
        // But HOST_MAX_LENGTH is 253, not 255
        String localPart = "a".repeat(64);
        // Domain must be <= 253 chars
        // 63 + 1 + 63 + 1 + 63 + 1 + 57 + 1 + 3 = 253
        String domainPart1 = "b".repeat(63);
        String domainPart2 = "c".repeat(63);
        String domainPart3 = "d".repeat(63);
        String domainPart4 = "e".repeat(57);
        String domain = domainPart1 + "." + domainPart2 + "." + domainPart3 + "." + domainPart4 + ".com";
        String email = localPart + "@" + domain;
        // 64 + 1 + 253 = 318 chars total
        assertThat(email).hasSize(318);
        assertThat(validateEmailService.validate(email)).isEmpty();
    }

    @Test
    void shouldRejectEmailExceedingMaxLength() {
        // Create an email 321 characters long
        // Local part: 65 chars (exceeds 64 limit), @ = 1 char, domain = 255 chars
        String localPart = "a".repeat(65);
        // Create a valid domain that's 255 chars
        String domainPart1 = "b".repeat(63);
        String domainPart2 = "c".repeat(63);
        String domainPart3 = "d".repeat(63);
        String domainPart4 = "e".repeat(59);
        String domain = domainPart1 + "." + domainPart2 + "." + domainPart3 + "." + domainPart4 + ".com";
        String email = localPart + "@" + domain;
        assertThat(email).hasSize(321);
        assertThat(validateEmailService.validate(email)).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldAcceptValidTwoLetterTLD() {
        assertThat(validateEmailService.validate("email@example.uk")).isEmpty();
    }

    @Test
    void shouldRejectInvalidTLD() {
        assertThat(validateEmailService.validate("email@example.a")).contains(ERROR_MESSAGE);
    }

    //See https://github.com/alphagov/notifications-utils/blob/master/tests/test_recipient_validation.py#L104-L121
    private static Stream<String> validEmailAddresses() {
        return Stream.of(
            "email@domain.com",
            "email@domain.COM",
            "firstname.lastname@domain.com",
            "email@subdomain.domain.com",
            "firstname+lastname@domain.com",
            "1234567890@domain.com",
            "email@domain-one.com",
            "_______@domain.com",
            "email@domain.name",
            "email@domain.superlongtld",
            "email@domain.co.jp",
            "firstname-lastname@domain.com",
            "info@german-financial-services.vermögensberatung",
            "info@german-financial-services.reallylongarbitrarytldthatiswaytoohugejustincase",
            "japanese-info@例え.テスト",
            format("%s@example.com", "a".repeat(64)),
            format("%s@example.com", "a".repeat(63)));
    }

    //See https://github.com/alphagov/notifications-utils/blob/master/tests/test_recipient_validation.py#L122-L152
    private static Stream<String> invalidEmailAddresses() {
        return Stream.of(
            "invalid@tld.co.k",
            "<John Doe> johndoe@email.com",
            "very.unusual.”@”.unusual.com@example.com",
            "very.”(),:;<>[]”.VERY.”very@\\\\ \"very”.unusual@strange.example.com",
            "email@123.123.123.123",
            "email@[123.123.123.123]",
            "plainaddress",
            "@no-local-part.com",
            "Outlook Contact <outlook-contact@domain.com>",
            "no-at.domain.com",
            "no-tld@domain",
            ";beginning-semicolon@domain.co.uk",
            "middle-semicolon@domain.co;uk",
            "trailing-semicolon@domain.com;",
            "\"email+leading-quotes@domain.com",
            "email+middle\"-quotes@domain.com",
            "\"quoted-local-part\"@domain.com",
            "\"quoted@domain.com\"",
            "lots-of-dots@domain..gov..uk",
            "two-dots..in-local@domain.com",
            "multiple@domains@domain.com",
            "spaces in local@domain.com",
            "spaces-in-domain@dom ain.com",
            "underscores-in-domain@dom_ain.com",
            "pipe-in-domain@example.com|gov.uk",
            "comma,in-local@gov.uk",
            "comma-in-domain@domain,gov.uk",
            "pound-sign-in-local£@domain.com",
            "local-with-’-apostrophe@domain.com",
            "local-with-”-quotes@domain.com",
            "domain-starts-with-a-dot@.domain.com",
            "brackets(in)local@domain.com",
            ".Douglas.@hmcts.net",
            "Douglas.@hmcts.net",
            "firstname.o'lastname@domain.com",
            format("%s@example.com", "a".repeat(65)),
            format("email-too-long-%s@example.com", "a".repeat(320)));
    }
}
