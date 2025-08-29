package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
class CaseTakenOfflineRespLipSolOneEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String CASE_TAKEN_OFFLINE_APPLICANT_NOTIFICATION = "case-taken-offline-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseTakenOfflineRespLipSolOneEmailDTOGenerator generator;

    @Test
    void shouldUseEnglishTemplate() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.YES)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage("ENGLISH").build()).build())
            .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
            .build();

        when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldUseWelshTemplate() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.YES)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage("WELSH").build()).build())

            .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
            .build();

        when(notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()).thenReturn(TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo(CASE_TAKEN_OFFLINE_APPLICANT_NOTIFICATION);
    }

    @Test
    void shouldAddCustomProperties() {
        String legacyCaseReference = "12345";
        String defendantName = "Defendant Name";
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().individualFirstName("Claimant").individualLastName("Name").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Defendant").individualLastName("Name").type(Party.Type.INDIVIDUAL).build())
            .legacyCaseReference(legacyCaseReference)
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = generator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties.size()).isEqualTo(2);
        assertThat(updatedProperties).containsEntry(CLAIM_REFERENCE_NUMBER, legacyCaseReference);
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, defendantName);
    }
}
