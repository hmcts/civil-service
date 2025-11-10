package uk.gov.hmcts.reform.dashboard.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import uk.gov.hmcts.reform.dashboard.config.NotificationTemplatesProperties;
import uk.gov.hmcts.reform.dashboard.templates.NotificationTemplatesLoadingException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void shouldDeduplicateTemplatesAndSkipEntriesWithoutNames() throws IOException {
        ObjectMapper mockedMapper = mock(ObjectMapper.class);
        Resource resourceA = mock(Resource.class);
        Resource resourceB = mock(Resource.class);
        Resource resourceInvalid = mock(Resource.class);
        PathMatchingResourcePatternResolver mockedResolver = mock(PathMatchingResourcePatternResolver.class);

        when(resourceA.getFilename()).thenReturn("b.json");
        when(resourceB.getFilename()).thenReturn("a.json");
        when(resourceInvalid.getFilename()).thenReturn("c.json");
        when(resourceA.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(resourceB.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(resourceInvalid.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        when(mockedResolver.getResources("pattern")).thenReturn(new Resource[]{resourceA, resourceInvalid, resourceB});

        NotificationTemplateDefinition definitionOne = NotificationTemplateDefinition.builder()
            .name("Template")
            .titleEn("First")
            .build();
        NotificationTemplateDefinition definitionDuplicate = NotificationTemplateDefinition.builder()
            .name("Template")
            .titleEn("Second")
            .build();
        NotificationTemplateDefinition definitionWithoutName = NotificationTemplateDefinition.builder()
            .name(null)
            .build();

        when(mockedMapper.readValue(any(InputStream.class), eq(NotificationTemplateDefinition.class)))
            .thenReturn(definitionOne, definitionWithoutName, definitionDuplicate);

        NotificationTemplatesProperties properties = new NotificationTemplatesProperties();
        properties.setLocation("pattern");

        NotificationTemplateJsonLoader loader = new NotificationTemplateJsonLoader(mockedMapper, mockedResolver, properties);

        List<NotificationTemplateDefinition> definitions = loader.loadTemplates();

        assertThat(definitions).hasSize(1);
        assertThat(definitions.get(0).getTitleEn()).isEqualTo("Second");
    }

    @Test
    void shouldReturnEmptyListWhenNoTemplatesFound() throws IOException {
        ObjectMapper mockedMapper = mock(ObjectMapper.class);
        PathMatchingResourcePatternResolver mockedResolver = mock(PathMatchingResourcePatternResolver.class);
        when(mockedResolver.getResources("pattern")).thenReturn(new Resource[0]);

        NotificationTemplatesProperties properties = new NotificationTemplatesProperties();
        properties.setLocation("pattern");

        NotificationTemplateJsonLoader loader = new NotificationTemplateJsonLoader(mockedMapper, mockedResolver, properties);

        assertThat(loader.loadTemplates()).isEmpty();
    }

    @Test
    void shouldWrapIoExceptionsWhenLoadingTemplates() throws IOException {
        PathMatchingResourcePatternResolver mockedResolver = mock(PathMatchingResourcePatternResolver.class);
        Resource resource = mock(Resource.class);
        when(mockedResolver.getResources("pattern")).thenReturn(new Resource[]{resource});
        when(resource.getDescription()).thenReturn("test.json");
        when(resource.getInputStream()).thenThrow(new IOException("boom"));

        NotificationTemplatesProperties properties = new NotificationTemplatesProperties();
        properties.setLocation("pattern");

        ObjectMapper mockedMapper = mock(ObjectMapper.class);
        NotificationTemplateJsonLoader loader = new NotificationTemplateJsonLoader(mockedMapper, mockedResolver, properties);

        assertThatThrownBy(loader::loadTemplates)
            .isInstanceOf(NotificationTemplatesLoadingException.class)
            .hasMessageContaining("Failed to parse notification template definition");
    }

    @Test
    void shouldSkipNullDefinitionsWhenLoadingTemplates() throws IOException {
        ObjectMapper mockedMapper = mock(ObjectMapper.class);
        PathMatchingResourcePatternResolver mockedResolver = mock(PathMatchingResourcePatternResolver.class);
        Resource resource = mock(Resource.class);

        when(mockedResolver.getResources("pattern")).thenReturn(new Resource[]{resource});
        when(resource.getFilename()).thenReturn("template.json");
        when(mockedMapper.readValue(any(InputStream.class), eq(NotificationTemplateDefinition.class)))
            .thenReturn(null);

        NotificationTemplatesProperties properties = new NotificationTemplatesProperties();
        properties.setLocation("pattern");

        NotificationTemplateJsonLoader loader = new NotificationTemplateJsonLoader(mockedMapper, mockedResolver, properties);

        assertThat(loader.loadTemplates()).isEmpty();
    }

    @Test
    void shouldWrapIOExceptionWhenResolvingResources() throws IOException {
        ObjectMapper mockedMapper = mock(ObjectMapper.class);
        PathMatchingResourcePatternResolver mockedResolver = mock(PathMatchingResourcePatternResolver.class);
        when(mockedResolver.getResources("pattern")).thenThrow(new IOException("resolver failure"));

        NotificationTemplatesProperties properties = new NotificationTemplatesProperties();
        properties.setLocation("pattern");

        NotificationTemplateJsonLoader loader = new NotificationTemplateJsonLoader(mockedMapper, mockedResolver, properties);

        assertThatThrownBy(loader::loadTemplates)
            .isInstanceOf(NotificationTemplatesLoadingException.class)
            .hasMessageContaining("Unable to read notification template definitions");
    }
}
