package uk.gov.hmcts.reform.civil.config;

import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class LaunchDarklyConfigurationTest {

    private LaunchDarklyConfiguration configuration = new LaunchDarklyConfiguration();

    /**
     * LDClient build test without flag files.
     */
    @Test
    public void streamingLD() {
        String key = "sdkkey";
        boolean offline = true;
        LDClientInterface client = configuration.ldClient(key, offline, null);
        Assertions.assertEquals(offline, client.isOffline());

        client = configuration.ldClient(key, offline, new String[0]);
        Assertions.assertEquals(offline, client.isOffline());
    }

    @Test
    public void unexistentFiles() {
        String key = "sdkkey";
        boolean offline = true;
        LDClientInterface client = configuration.ldClient(key, offline, new String[]{
            "AFileThatDoesNotExist"
        });
        Assertions.assertEquals(offline, client.isOffline());
    }

    @Test
    public void withFlags() {
        String path = "./bin/utils/launchdarkly-flags.json";
        if (Files.exists(Paths.get(path))) {
            String key = "sdkkey";
            boolean offline = true;
            LDClientInterface client = configuration.ldClient(key, offline, new String[]{
                "AFileThatDoesNotExist"
            });
            Assertions.assertEquals(offline, client.isOffline());
        }
    }
}
