package uk.gov.hmcts.reform.civil.config;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

public class LaunchDarklyConfigurationTest {

    private LaunchDarklyConfiguration configuration = new LaunchDarklyConfiguration();

    /**
     * Without flag files the client streams from LaunchDarkly, and the fake key can never connect, so
     * these don't wait for initialisation: the default startWait is 5s per client, all spent on network.
     */
    private LDClient streamingClient(String key, boolean offline, String[] flagFiles) {
        return new LDClient(key, configuration.configBuilder(offline, flagFiles).startWait(Duration.ZERO).build());
    }

    /**
     * LDClient build test without flag files.
     */
    @Test
    public void streamingLD() throws Exception {
        String key = "sdkkey";
        boolean offline = false;
        try (LDClient client = streamingClient(key, offline, null)) {
            Assertions.assertEquals(client.isOffline(), offline);
        }

        try (LDClient client = streamingClient(key, offline, new String[0])) {
            Assertions.assertEquals(client.isOffline(), offline);
        }
    }

    @Test
    public void unexistentFiles() throws Exception {
        String key = "sdkkey";
        boolean offline = false;
        try (LDClient client = streamingClient(key, offline, new String[]{
            "AFileThatDoesNotExist"
        })) {
            Assertions.assertEquals(client.isOffline(), offline);
        }
    }

    @Test
    public void withFlags() throws Exception {
        String path = "./bin/utils/launchdarkly-flags.json";
        if (Files.exists(Paths.get(path))) {
            String key = "sdkkey";
            boolean offline = false;
            try (LDClient client = configuration.ldClient(key, offline, new String[]{path})) {
                Assertions.assertEquals(client.isOffline(), offline);
                Assertions.assertFalse(client.boolVariation(
                    "general_applications_enabled",
                    LDContext.create("civil-service"),
                    true
                ));
            }
        }
    }

    @Test
    public void returnsSuppliedDefaultsWhenOffline() throws Exception {
        try (LDClient client = configuration.ldClient("sdkkey", true, null)) {
            LDContext context = LDContext.create("civil-service");

            Assertions.assertTrue(client.boolVariation("unknown-flag", context, true));
            Assertions.assertFalse(client.boolVariation("unknown-flag", context, false));
        }
    }
}
