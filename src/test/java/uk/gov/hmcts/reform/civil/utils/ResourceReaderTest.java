package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceReaderTest {

    @Test
    void constructor_ShouldBePrivate() throws NoSuchMethodException {
        // Act
        Constructor<ResourceReader> constructor = ResourceReader.class.getDeclaredConstructor();

        // Assert
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    void shouldReturnStringIfResourceExists() {
        String content = ResourceReader.readString("sample-resource.txt");
        assertThat(content).contains("Sample content");
    }

    @Test
    void shouldThrowExceptionWhileReadingStringIfResourceDoesNotExist() {
        assertThatThrownBy(() -> ResourceReader.readString("non-existing-resource.txt"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Resource does not exist");
    }

    @Test
    void shouldReturnBytesIfResourceExists() {
        byte[] content = ResourceReader.readBytes("sample-resource.txt");
        assertThat(content).contains("Sample content".getBytes());
    }

    @Test
    void shouldThrowExceptionWhileReadingBytesIfResourceDoesNotExist() {
        assertThatThrownBy(() -> ResourceReader.readBytes("non-existing-resource.txt"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Resource does not exist");
    }

    @Test
    void shouldThrowExceptionWhileReadingBytesIfResourceIsNull() {
        assertThatThrownBy(() -> ResourceReader.readBytes(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhileReadingStringIfResourceIsNull() {
        assertThatThrownBy(() -> ResourceReader.readString(null))
            .isInstanceOf(NullPointerException.class);
    }
}
