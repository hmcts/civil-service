package uk.gov.hmcts.reform.dashboard.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.dashboard.config.NotificationTemplatesProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NotificationTemplateJsonLoader {

    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourcePatternResolver;
    private final NotificationTemplatesProperties properties;

    public NotificationTemplateJsonLoader(ObjectMapper objectMapper,
                                          ResourcePatternResolver resourcePatternResolver,
                                          NotificationTemplatesProperties properties) {
        this.objectMapper = objectMapper;
        this.resourcePatternResolver = resourcePatternResolver;
        this.properties = properties;
    }

    public List<NotificationTemplateDefinition> loadTemplates() {
        String location = properties.getLocation();
        try {
            Resource[] resources = resourcePatternResolver.getResources(location);
            if (resources.length == 0) {
                log.info("No notification template definitions found for pattern {}", location);
                return List.of();
            }

            Map<String, NotificationTemplateDefinition> definitions = new LinkedHashMap<>();
            List<Resource> sorted = new ArrayList<>(Arrays.asList(resources));
            sorted.sort(Comparator.comparing(Resource::getFilename, Comparator.nullsLast(String::compareToIgnoreCase)));

            for (Resource resource : sorted) {
                NotificationTemplateDefinition definition = readDefinition(resource);
                if (definition == null || definition.getName() == null) {
                    log.warn("Skipping notification template file {} because the name is missing", resource.getFilename());
                    continue;
                }
                NotificationTemplateDefinition sanitised = definition.sanitise();
                if (definitions.put(sanitised.getName(), sanitised) != null) {
                    log.warn("Duplicate notification template name {} encountered; later definition will be used", sanitised.getName());
                }
            }
            return List.copyOf(definitions.values());
        } catch (IOException ex) {
            throw new NotificationTemplatesLoadingException(
                "Unable to read notification template definitions from pattern " + location, ex
            );
        }
    }

    private NotificationTemplateDefinition readDefinition(Resource resource) {
        try {
            return objectMapper.readValue(resource.getInputStream(), NotificationTemplateDefinition.class);
        } catch (IOException ex) {
            throw new NotificationTemplatesLoadingException(
                "Failed to parse notification template definition from " + resource.getDescription(), ex
            );
        }
    }
}
