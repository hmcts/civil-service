package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
