package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.ENGLISH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.WELSH;

@ExtendWith(MockitoExtension.class)
class ClaimantConfirmProceedDefendantEmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

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
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE);
    }

}
