package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.ENGLISH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed.ClaimantConfirmProceedDefendantEmailDTOGenerator.NO_EMAIL_OPERATION;

@ExtendWith(MockitoExtension.class)
class ClaimantConfirmProceedDefendantEmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantConfirmProceedDefendantEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantGetTemplateIsInvoked() {
        CaseData caseData = CaseData.builder().caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(
            RespondentLiPResponse.builder().respondent1ResponseLanguage(WELSH.toString()).build()).build()).build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantGetTemplateIsInvokedAndBilingual() {
        CaseData caseData = CaseData.builder().caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(
            RespondentLiPResponse.builder().respondent1ResponseLanguage(ENGLISH.toString()).build()).build()).build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnNoEmailOperation_whenDefendantGetTemplateNocDisabledClaimantBilingual() {
        CaseData caseData = CaseData.builder().caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(
            RespondentLiPResponse.builder().respondent1ResponseLanguage(ENGLISH.toString()).build()).build())
            .claimantBilingualLanguagePreference(WELSH.toString()).build();
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(false);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(NO_EMAIL_OPERATION);
    }

    @Test
    void shouldReturnNoEmailOperation_whenDefendantGetTemplateIsInvokedAndClaimantBilingualAndProceedNoPartAdmit() {
        CaseData caseData = CaseData.builder().caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(
                RespondentLiPResponse.builder().respondent1ResponseLanguage(ENGLISH.toString()).build()).build())
            .claimantBilingualLanguagePreference(WELSH.toString())
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .build();
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(NO_EMAIL_OPERATION);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomProperties() {
        String legacyCaseReference = "000MC001";

        Party respondent = Party.builder()
            .individualTitle("Ms")
            .individualFirstName("Jane")
            .individualLastName("Smith")
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .legacyCaseReference(legacyCaseReference)
            .respondent1(respondent)
            .build();

        Map<String, String> properties = new HashMap<>();

        Map<String, String> result = emailDTOGenerator.addCustomProperties(properties, caseData);

        assertThat(result)
            .containsEntry(RESPONDENT_NAME, "Ms Jane Smith")
            .containsEntry(CLAIM_REFERENCE_NUMBER, legacyCaseReference);
    }
}
