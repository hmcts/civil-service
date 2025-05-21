package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaisingClaimAgainstLitigantInPersonForSpecAppSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "claimant-solicitor-template-id";
    private static final String REFERENCE_TEMPLATE = "applicant-create-case-handed-offline-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private RaisingClaimAgainstLitigantInPersonForSpecAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getClaimantSolicitorSpecCaseWillProgressOffline()).thenReturn(TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE);
    }
}