package uk.gov.hmcts.reform.civil.sendgrid;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailSendFailedExceptionTest {

    @Nested
    class ConstructorTests {

        @Test
        void constructor_WithMessageAndCause_CreatesException() {
            // Arrange
            String message = "Failed to send email";
            Exception cause = new RuntimeException("SMTP connection failed");

            // Act
            EmailSendFailedException exception = new EmailSendFailedException(message, cause);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        void constructor_WithCauseOnly_CreatesException() {
            // Arrange
            Exception cause = new RuntimeException("SendGrid API error");

            // Act
            EmailSendFailedException exception = new EmailSendFailedException(cause);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("java.lang.RuntimeException: SendGrid API error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        void constructor_WithNullMessage_CreatesExceptionWithNullMessage() {
            // Arrange
            Exception cause = new RuntimeException("Root cause");

            // Act
            EmailSendFailedException exception = new EmailSendFailedException(null, cause);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        void constructor_WithNullCause_CreatesExceptionWithNullCause() {
            // Arrange
            String message = "Email send failed";

            // Act
            EmailSendFailedException exception = new EmailSendFailedException(message, null);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void constructor_WithBothNull_CreatesExceptionWithNulls() {
            // Act
            EmailSendFailedException exception = new EmailSendFailedException(null, null);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void constructor_WithNullCauseOnly_CreatesExceptionWithNullCause() {
            // Act
            EmailSendFailedException exception = new EmailSendFailedException(null);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    class InheritanceTests {

        @Test
        void extendsRuntimeException() {
            // Arrange
            EmailSendFailedException exception = new EmailSendFailedException("Test", null);

            // Assert
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        void canBeThrownAsRuntimeException() {
            // Act & Assert
            assertThatThrownBy(() -> {
                throw new EmailSendFailedException("Email service unavailable", null);
            }).isInstanceOf(RuntimeException.class).isInstanceOf(EmailSendFailedException.class).hasMessage(
                "Email service unavailable");
        }
    }

    @Nested
    class StackTraceTests {

        @Test
        void stackTrace_IsPreserved() {
            // Arrange
            Exception cause = new RuntimeException("Original error");
            EmailSendFailedException exception = new EmailSendFailedException("Wrapper error", cause);

            // Act
            StackTraceElement[] stackTrace = exception.getStackTrace();

            // Assert
            assertThat(stackTrace).isNotEmpty();
            assertThat(exception.getCause().getStackTrace()).isNotEmpty();
        }

        @Test
        void printStackTrace_IncludesCause() {
            // Arrange
            Exception cause = new RuntimeException("Root cause");
            LocationRefDataException exception = new LocationRefDataException(cause);

            // Capture the stack trace output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            // Act
            exception.printStackTrace(printStream);

            // Assert
            String stackTraceOutput = outputStream.toString();
            assertThat(stackTraceOutput).contains("LocationRefDataException: java.lang.RuntimeException: Root cause")
                .contains("Caused by: java.lang.RuntimeException: Root cause");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).isEqualTo("java.lang.RuntimeException: Root cause");
        }
    }

    @Nested
    class UsageScenarioTests {

        @Test
        void typicalUsage_ApiError() {
            // Arrange
            Exception apiError = new RuntimeException("401 Unauthorized");

            // Act & Assert
            assertThatThrownBy(() -> {
                throw new EmailSendFailedException("Failed to send notification email", apiError);
            }).isInstanceOf(EmailSendFailedException.class).hasMessage("Failed to send notification email").hasCause(
                apiError);
        }

        @Test
        void typicalUsage_NetworkError() {
            // Arrange
            Exception networkError = new RuntimeException("Connection timeout");

            // Act & Assert
            assertThatThrownBy(() -> {
                throw new EmailSendFailedException(networkError);
            }).isInstanceOf(EmailSendFailedException.class).hasMessageContaining("Connection timeout").hasCause(
                networkError);
        }

        @Test
        void typicalUsage_ValidationError() {
            // Arrange
            IllegalArgumentException validationError = new IllegalArgumentException("Invalid email address");

            // Act & Assert
            assertThatThrownBy(() -> {
                throw new EmailSendFailedException("Email validation failed", validationError);
            }).isInstanceOf(EmailSendFailedException.class).hasMessage("Email validation failed").hasCauseInstanceOf(
                IllegalArgumentException.class);
        }
    }
}
