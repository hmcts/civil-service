package uk.gov.hmcts.reform.dashboard.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import uk.gov.hmcts.reform.dashboard.config.NotificationTemplatesProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void shouldReturnEmptyWhenTemplateNameIsNull() {
        assertThat(catalog.findByName(null)).isEmpty();
    }

    @Test
    void shouldReloadTemplatesAndProtectInternalState() {
        NotificationTemplateDefinition first = NotificationTemplateDefinition.builder()
            .name("Template.One")
            .titleEn("First")
            .build();
        NotificationTemplateDefinition second = NotificationTemplateDefinition.builder()
            .name("Template.Two")
            .titleEn("Second")
            .build();
        NotificationTemplateDefinition replacement = NotificationTemplateDefinition.builder()
            .name("Template.One")
            .titleEn("Replacement")
            .build();

        NotificationTemplateJsonLoader loader = mock(NotificationTemplateJsonLoader.class);
        when(loader.loadTemplates()).thenReturn(List.of(first));

        NotificationTemplateCatalog reloadedCatalog = new NotificationTemplateCatalog(loader);
        assertThat(reloadedCatalog.findByName("Template.One")).contains(first);

        when(loader.loadTemplates()).thenReturn(List.of(first, replacement, second));
        reloadedCatalog.reload();

        assertThat(reloadedCatalog.findByName("Template.Two")).contains(second);
        assertThat(reloadedCatalog.findByName("Template.One"))
            .isPresent()
            .contains(replacement);
        assertThatThrownBy(() -> reloadedCatalog.findAll().add(first))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
