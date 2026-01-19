package uk.gov.hmcts.reform.civil.service.dashboardnotifications.createsdo;

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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions.YES;

@ExtendWith(MockitoExtension.class)
public class CreateSdoDashboardDecisionServiceTest {

    public static final String BASE_LOCATION = "Base Location";

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CreateSdoDashboardDecisionService createSdoDashboardDecisionService;

    @Nested
    public class UploadDocumentsTest {

        @Test
        public void shouldReturnTrue_whenHasAtLeastOneUploadDocuments() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setRes1MediationDocumentsReferred(List.of(new Element<>()));
            caseData.setRes1MediationNonAttendanceDocs(null);
            caseData.setApp1MediationDocumentsReferred(null);
            caseData.setApp1MediationNonAttendanceDocs(null);

            assertTrue(createSdoDashboardDecisionService.hasUploadDocuments(caseData));

            caseData.setRes1MediationDocumentsReferred(null);
            caseData.setRes1MediationNonAttendanceDocs(List.of(new Element<>()));
            caseData.setApp1MediationDocumentsReferred(null);
            caseData.setApp1MediationNonAttendanceDocs(null);

            assertTrue(createSdoDashboardDecisionService.hasUploadDocuments(caseData));

            caseData.setRes1MediationDocumentsReferred(null);
            caseData.setRes1MediationNonAttendanceDocs(null);
            caseData.setApp1MediationDocumentsReferred(List.of(new Element<>()));
            caseData.setApp1MediationNonAttendanceDocs(null);

            assertTrue(createSdoDashboardDecisionService.hasUploadDocuments(caseData));

            caseData.setRes1MediationDocumentsReferred(null);
            caseData.setRes1MediationNonAttendanceDocs(null);
            caseData.setApp1MediationDocumentsReferred(null);
            caseData.setApp1MediationNonAttendanceDocs(List.of(new Element<>()));

            assertTrue(createSdoDashboardDecisionService.hasUploadDocuments(caseData));
        }

        @Test
        public void shouldReturnFalse_whenUploadDocumentsIsMissing() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setRes1MediationDocumentsReferred(null);
            caseData.setRes1MediationNonAttendanceDocs(null);
            caseData.setApp1MediationDocumentsReferred(null);
            caseData.setApp1MediationNonAttendanceDocs(null);

            assertFalse(createSdoDashboardDecisionService.hasUploadDocuments(caseData));
        }
    }

    @Nested
    public class IsMediationUnsuccessfulReasonEqualToNotContactableDefendantOneTests {

        @Test
        public void shouldReturnTrue_whenReasonMatch() {
            CaseData caseData = CaseDataBuilder.builder()
                .mediation(Mediation.builder().mediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE)).build())
                .build();

            assertTrue(createSdoDashboardDecisionService.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(
                caseData));
        }

        @Test
        public void shouldReturnFalse_whenReasonNotMatched() {
            CaseData caseData = CaseDataBuilder.builder()
                .mediation(Mediation.builder().mediationUnsuccessfulReasonsMultiSelect(
                    List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE)).build())
                .build();

            assertFalse(createSdoDashboardDecisionService.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(
                caseData));
        }
    }

    @Nested
    public class IsSDODrawnPreCPReleaseTests {

        @Test
        public void shouldReturnTrue_whenNotCaseProgressionEnabledAndLocationWhiteListedAndNotWelshEnabledForMainCase() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

            assertTrue(createSdoDashboardDecisionService.isSDODrawnPreCPRelease(caseData));
        }

        @Test
        public void shouldReturnFalse_whenEitherCaseProgressionEnabledOrLocationWhiteListedOrWelshEnabledForMainCase() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(true);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

            assertFalse(createSdoDashboardDecisionService.isSDODrawnPreCPRelease(caseData));

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

            assertFalse(createSdoDashboardDecisionService.isSDODrawnPreCPRelease(caseData));
        }
    }

    @Nested
    public class IsEligibleForReconsiderationTests {

        private static Stream<Arguments> provideCsvSourceTrueCases() {
            return Stream.of(
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(500), null),
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(10000), null),
                Arguments.of("SMALL_CLAIM", BigDecimal.valueOf(10000), YES),
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
        public void shouldReturnTrue_ForGiven_whenFeatureToggleTrue(String responseClaimTrack,
                                                                    BigDecimal totalClaimAmount,
                                                                    DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = new CaseDataBuilder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();
            caseData.setResponseClaimTrack(responseClaimTrack);
            caseData.setTotalClaimAmount(totalClaimAmount);
            caseData.setDecisionOnRequestReconsiderationOptions(option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

            assertTrue(createSdoDashboardDecisionService.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceTrueCases")
        public void shouldReturnTrue_ForGiven_whenWelshEnabledForMainCase(String responseClaimTrack,
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

            assertTrue(createSdoDashboardDecisionService.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceTrueCases")
        public void shouldReturnFalse_ForGiven_whenFeatureTogglesAreFalse(String responseClaimTrack,
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

            assertFalse(createSdoDashboardDecisionService.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceFalseCases")
        public void shouldReturnFalse_ForGiven_whenFeatureToggleTrue(String responseClaimTrack,
                                                                     BigDecimal totalClaimAmount,
                                                                     DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = new CaseDataBuilder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();
            caseData.setResponseClaimTrack(responseClaimTrack);
            caseData.setTotalClaimAmount(totalClaimAmount);
            caseData.setDecisionOnRequestReconsiderationOptions(option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

            assertFalse(createSdoDashboardDecisionService.isEligibleForReconsideration(caseData));
        }
    }

    @Nested
    public class HasTrackChangedTests {

        @Test
        public void shouldReturnTrue_whenTrackChangedToFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));
            caseData.setResponseClaimTrack("FAST_CLAIM");

            assertTrue(createSdoDashboardDecisionService.hasTrackChanged(caseData));
        }

        @Test
        public void shouldReturnFalse_whenFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10001));
            caseData.setResponseClaimTrack("FAST_CLAIM");

            assertFalse(createSdoDashboardDecisionService.hasTrackChanged(caseData));
        }

        @Test
        public void shouldReturnFalse_whenTrackNotChanged() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));
            caseData.setResponseClaimTrack("SMALL_CLAIM");

            assertFalse(createSdoDashboardDecisionService.hasTrackChanged(caseData));
        }
    }

    @Nested
    public class IsCarmApplicableCaseTests {

        @Test
        public void shouldReturnTrue_whenCarmEnabledAndSmallClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));

            when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

            assertTrue(createSdoDashboardDecisionService.isCarmApplicableCase(caseData));
        }

        @Test
        public void shouldReturnFalse_whenCarmDisabledAndSmallClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));

            when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);

            assertFalse(createSdoDashboardDecisionService.isCarmApplicableCase(caseData));
        }

        @Test
        public void shouldReturnFalse_whenCarmEnabledAndFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10001));

            when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

            assertFalse(createSdoDashboardDecisionService.isCarmApplicableCase(caseData));
        }
    }
}
