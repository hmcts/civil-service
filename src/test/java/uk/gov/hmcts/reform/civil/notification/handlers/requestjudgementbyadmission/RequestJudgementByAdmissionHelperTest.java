package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
class RequestJudgementByAdmissionHelperTest {

    @Mock
    private PinInPostConfiguration pipInPostConfiguration;

    @InjectMocks
    private RequestJudgementByAdmissionHelper helper;

    @Test
    void shouldAddLipProperties() {
        when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");

        Party applicant = new Party()
            .setType(Party.Type.COMPANY)
            .setCompanyName("Applicant Company");
        Party respondent = new Party()
            .setType(Party.Type.COMPANY)
            .setCompanyName("Respondent Company");

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .respondent1(respondent)
            .legacyCaseReference("000DC001")
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = helper.addLipProperties(properties, caseData);

        assertThat(updatedProperties)
            .containsEntry(CLAIMANT_NAME, "Applicant Company")
            .containsEntry(RESPONDENT_NAME, "Respondent Company")
            .containsEntry(CLAIM_REFERENCE_NUMBER, "000DC001")
            .containsEntry(FRONTEND_URL, "dummy_cui_front_end_url");
    }
}
