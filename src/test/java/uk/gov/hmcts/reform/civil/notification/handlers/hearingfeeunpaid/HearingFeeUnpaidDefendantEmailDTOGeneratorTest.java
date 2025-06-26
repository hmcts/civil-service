package uk.gov.hmcts.reform.civil.notification.handlers.hearingfeeunpaid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingFeeUnpaidDefendantEmailDTOGeneratorTest {

    public static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";
    public static final String TEMPLATE_ID = "default-template-id";
    public static final String HEARING_FEE_UNPAID_DEFENDANT_LIP_NOTIFICATION = "hearing-fee-unpaid-defendantLip-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private HearingFeeUnpaidDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnBilingualTemplateId_whenRespondentResponseIsBilingual() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build()
                .toBuilder().caseDataLiP(CaseDataLiP.builder()
                        .respondent1LiPResponse(RespondentLiPResponse.builder()
                                .respondent1ResponseLanguage(Language.BOTH.toString()).build()).build())
                .build();

        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(BILINGUAL_TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(BILINGUAL_TEMPLATE_ID);
    }

    @Test
    void shouldReturnDefaultTemplateId_whenRespondentResponseIsNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build()
                .toBuilder().build();

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(HEARING_FEE_UNPAID_DEFENDANT_LIP_NOTIFICATION);
    }
}