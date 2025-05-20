package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.rejectrepayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseCuiRejectPayRespLipEmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "claimant-reject-repayment-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantResponseCuiRejectPayRespLipEmailDTOGenerator emailGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantGetTemplateIsInvoked() {
        CaseData caseData = CaseData.builder().applicant1(Party.builder().build()).applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.BOTH.getDisplayedValue()).build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLipWelshTemplate()).thenReturn(expectedTemplateId);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantGetTemplateIsInvokedAndBilingual() {
        CaseData caseData = CaseData.builder().applicant1(Party.builder().build()).applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.ENGLISH.getDisplayedValue()).build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLipTemplate()).thenReturn(expectedTemplateId);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE);
    }

}
