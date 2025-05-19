package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.testcontainers.shaded.org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@SuppressWarnings("unchecked")
public class YamlNotificationTestUtil {
    public static Map<String, Object> loadNotificationsConfig() {
        try {
            Yaml yaml = new Yaml();
            try (InputStream in = YamlNotificationTestUtil.class.getClassLoader().getResourceAsStream(
                "common-notification-data.yaml")) {
                Map<String, Object> root = yaml.load(in);
                return (Map<String, Object>) root.get("notifications");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML config", e);
        }
    }
}
