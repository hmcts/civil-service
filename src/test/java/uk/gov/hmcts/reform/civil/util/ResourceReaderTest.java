package uk.gov.hmcts.reform.civil.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.utils.ResourceReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceReaderTest {

    @Test
    void shouldReturnStringIfResourceExists() {
        String content = ResourceReader.readString("sample-resource.txt");
        assertThat(content).contains("Sample content");
    }

    @Test
    void shouldThrowExceptionWhileReadingStringIfResourceDoesNotExist() {
        assertThatThrownBy(() -> ResourceReader.readString("non-existing-resource.txt"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnBytesIfResourceExists() {
        byte[] content = ResourceReader.readBytes("sample-resource.txt");
        assertThat(content).contains("Sample content".getBytes());
    }

    @Test
    void shouldThrowExceptionWhileReadingBytesIfResourceDoesNotExist() {
        assertThatThrownBy(() -> ResourceReader.readBytes("non-existing-resource.txt"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
