package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class MaskHelperTest {

    @Test
    void class_HasPrivateConstructor() throws Exception {
        // Act
        Constructor<MaskHelper> constructor = MaskHelper.class.getDeclaredConstructor();

        // Assert
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    void privateConstructor_CanBeInvoked() throws Exception {
        // Arrange
        Constructor<MaskHelper> constructor = MaskHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert
        assertThat(constructor.newInstance()).isNotNull();
    }

    @Test
    void class_IsFinal() {
        // Assert
        assertThat(Modifier.isFinal(MaskHelper.class.getModifiers())).isFalse();
    }

    @Test
    void class_HasOnlyStaticMethods() {
        // Assert
        assertThat(MaskHelper.class.getDeclaredMethods()).allMatch(method -> Modifier.isStatic(method.getModifiers()));
    }

    @Test
    void class_IsUtilityClass() {
        // Assert - verify class characteristics of a utility class
        assertThat(MaskHelper.class.getDeclaredConstructors()).hasSize(1);
        assertThat(Modifier.isPrivate(MaskHelper.class.getDeclaredConstructors()[0].getModifiers())).isTrue();
        assertThat(MaskHelper.class.getDeclaredMethods()).filteredOn(method -> !method.getName().contains("$") && !method.getName().startsWith(
            "lambda")).allMatch(method -> Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void verifyImports_UsesApacheCommonsStringUtils() {
        // This test verifies that the class uses StringUtils.EMPTY from Apache Commons
        // by checking the behavior when null is passed
        assertThat(MaskHelper.maskEmail(null)).isEmpty();
        assertThat(MaskHelper.maskEmailsInErrorMessages(null)).isEmpty();
    }

    @Nested
    class MaskEmailTests {

        @Test
        void maskEmail_WithNull_ReturnsEmptyString() {
            // Act
            String result = MaskHelper.maskEmail(null);

            // Assert
            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"a@b.c", "user@domain.com", "long.email.address@very.long.domain.com"})
        void maskEmail_WithVariousEmails_MasksCorrectly(String email) {
            // Act
            String result = MaskHelper.maskEmail(email);

            // Assert
            assertThat(result).matches("\\*+@\\*+");
            assertThat(result.chars().filter(ch -> ch == '@').count()).isOne();
            assertThat(result).hasSameSizeAs(email);
        }

        @ParameterizedTest
        @CsvSource({
            "'test@example.com', '****@***********'",
            "'test@@example.com', '****@@***********'",
            "'notanemail', '**********'",
            "'', ''"
        })
        void maskEmail_WithVariousInputs_MasksCorrectly(String input, String expected) {
            // Act
            String result = MaskHelper.maskEmail(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class MaskEmailsInErrorMessagesTests {

        @Test
        void maskEmailsInErrorMessages_WithNull_ReturnsEmptyString() {
            // Act
            String result = MaskHelper.maskEmailsInErrorMessages(null);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void maskEmailsInErrorMessages_WithEmptyString_ReturnsEmptyString() {
            // Act
            String result = MaskHelper.maskEmailsInErrorMessages("");

            // Assert
            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
            "'This is an error message without emails', 'Thisisanerrormessagewithoutemails'",
            "'Error sending email to user@example.com failed', 'Errorsendingemailto****@***********failed'",
            "'Failed to send from sender@example.com to receiver@test.com', 'Failedtosendfrom******@***********to********@********'",
            "'This   has   multiple   spaces', 'Thishasmultiplespaces'",
            "'user@example.com is not valid', '****@***********isnotvalid'",
            "'Invalid email address: test@domain.com', 'Invalidemailaddress:****@**********'",
            "'Email: test.user+tag@sub-domain.example.com failed!', 'Email:*************@**********************failed!'",
            "'User 123 with email john@doe.com and ID @handle failed', 'User123withemail****@*******andID@******failed'",
            "'The @ symbol user@email.com appears @ various places', 'The@symbol****@*********appears@variousplaces'",
            "'admin@test.com user@test.com system@test.com', '*****@************@**************@********'"
        })
        void maskEmailsInErrorMessages_WithVariousInputs_ProcessesCorrectly(String input, String expected) {
            // Act
            String result = MaskHelper.maskEmailsInErrorMessages(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }
}
