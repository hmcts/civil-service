package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationHelperTest {

    public static final String BASE_LOCATION = "Base Location";

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private SimpleStateFlowEngine simpleStateFlowEngine;

    @Mock
    private SdoCaseClassificationService sdoCaseClassificationService;

    @InjectMocks
    private DashboardNotificationHelper dashboardDecisionHelper;

    @ParameterizedTest
    @CsvSource({
        "true, true, true, CREATE_CLAIM_SPEC_AFTER_PAYMENT, CREATE_CLAIM_SPEC_AFTER_PAYMENT, true",
        "true, true, true, DEFENDANT_RESPONSE_SPEC, DEFENDANT_RESPONSE_SPEC, true",
        "false, true, true, DEFENDANT_RESPONSE_SPEC, DEFENDANT_RESPONSE_SPEC, false",
        "true, false, true, DEFENDANT_RESPONSE_SPEC, DEFENDANT_RESPONSE_SPEC, false",
        "true, true, false, DEFENDANT_RESPONSE_SPEC, DEFENDANT_RESPONSE_SPEC, false",
        "true, true, true, CLAIMANT_RESPONSE, DEFENDANT_RESPONSE_SPEC, false"
    })
    void shouldIdentifyCivilSubmissionAwaitingTranslation(boolean lip, boolean bilingual, boolean welshEnabled,
                                                          CaseEvent processEvent, CaseEvent expectedEvent,
                                                          boolean expected) {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getBusinessProcess()).thenReturn(BusinessProcess.ready(processEvent));
        if (processEvent == expectedEvent) {
            when(caseData.isApplicantLiP()).thenReturn(lip);
            if (lip) {
                when(caseData.isClaimantBilingual()).thenReturn(bilingual);
                if (bilingual) {
                    when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(welshEnabled);
                }
            }
        }

        assertThat(dashboardDecisionHelper.isAwaitingWelshTranslation(caseData, expectedEvent)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT, true, true",
        "INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT, false, false",
        "UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_DOC, true, false"
    })
    void shouldIdentifyGaSubmissionAwaitingTranslation(CaseEvent processEvent, boolean bilingual, boolean expected) {
        GeneralApplicationCaseData caseData = mock(GeneralApplicationCaseData.class);
        when(caseData.getBusinessProcess()).thenReturn(BusinessProcess.ready(processEvent));
        if (processEvent == CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT) {
            when(caseData.isApplicationBilingual()).thenReturn(bilingual);
        }

        assertThat(dashboardDecisionHelper.isAwaitingWelshTranslation(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldNotDeferNotificationsWithoutBusinessProcess() {
        assertFalse(dashboardDecisionHelper.isAwaitingWelshTranslation(CaseData.builder().build(), CaseEvent.DEFENDANT_RESPONSE_SPEC));
        assertFalse(dashboardDecisionHelper.isAwaitingWelshTranslation(new GeneralApplicationCaseData()));
    }

    @Nested
    class UploadDocumentsTest {

        @Test
        void shouldReturnTrue_whenHasAtLeastOneUploadDocuments() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setRes1MediationDocumentsReferred(List.of(new Element<>()));
            caseData.setRes1MediationNonAttendanceDocs(null);
            caseData.setApp1MediationDocumentsReferred(null);
            caseData.setApp1MediationNonAttendanceDocs(null);

            assertTrue(dashboardDecisionHelper.hasUploadDocuments(caseData));

            caseData.setRes1MediationDocumentsReferred(null);
            caseData.setRes1MediationNonAttendanceDocs(List.of(new Element<>()));
            caseData.setApp1MediationDocumentsReferred(null);
            caseData.setApp1MediationNonAttendanceDocs(null);

            assertTrue(dashboardDecisionHelper.hasUploadDocuments(caseData));

            caseData.setRes1MediationDocumentsReferred(null);
            caseData.setRes1MediationNonAttendanceDocs(null);
            caseData.setApp1MediationDocumentsReferred(List.of(new Element<>()));
            caseData.setApp1MediationNonAttendanceDocs(null);

            assertTrue(dashboardDecisionHelper.hasUploadDocuments(caseData));

            caseData.setRes1MediationDocumentsReferred(null);
            caseData.setRes1MediationNonAttendanceDocs(null);
            caseData.setApp1MediationDocumentsReferred(null);
            caseData.setApp1MediationNonAttendanceDocs(List.of(new Element<>()));

            assertTrue(dashboardDecisionHelper.hasUploadDocuments(caseData));
        }

        @Test
        void shouldReturnFalse_whenUploadDocumentsIsMissing() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setRes1MediationDocumentsReferred(null);
            caseData.setRes1MediationNonAttendanceDocs(null);
            caseData.setApp1MediationDocumentsReferred(null);
            caseData.setApp1MediationNonAttendanceDocs(null);

            assertFalse(dashboardDecisionHelper.hasUploadDocuments(caseData));
        }
    }

    @Nested
    class IsMediationUnsuccessfulReasonEqualToNotContactableClaimantOneTests {

        @Test
        void shouldReturnTrue_whenReasonMatch() {
            CaseData caseData = CaseDataBuilder.builder()
                .mediation(new Mediation().setMediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE)))
                .build();

            assertTrue(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(
                caseData));
        }

        @Test
        void shouldReturnFalse_whenReasonNotMatched() {
            CaseData caseData = CaseDataBuilder.builder()
                .mediation(new Mediation().setMediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE)))
                .build();

            assertFalse(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(
                caseData));
        }
    }

    @Nested
    class IsMediationUnsuccessfulReasonEqualToNotContactableDefendantOneTests {

        @Test
        void shouldReturnTrue_whenReasonMatch() {
            CaseData caseData = CaseDataBuilder.builder()
                .mediation(new Mediation().setMediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE)))
                .build();

            assertTrue(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(
                caseData));
        }

        @Test
        void shouldReturnFalse_whenReasonNotMatched() {
            CaseData caseData = CaseDataBuilder.builder()
                .mediation(new Mediation().setMediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE)))
                .build();

            assertFalse(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(
                caseData));
        }
    }

    @Nested
    class IsSDODrawnPreCPReleaseTests {

        @Test
        void shouldReturnTrue_whenNotCaseProgressionEnabledAndLocationWhiteListedAndNotWelshEnabledForMainCase() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

            assertTrue(dashboardDecisionHelper.isSDODrawnPreCPRelease(caseData));
        }

        @Test
        void shouldReturnFalse_whenEitherCaseProgressionEnabledOrLocationWhiteListedOrWelshEnabledForMainCase() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(true);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

            assertFalse(dashboardDecisionHelper.isSDODrawnPreCPRelease(caseData));

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

            assertFalse(dashboardDecisionHelper.isSDODrawnPreCPRelease(caseData));
        }
    }

    @Nested
    class HasTrackChangedTests {

        @Test
        void shouldReturnTrue_whenTrackChangedToFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));
            caseData.setResponseClaimTrack("FAST_CLAIM");

            assertTrue(dashboardDecisionHelper.hasTrackChanged(caseData));
        }

        @Test
        void shouldReturnFalse_whenFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10001));
            caseData.setResponseClaimTrack("FAST_CLAIM");

            assertFalse(dashboardDecisionHelper.hasTrackChanged(caseData));
        }

        @Test
        void shouldReturnFalse_whenTrackNotChanged() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));
            caseData.setResponseClaimTrack("SMALL_CLAIM");

            assertFalse(dashboardDecisionHelper.hasTrackChanged(caseData));
        }
    }

    @Nested
    class IsCarmApplicableCaseTests {

        @Test
        void shouldReturnTrue_whenCarmEnabledAndSmallClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));

            when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

            assertTrue(dashboardDecisionHelper.isCarmApplicableCase(caseData));
        }

        @Test
        void shouldReturnFalse_whenCarmDisabledAndSmallClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));

            when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);

            assertFalse(dashboardDecisionHelper.isCarmApplicableCase(caseData));
        }

        @Test
        void shouldReturnFalse_whenCarmEnabledAndFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10001));

            when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

            assertFalse(dashboardDecisionHelper.isCarmApplicableCase(caseData));
        }
    }

    @Nested
    class IsDashBoardEnabledForCaseTests {

        @Test
        void shouldReturnTrue_whenDashboardEnabledForCase() {
            StateFlow stateFlow = mock(StateFlow.class);
            CaseData caseData = CaseDataBuilder.builder().build();

            when(simpleStateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
            when(stateFlow.isFlagSet(FlowFlag.DASHBOARD_SERVICE_ENABLED)).thenReturn(true);

            assertTrue(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData));
        }

        @Test
        void shouldReturnFalse_whenDashboardNotEnabledForCase() {
            StateFlow stateFlow = mock(StateFlow.class);
            CaseData caseData = CaseDataBuilder.builder().build();

            when(simpleStateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
            when(stateFlow.isFlagSet(FlowFlag.DASHBOARD_SERVICE_ENABLED)).thenReturn(false);

            assertFalse(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData));
        }
    }

    @Nested
    class IsOrderMadeFastTrackTrialNotRespondedDefendantTests {

        @Test
        void shouldReturnTrue_whenFastTrackAndTrialReadyRespondent1IsNull() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTrialReadyRespondent1(null);

            when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(true);

            assertTrue(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotRespondedDefendant(caseData));
        }

        @Test
        void shouldReturnFalse_whenNotFastTrackAndTrialReadyRespondent1IsNull() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTrialReadyRespondent1(null);

            when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(false);

            assertFalse(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotRespondedDefendant(caseData));
        }

        @Test
        void shouldReturnFalse_whenFastTrackAndTrialReadyRespondent1IsSet() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTrialReadyRespondent1(YES);

            when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(true);

            assertFalse(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotRespondedDefendant(caseData));
        }
    }

    @Nested
    class IsOrderMadeFastTrackTrialNotRespondedClaimantTests {

        @Test
        void shouldReturnTrue_whenFastTrackAndTrialReadyApplicantIsNull() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTrialReadyApplicant(null);

            when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(true);

            assertTrue(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotRespondedClaimant(caseData));
        }

        @Test
        void shouldReturnFalse_whenNotFastTrackAndTrialReadyApplicantIsNull() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTrialReadyApplicant(null);

            when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(false);

            assertFalse(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotRespondedClaimant(caseData));
        }

        @Test
        void shouldReturnFalse_whenFastTrackAndTrialReadyApplicantIsSet() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTrialReadyApplicant(YES);

            when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(true);

            assertFalse(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotRespondedClaimant(caseData));
        }
    }
}
