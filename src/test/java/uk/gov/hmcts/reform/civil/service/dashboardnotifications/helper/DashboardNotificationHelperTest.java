package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationHelperTest {

    public static final String BASE_LOCATION = "Base Location";

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private SimpleStateFlowEngine simpleStateFlowEngine;

    @InjectMocks
    private DashboardNotificationHelper dashboardDecisionHelper;

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
                .mediation(Mediation.builder().mediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE)).build())
                .build();

            assertTrue(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(
                caseData));
        }

        @Test
        void shouldReturnFalse_whenReasonNotMatched() {
            CaseData caseData = CaseDataBuilder.builder()
                .mediation(Mediation.builder().mediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE)).build())
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
                .mediation(Mediation.builder().mediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE)).build())
                .build();

            assertTrue(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(
                caseData));
        }

        @Test
        void shouldReturnFalse_whenReasonNotMatched() {
            CaseData caseData = CaseDataBuilder.builder()
                .mediation(Mediation.builder().mediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE)).build())
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
    class IsEligibleForReconsiderationTests {

        private static Stream<Arguments> provideCsvSourceTrueCases() {
            return Stream.of(
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(500), null),
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(10000), null),
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(10000), DecisionOnRequestReconsiderationOptions.YES),
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(10000), null)
            );
        }

        private static Stream<Arguments> provideCsvSourceFalseCases() {
            return Stream.of(
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(1000), CREATE_SDO),
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(10000), CREATE_SDO),
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(10001), null),
                Arguments.of("FAST_CLAIM", BigDecimal.valueOf(1000), null)
            );
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceTrueCases")
        void shouldReturnTrue_ForGiven_whenFeatureToggleTrue(String responseClaimTrack,
                                                             BigDecimal totalClaimAmount,
                                                             DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = new CaseDataBuilder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();
            caseData.setResponseClaimTrack(responseClaimTrack);
            caseData.setTotalClaimAmount(totalClaimAmount);
            caseData.setDecisionOnRequestReconsiderationOptions(option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

            assertTrue(dashboardDecisionHelper.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceTrueCases")
        void shouldReturnTrue_ForGiven_whenWelshEnabledForMainCase(String responseClaimTrack,
                                                                   BigDecimal totalClaimAmount,
                                                                   DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = new CaseDataBuilder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();
            caseData.setResponseClaimTrack(responseClaimTrack);
            caseData.setTotalClaimAmount(totalClaimAmount);
            caseData.setDecisionOnRequestReconsiderationOptions(option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

            assertTrue(dashboardDecisionHelper.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceTrueCases")
        void shouldReturnFalse_ForGiven_whenFeatureTogglesAreFalse(String responseClaimTrack,
                                                                   BigDecimal totalClaimAmount,
                                                                   DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = new CaseDataBuilder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();
            caseData.setResponseClaimTrack(responseClaimTrack);
            caseData.setTotalClaimAmount(totalClaimAmount);
            caseData.setDecisionOnRequestReconsiderationOptions(option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

            assertFalse(dashboardDecisionHelper.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceFalseCases")
        void shouldReturnFalse_ForGiven_whenFeatureToggleTrue(String responseClaimTrack,
                                                              BigDecimal totalClaimAmount,
                                                              DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = new CaseDataBuilder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();
            caseData.setResponseClaimTrack(responseClaimTrack);
            caseData.setTotalClaimAmount(totalClaimAmount);
            caseData.setDecisionOnRequestReconsiderationOptions(option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

            assertFalse(dashboardDecisionHelper.isEligibleForReconsideration(caseData));
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
    class IsOrderMadeFastTrackTrialNotRespondedTests {

        @Test
        void shouldReturnTrue_whenFastTrackAndTrialReadyApplicantIsNull() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setDrawDirectionsOrderRequired(NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setTrialReadyApplicant(null);

            assertTrue(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData));
        }

        @Test
        void shouldReturnFalse_whenNotFastTrackAndTrialReadyApplicantIsNull() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setDrawDirectionsOrderRequired(NO);
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setTrialReadyApplicant(null);

            assertFalse(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData));
        }

        @Test
        void shouldReturnFalse_whenFastTrackAndTrialReadyApplicantIsSet() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setDrawDirectionsOrderRequired(NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setTrialReadyApplicant(YES);

            assertFalse(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData));
        }
    }
}
