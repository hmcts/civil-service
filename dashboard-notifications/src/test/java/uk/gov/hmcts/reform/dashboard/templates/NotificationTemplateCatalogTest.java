package uk.gov.hmcts.reform.dashboard.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import uk.gov.hmcts.reform.dashboard.config.NotificationTemplatesProperties;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTemplateCatalogTest {

    private NotificationTemplateCatalog catalog;

    @BeforeEach
    void setup() {
        NotificationTemplatesProperties properties = new NotificationTemplatesProperties();
        properties.setLocation("classpath:/test-notification-templates/*.json");
        NotificationTemplateJsonLoader loader = new NotificationTemplateJsonLoader(
            new ObjectMapper(),
            new PathMatchingResourcePatternResolver(),
            properties
        );
        catalog = new NotificationTemplateCatalog(loader);
    }

    @Test
    void shouldReturnTemplateByName() {
        assertThat(catalog.findByName("Notice.AAA6.Test.Template")).isPresent();
    }

    @Test
    void shouldExposeAllTemplates() {
        assertThat(catalog.findAll()).hasSize(2);
    }
}
