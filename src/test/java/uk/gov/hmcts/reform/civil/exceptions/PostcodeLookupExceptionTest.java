package uk.gov.hmcts.reform.civil.exceptions;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostcodeLookupExceptionTest {

    @Nested
    class ConstructorTests {

        @Test
        void constructor_WithMessageAndCause_CreatesException() {
            // Arrange
            String message = "Failed to lookup postcode";
            Exception cause = new RuntimeException("Network error");

            // Act
            PostcodeLookupException exception = new PostcodeLookupException(message, cause);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        void constructor_WithNullMessage_CreatesExceptionWithNullMessage() {
            // Arrange
            Exception cause = new RuntimeException("Root cause");

            // Act
            PostcodeLookupException exception = new PostcodeLookupException(null, cause);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        void constructor_WithNullCause_CreatesExceptionWithNullCause() {
            // Arrange
            String message = "Postcode lookup failed";

            // Act
            PostcodeLookupException exception = new PostcodeLookupException(message, null);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void constructor_WithBothNull_CreatesExceptionWithNulls() {
            // Act
            PostcodeLookupException exception = new PostcodeLookupException(null, null);

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
            PostcodeLookupException exception = new PostcodeLookupException("Test", null);

            // Assert
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        void canBeThrownAsRuntimeException() {
            // Act & Assert
            assertThatThrownBy(() -> {
                throw new PostcodeLookupException("Postcode service unavailable", null);
            }).isInstanceOf(RuntimeException.class).isInstanceOf(PostcodeLookupException.class).hasMessage(
                "Postcode service unavailable");
        }
    }

    @Nested
    class StackTraceTests {

        @Test
        void stackTrace_IsPreserved() {
            // Arrange
            Exception cause = new RuntimeException("Original error");
            PostcodeLookupException exception = new PostcodeLookupException("Wrapper error", cause);

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
            PostcodeLookupException exception = new PostcodeLookupException("Lookup failed", cause);

            // Capture the stack trace output
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            java.io.PrintStream printStream = new java.io.PrintStream(outputStream);

            // Act
            exception.printStackTrace(printStream);

            // Assert
            String stackTraceOutput = outputStream.toString();
            for (String s : Arrays.asList(
                "PostcodeLookupException: Lookup failed",
                "Caused by: java.lang.RuntimeException: Root cause"
            )) {
                assertThat(stackTraceOutput).contains(s);
            }
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).isEqualTo("Lookup failed");
        }
    }

    @Nested
    class UsageScenarioTests {

        @Test
        void typicalUsage_ServiceUnavailable() {
            // Arrange
            String postcode = "SW1A 1AA";
            Exception networkError = new RuntimeException("Connection timeout");

            // Act & Assert
            assertThatThrownBy(() -> {
                throw new PostcodeLookupException("Failed to lookup postcode: " + postcode, networkError);
            }).isInstanceOf(PostcodeLookupException.class).hasMessage("Failed to lookup postcode: SW1A 1AA").hasCause(
                networkError);
        }

        @Test
        void typicalUsage_InvalidResponse() {
            // Arrange
            Exception parseError = new IllegalArgumentException("Invalid JSON response");

            // Act & Assert
            assertThatThrownBy(() -> {
                throw new PostcodeLookupException("Invalid response from postcode service", parseError);
            }).isInstanceOf(PostcodeLookupException.class).hasMessage("Invalid response from postcode service").hasCauseInstanceOf(
                IllegalArgumentException.class);
        }
    }
}
