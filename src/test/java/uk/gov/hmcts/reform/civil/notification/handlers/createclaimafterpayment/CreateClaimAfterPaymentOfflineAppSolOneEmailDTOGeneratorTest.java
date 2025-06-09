package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGeneratorTest {

    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final String CASE_PROCEEDS_IN_CASEMAN_APPLICANT_NOTIFICATION = "case-proceeds-in-caseman-applicant-notification-%s";
    public static final String EXPECTED_TEMPLATE_ID = "expected-template-id";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn(EXPECTED_TEMPLATE_ID);

        String actualTemplateId = generator.getEmailTemplateId(mock(CaseData.class));

        assertThat(actualTemplateId).isEqualTo(EXPECTED_TEMPLATE_ID);
        verify(notificationsProperties).getSolicitorCaseTakenOffline();
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String refTpl = generator.getReferenceTemplate();

        assertThat(refTpl)
                .isEqualTo(CASE_PROCEEDS_IN_CASEMAN_APPLICANT_NOTIFICATION);
    }
}
