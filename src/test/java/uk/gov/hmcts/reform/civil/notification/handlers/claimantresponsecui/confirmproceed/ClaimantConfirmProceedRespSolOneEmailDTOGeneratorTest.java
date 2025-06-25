package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;

@ExtendWith(MockitoExtension.class)
class ClaimantConfirmProceedRespSolOneEmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantConfirmProceedRespSolOneEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectTemplateWhenApplicantOneProceedAndDefendantNoCOnline_FastClaim() {
        CaseData caseData = CaseData.builder().respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .responseClaimTrack(FAST_CLAIM.name()).build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction()).thenReturn(expectedTemplateId);
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectTemplateWhenApplicantOneProceedAndIsClaimProceed_SmallClaim() {
        CaseData caseData = CaseData.builder().respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .caseDataLiP(CaseDataLiP.builder().applicant1SettleClaim(YesOrNo.NO).build())
            .defenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .responseClaimTrack(SMALL_CLAIM).build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentSolicitorNotifyToProceedInMediation()).thenReturn(expectedTemplateId);
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectTemplateWhenNoProceed() {
        CaseData caseData = CaseData.builder().respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .responseClaimTrack(FAST_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder().applicant1SettleClaim(YesOrNo.YES).build())
            .defenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec()).thenReturn(expectedTemplateId);
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectTemplateWhenMediationRequired() {
        CaseData caseData = CaseData.builder().respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .responseClaimTrack(SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLRForMediation()).thenReturn(expectedTemplateId);
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(false);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE);
    }
}
