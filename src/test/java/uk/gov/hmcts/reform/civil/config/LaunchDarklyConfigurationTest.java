package uk.gov.hmcts.reform.civil.config;

import com.launchdarkly.sdk.server.LDClient;
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
        Boolean offline = false;
        LDClient client = configuration.ldClient(key, offline, null);
        Assertions.assertEquals(client.isOffline(), offline.booleanValue());

        client = configuration.ldClient(key, offline, new String[0]);
        Assertions.assertEquals(client.isOffline(), offline.booleanValue());
    }

    @Test
    public void unexistentFiles() {
        String key = "sdkkey";
        Boolean offline = false;
        LDClient client = configuration.ldClient(key, offline, new String[]{
            "AFileThatDoesNotExist"
        });
        Assertions.assertEquals(client.isOffline(), offline.booleanValue());
    }

    @Test
    public void withFlags() {
        String path = "./bin/utils/launchdarkly-flags.json";
        if (Files.exists(Paths.get(path))) {
            String key = "sdkkey";
            Boolean offline = false;
            LDClient client = configuration.ldClient(key, offline, new String[]{
                "AFileThatDoesNotExist"
            });
            Assertions.assertEquals(client.isOffline(), offline.booleanValue());
        }
    }
}
