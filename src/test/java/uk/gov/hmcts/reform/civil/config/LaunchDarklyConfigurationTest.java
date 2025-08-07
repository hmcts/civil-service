package uk.gov.hmcts.reform.civil.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchDarklyConfigurationTest {

    private final LaunchDarklyConfiguration configuration = new LaunchDarklyConfiguration();

    /**
     * LDClient build test without flag files.
     */
    @Test
    void streamingLD() {
        String key = "sdkkey";
        Boolean offline = false;
        LDClient client;
        for (LDClient ldClient : Arrays.asList(
            configuration.ldClient(key, offline, null),
            configuration.ldClient(key, offline, new String[0])
        )) {
            client = ldClient;
            assertEquals(offline, client.isOffline());
        }

    }

    @Test
    void unexistentFiles() {
        String key = "sdkkey";
        Boolean offline = false;
        try (LDClient client = configuration.ldClient(
            key, offline, new String[]{
                "AFileThatDoesNotExist"
            }
        )) {
            assertEquals(offline, client.isOffline());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void withFlags() {
        String path = "./bin/utils/launchdarkly-flags.json";
        if (Files.exists(Paths.get(path))) {
            String key = "sdkkey";
            Boolean offline = false;
            try (LDClient client = configuration.ldClient(
                key, offline, new String[]{
                    "AFileThatDoesNotExist"
                }
            )) {
                assertEquals(offline, client.isOffline());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void shouldCreateOfflineClient() throws IOException {
        String key = "sdkkey";
        Boolean offline = true;
        try (LDClient client = configuration.ldClient(key, offline, null)) {
            assertTrue(client.isOffline());
        }
    }

    @Test
    void shouldHandleEmptyStringInFlagFiles() throws IOException {
        String key = "sdkkey";
        Boolean offline = false;
        try (LDClient client = configuration.ldClient(
            key, offline, new String[]{"", "  ", null}
        )) {
            assertEquals(offline, client.isOffline());
        }
    }

    @Test
    void shouldHandleExistingFiles(@TempDir Path tempDir) throws IOException {
        // Create temporary files
        Path file1 = tempDir.resolve("flags1.json");
        Path file2 = tempDir.resolve("flags2.json");
        Files.writeString(file1, "{}");
        Files.writeString(file2, "{}");

        String key = "sdkkey";
        Boolean offline = true; // Use offline mode to avoid actual LD connection
        try (LDClient client = configuration.ldClient(
            key, offline, new String[]{
                file1.toString(),
                file2.toString()
            }
        )) {
            assertTrue(client.isOffline());
        }
    }

    @Test
    void shouldHandleRelativePathFiles(@TempDir Path tempDir) throws IOException {
        // Create a temp file with relative path
        Path currentDir = Paths.get("").toAbsolutePath();
        Path relativeDir = currentDir.relativize(tempDir);
        Path file = tempDir.resolve("flags.json");
        Files.writeString(file, "{}");

        String relativePath = relativeDir.resolve("flags.json").toString();

        String key = "sdkkey";
        Boolean offline = true;
        try (LDClient client = configuration.ldClient(
            key, offline, new String[]{relativePath}
        )) {
            assertTrue(client.isOffline());
        }
    }

    @Test
    void shouldHandleMixOfExistingAndNonExistingFiles(@TempDir Path tempDir) throws IOException {
        Path existingFile = tempDir.resolve("exists.json");
        Files.writeString(existingFile, "{}");

        String key = "sdkkey";
        Boolean offline = true;
        try (LDClient client = configuration.ldClient(
            key, offline, new String[]{
                existingFile.toString(),
                "nonexistent.json",
                "",
                null
            }
        )) {
            assertTrue(client.isOffline());
        }
    }

    @Test
    void shouldLogDebugWhenFileNotFound() throws IOException {
        // Setup logger to capture debug logs
        Logger logger = (Logger) LoggerFactory.getLogger(LaunchDarklyConfiguration.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);

        try {
            String key = "sdkkey";
            Boolean offline = false;
            try (LDClient ignored = configuration.ldClient(
                key, offline, new String[]{"nonexistent-file.json"}
            )) {
                // Check debug log was generated
                List<ILoggingEvent> logsList = listAppender.list;
                assertThat(logsList).anyMatch(event ->
                    event.getLevel() == Level.DEBUG
                        && event.getFormattedMessage().contains("Could not find files defined by nonexistent-file.json")
                );
            }
        } finally {
            logger.setLevel(originalLevel);
            logger.detachAppender(listAppender);
        }
    }
}
