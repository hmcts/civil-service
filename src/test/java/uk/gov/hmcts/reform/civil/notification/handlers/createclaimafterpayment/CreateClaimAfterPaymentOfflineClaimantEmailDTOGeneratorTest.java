package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class CreateClaimAfterPaymentOfflineClaimantEmailDTOGeneratorTest {

    public static final String CASE_PROCEEDS_IN_CASEMAN_APPLICANT_NOTIFICATION = "case-proceeds-in-caseman-applicant-notification-%s";
    public static final String NON_BILINGUAL_TEMPLATE_ID = "non-bilingual-template-id";
    public static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";
    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CreateClaimAfterPaymentOfflineClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnBilingualTemplateIdWhenClaimantIsBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate())
                .thenReturn(BILINGUAL_TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        Assertions.assertEquals(BILINGUAL_TEMPLATE_ID, templateId);
    }

    @Test
    void shouldReturnNonBilingualTemplateIdWhenClaimantIsNotBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(false);
        when(notificationsProperties.getClaimantLipClaimUpdatedTemplate())
                .thenReturn(NON_BILINGUAL_TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        Assertions.assertEquals(NON_BILINGUAL_TEMPLATE_ID, templateId);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        Assertions.assertEquals(CASE_PROCEEDS_IN_CASEMAN_APPLICANT_NOTIFICATION, referenceTemplate);
    }

    @Test
    void addProperties_returnsOnlyLipProps_whenLipvLROneVOne() {
        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().companyName("Applicant 1").type(Party.Type.COMPANY).build())
                .respondent1(Party.builder().companyName("Respondent 1").type(Party.Type.COMPANY).build())
                .build();

        Map<String, String> properties = new HashMap<>();

        Map<String, String> props = generator.addCustomProperties(properties, caseData);

        assertThat(props).containsAllEntriesOf(Map.of(
                CLAIMANT_V_DEFENDANT, "Applicant 1 V Respondent 1",
                CLAIMANT_NAME, "Applicant 1"
        ));
    }
}
