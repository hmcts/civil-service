package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class ClaimantResponseConfirmsToProceedEmailHelperTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantResponseConfirmsToProceedEmailHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenCaseIsUnspecAndNotMultiClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(SMALL_CLAIM)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsToProceed()).thenReturn(expectedTemplateId);

        String actualTemplateId = helper.getTemplate(caseData, false);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenCaseIsUnspecAndMultiClaimAndMultiOrIntermediateTrackEnabled() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(MULTI_CLAIM)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsToProceed()).thenReturn(expectedTemplateId);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        String actualTemplateId = helper.getTemplate(caseData, false);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenCaseIsUnspecAndMultiClaimAndNotMultiOrIntermediateTrackEnabled() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(MULTI_CLAIM)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn(expectedTemplateId);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        String actualTemplateId = helper.getTemplate(caseData, false);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateForApplicantWhenCaseIsSpecAndProceedsWithAction() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .allocatedTrack(SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .responseClaimMediationSpecRequired(NO)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsToProceedSpecWithAction()).thenReturn(expectedTemplateId);

        String actualTemplateId = helper.getTemplate(caseData, true);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateForApplicantWhenCaseIsSpecAndNotProceedsWithAction() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .allocatedTrack(SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsToProceedSpec()).thenReturn(expectedTemplateId);

        String actualTemplateId = helper.getTemplate(caseData, true);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateForRespondentWhenCaseIsSpecAndProceedsWithAction() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .allocatedTrack(SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .responseClaimMediationSpecRequired(NO)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction()).thenReturn(expectedTemplateId);

        String actualTemplateId = helper.getTemplate(caseData, false);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateForRespondentWhenCaseIsSpecAndNotProceedsWithAction() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .allocatedTrack(SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpec()).thenReturn(expectedTemplateId);

        String actualTemplateId = helper.getTemplate(caseData, false);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }
}
