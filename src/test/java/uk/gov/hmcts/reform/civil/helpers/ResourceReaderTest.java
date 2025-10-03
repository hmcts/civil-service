package uk.gov.hmcts.reform.civil.helpers;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class ResourceReaderTest {

    @Test
    void constructor_ShouldBePrivate() throws NoSuchMethodException {
        Constructor<ResourceReader> constructor = ResourceReader.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    void constructor_ShouldBeInstantiable() throws Exception {
        // Arrange
        Constructor<ResourceReader> constructor = ResourceReader.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        ResourceReader instance = constructor.newInstance();

        assertThat(instance).isNotNull();
    }

    @Test
    void shouldThrowIllegalStateException_whenResourcePathIsNull() {
        Exception exception = assertThrows(
            IllegalStateException.class,
            () -> ResourceReader.readString(null)
        );
        String expectedMessage = "Unable to read resource: null";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldThrowIllegalStateException_whenResourcePathIsInvalid() {
        Exception exception = assertThrows(
            IllegalStateException.class,
            () -> ResourceReader.readString("abc")
        );
        String expectedMessage = "Unable to read resource: abc";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shouldReadStringFromResource() {
        String content = ResourceReader.readString("/sample-resource.txt");
        assertThat(content.strip()).isEqualTo("Sample content");
    }

    @Test
    void shouldReadBytesFromResource() {
        byte[] content = ResourceReader.readBytes("/sample-resource.txt");
        assertThat(new String(content).strip()).isEqualTo("Sample content");
    }

    @Test
    void shouldThrowIllegalStateException_whenResourcePathIsNullForBytes() {
        assertThatThrownBy(() -> ResourceReader.readBytes(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to read resource: null");
    }

    @Test
    void shouldThrowIllegalStateException_whenResourcePathIsInvalidForBytes() {
        assertThatThrownBy(() -> ResourceReader.readBytes("/non-existent-resource.txt"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Unable to read resource: /non-existent-resource.txt");
    }

    @Test
    void shouldThrowIllegalStateException_whenIOExceptionOccurs() {
        try (MockedStatic<IOUtils> mockedIOUtils = mockStatic(IOUtils.class)) {
            mockedIOUtils.when(() -> IOUtils.toByteArray(any(InputStream.class)))
                .thenThrow(new IOException("Simulated IO exception"));

            assertThatThrownBy(() -> ResourceReader.readBytes("/sample-resource.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    void shouldThrowIllegalStateException_whenNullPointerExceptionOccurs() {
        try (MockedStatic<IOUtils> mockedIOUtils = mockStatic(IOUtils.class)) {
            mockedIOUtils.when(() -> IOUtils.toByteArray(any(InputStream.class)))
                .thenThrow(new NullPointerException("Simulated null pointer exception"));

            assertThatThrownBy(() -> ResourceReader.readBytes("/sample-resource.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to read resource: /sample-resource.txt")
                .hasCauseInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void shouldHandleIOException() {
        String content = ResourceReader.readString("/sample-resource.txt");
        assertThat(content).isNotNull();
        assertThat(content.strip()).isEqualTo("Sample content");
    }
}
