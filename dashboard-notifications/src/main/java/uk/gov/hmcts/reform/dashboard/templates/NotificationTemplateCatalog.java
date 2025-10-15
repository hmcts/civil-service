package uk.gov.hmcts.reform.dashboard.templates;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class NotificationTemplateCatalog {

    private final NotificationTemplateJsonLoader loader;

    private volatile Map<String, NotificationTemplateDefinition> templates = Map.of();

    public NotificationTemplateCatalog(NotificationTemplateJsonLoader loader) {
        this.loader = loader;
        reload();
    }

    public Optional<NotificationTemplateDefinition> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(templates.get(name));
    }

    public Collection<NotificationTemplateDefinition> findAll() {
        return templates.values();
    }

    public synchronized void reload() {
        Map<String, NotificationTemplateDefinition> loaded = new LinkedHashMap<>();
        loader.loadTemplates().stream()
            .filter(definition -> Objects.nonNull(definition.getName()))
            .forEach(definition -> loaded.put(definition.getName(), definition));
        this.templates = Map.copyOf(loaded);
    }
}
