package uk.gov.hmcts.reform.civil.notification.handlers.ga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GaEmailDTOGeneratorTest {

    private static final String PARENT_REFERENCE = "1594901956117591";
    private static final String TASK_ID = "NotifyGaSomething";

    private GaNotificationContext context;
    private GaEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        GeneralApplicationCaseData gaCaseData = mock(GeneralApplicationCaseData.class);
        when(gaCaseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(gaCaseData.getGeneralAppParentCaseLink())
            .thenReturn(new GeneralAppParentCaseLink(PARENT_REFERENCE));
        context = new GaNotificationContext(gaCaseData, mock(GeneralApplicationCaseData.class));

        generator = new GaEmailDTOGenerator() {
            @Override
            public Boolean getShouldNotify(GaNotificationContext context) {
                return true;
            }

            @Override
            public Map<String, String> addProperties(GaNotificationContext context) {
                return new HashMap<>();
            }

            @Override
            protected String getEmailAddress(GaNotificationContext context) {
                return "applicant@example.com";
            }

            @Override
            protected String getEmailTemplateId(GaNotificationContext context) {
                return "template-id";
            }

            @Override
            protected Map<String, String> addCustomProperties(Map<String, String> properties,
                                                              GaNotificationContext context) {
                properties.put("custom", "value");
                return properties;
            }
        };
    }

    @Test
    void shouldSendUnderTheParentCaseReference() {
        EmailDTO emailDTO = generator.buildEmailDTO(context, TASK_ID);

        // the GA notification services have always used the parent reference, not a formatted
        // legacy case reference, so migrating a GA BPMN must not change it
        assertThat(emailDTO.getReference()).isEqualTo(PARENT_REFERENCE);
        assertThat(emailDTO.getTargetEmail()).isEqualTo("applicant@example.com");
        assertThat(emailDTO.getEmailTemplate()).isEqualTo("template-id");
        assertThat(emailDTO.getParameters()).containsEntry("custom", "value");
    }

    @Test
    void shouldBuildOneEmailByDefault() {
        assertThat(generator.buildEmailDTOs(context, TASK_ID)).hasSize(1);
    }

    @Test
    void shouldReportTheGaCaseReferenceForLoggingAndErrorTracking() {
        assertThat(context.getCcdCaseReference()).isEqualTo(1234567890123456L);
    }
}
