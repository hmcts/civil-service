package uk.gov.hmcts.reform.civil.referencedata;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationRefDataExceptionTest {

    @Nested
    class ConstructorTests {

        @Test
        void constructor_WithMessage_CreatesException() {
            // Arrange
            String message = "Failed to retrieve location data";

            // Act
            LocationRefDataException exception = new LocationRefDataException(message);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void constructor_WithCause_CreatesException() {
            // Arrange
            Exception cause = new RuntimeException("API error");

            // Act
            LocationRefDataException exception = new LocationRefDataException(cause);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("java.lang.RuntimeException: API error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        void constructor_WithNullMessage_CreatesExceptionWithNullMessage() {
            // Act
            LocationRefDataException exception = new LocationRefDataException((String) null);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void constructor_WithNullCause_CreatesExceptionWithNullCause() {
            // Act
            LocationRefDataException exception = new LocationRefDataException((Exception) null);

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void constructor_WithEmptyMessage_CreatesExceptionWithEmptyMessage() {
            // Act
            LocationRefDataException exception = new LocationRefDataException("");

            // Assert
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEmpty();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        void constructor_WithDifferentExceptionTypes_WorksCorrectly() {
            // Arrange
            IllegalArgumentException cause1 = new IllegalArgumentException("Invalid court code");
            NullPointerException cause2 = new NullPointerException("Location is null");

            // Act
            LocationRefDataException exception1 = new LocationRefDataException(cause1);
            LocationRefDataException exception2 = new LocationRefDataException(cause2);

            // Assert
            assertThat(exception1.getCause()).isInstanceOf(IllegalArgumentException.class);
            assertThat(exception2.getCause()).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class InheritanceTests {

        @Test
        void extendsRuntimeException() {
            // Arrange
            LocationRefDataException exception = new LocationRefDataException("Test");

            // Assert
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        void canBeThrownAsRuntimeException() {
            // Act & Assert
            assertThatThrownBy(() -> {
                throw new LocationRefDataException("Location service unavailable");
            }).isInstanceOf(RuntimeException.class).isInstanceOf(LocationRefDataException.class).hasMessage(
                "Location service unavailable");
        }
    }

    @Nested
    class StackTraceTests {

        @Test
        void stackTrace_IsPreserved() {
            // Arrange
            Exception cause = new RuntimeException("Original error");
            LocationRefDataException exception = new LocationRefDataException(cause);

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
        void typicalUsage_ServiceError() {
            // Arrange
            String courtCode = "123456";

            // Act & Assert
            assertThatThrownBy(() -> {
                throw new LocationRefDataException("Failed to find location for court code: " + courtCode);
            }).isInstanceOf(LocationRefDataException.class).hasMessage("Failed to find location for court code: 123456").hasNoCause();
        }

        @Test
        void typicalUsage_ApiError() {
            // Arrange
            Exception apiError = new RuntimeException("500 Internal Server Error");

            // Act & Assert
            assertThatThrownBy(() -> {
                throw new LocationRefDataException(apiError);
            }).isInstanceOf(LocationRefDataException.class).hasMessageContaining("500 Internal Server Error").hasCause(
                apiError);
        }

        @Test
        void typicalUsage_ValidationError() {
            // Act & Assert
            assertThatThrownBy(() -> {
                throw new LocationRefDataException("Invalid location reference: null");
            }).isInstanceOf(LocationRefDataException.class).hasMessage("Invalid location reference: null").hasNoCause();
        }

        @Test
        void typicalUsage_NestedExceptions() {
            // Arrange
            Exception rootCause = new IllegalStateException("Database connection lost");
            Exception wrappedException = new RuntimeException("Failed to query location data", rootCause);

            // Act & Assert
            assertThatThrownBy(() -> {
                throw new LocationRefDataException(wrappedException);
            }).isInstanceOf(LocationRefDataException.class).hasMessageContaining("Failed to query location data").hasCause(
                wrappedException).rootCause().isInstanceOf(IllegalStateException.class).hasMessage(
                "Database connection lost");
        }
    }
}
