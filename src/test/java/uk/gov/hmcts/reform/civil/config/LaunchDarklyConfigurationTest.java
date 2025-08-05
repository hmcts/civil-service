package uk.gov.hmcts.reform.civil.config;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
            Assertions.assertEquals(offline, client.isOffline());
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
            Assertions.assertEquals(offline, client.isOffline());
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
                Assertions.assertEquals(offline, client.isOffline());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
