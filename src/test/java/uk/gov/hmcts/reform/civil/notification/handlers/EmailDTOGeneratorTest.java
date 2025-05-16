package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailDTOGeneratorTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TEMPLATE_ID = "template-id";
    private static final String TEST_REFERENCE_TEMPLATE = "reference-%s";
    private static final String LEGACY_CASE_REFERENCE = "12345";
    protected static final String CUSTOM_KEY = "customKey";
    protected static final String CUSTOM_VALUE = "customValue";

    private EmailDTOGenerator emailDTOGenerator;
    String taskId = "someTaskId";

    @Mock
    private CaseData caseData;

    @Mock
    private TemplateCommonPropertiesHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a concrete implementation of the abstract class for testing
        emailDTOGenerator = new EmailDTOGenerator() {

            @Override
            protected Boolean getShouldNotify(CaseData caseData) {
                return Boolean.TRUE;
            }

            @Override
            protected String getEmailAddress(CaseData caseData) {
                return TEST_EMAIL;
            }

            @Override
            protected String getEmailTemplateId(CaseData caseData) {
                return TEST_TEMPLATE_ID;
            }

            @Override
            protected String getReferenceTemplate() {
                return TEST_REFERENCE_TEMPLATE;
            }

            @Override
            protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
                properties.put(CUSTOM_KEY, CUSTOM_VALUE);
                return properties;
            }
        };

        // Inject the mocked helper
        emailDTOGenerator.templateCommonPropertiesHelper = helper;
    }

    @Test
    void shouldBuildEmailDTOCorrectly() {
        when(caseData.getLegacyCaseReference()).thenReturn(LEGACY_CASE_REFERENCE);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);

        EmailDTO emailDTO = emailDTOGenerator.buildEmailDTO(caseData, taskId);

        assertThat(emailDTO.getTargetEmail()).isEqualTo(TEST_EMAIL);
        assertThat(emailDTO.getEmailTemplate()).isEqualTo(TEST_TEMPLATE_ID);
        assertThat(emailDTO.getReference()).isEqualTo(String.format(TEST_REFERENCE_TEMPLATE, LEGACY_CASE_REFERENCE));

        assertThat(emailDTO.getParameters())
            .containsEntry(CUSTOM_KEY, CUSTOM_VALUE);

        verify(helper).addBaseProperties(eq(caseData), any());
        verify(helper).addCommonFooterSignature(any());
        verify(helper).addCnbcContact(eq(caseData), any());
        verify(helper).addSpecAndUnspecContact(eq(caseData), any());
    }
}
