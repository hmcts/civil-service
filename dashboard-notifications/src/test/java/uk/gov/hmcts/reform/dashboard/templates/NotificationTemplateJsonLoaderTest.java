package uk.gov.hmcts.reform.dashboard.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import uk.gov.hmcts.reform.dashboard.config.NotificationTemplatesProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTemplateJsonLoaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Test
    void shouldLoadAndSanitiseTemplatesFromJsonResources() {
        NotificationTemplatesProperties properties = new NotificationTemplatesProperties();
        properties.setLocation("classpath:/test-notification-templates/*.json");

        NotificationTemplateJsonLoader loader = new NotificationTemplateJsonLoader(objectMapper, resolver, properties);

        List<NotificationTemplateDefinition> definitions = loader.loadTemplates();

        assertThat(definitions).hasSize(2);

        NotificationTemplateDefinition definition = definitions.stream()
            .filter(d -> "Notice.AAA6.Test.Template".equals(d.getName()))
            .findFirst()
            .orElseThrow();

        assertThat(definition.getTitleEn()).isEqualTo("Title in English");
        assertThat(definition.getTitleCy()).isEqualTo("Teitl Cymraeg");
        assertThat(definition.getRole()).isEqualTo("CLAIMANT");
    }
}
