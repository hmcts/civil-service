package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.assertion.DayAssert.assertThat;
import static uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions.CREATE_SDO;

@ExtendWith(MockitoExtension.class)
class SdoReconsiderationDeadlineServiceTest {

    private static final String BASE_LOCATION = "Base Location";

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private SdoFeatureToggleService featureToggleService;

    @Mock
    private Time time;

    @InjectMocks
    private SdoReconsiderationDeadlineService reconsiderationDeadlineService;

    @Test
    void shouldReturnPlus7DaysSkippingBankHolidays_whenResponseDateIsProvided() {
        LocalDate christmasDay = LocalDate.of(2025, 12, 25);
        LocalDateTime providedDate = LocalDate.of(2025, 12, 24).atTime(23, 59);
        LocalDateTime expectedDeadline = LocalDate.of(2026, 1, 1).atTime(16, 0);

        when(workingDayIndicator.isPublicHoliday(any())).thenReturn(false);
        when(workingDayIndicator.isPublicHoliday(christmasDay)).thenReturn(true);

        LocalDateTime deadline = reconsiderationDeadlineService.calculateReconsiderationDeadline(providedDate);

        assertThat(deadline).isTheSame(expectedDeadline);
    }

    @Test
    void shouldReturnPlus7Days_whenWorkingDaysThrowsAnException() {
        LocalDateTime providedDate = LocalDate.of(2025, 12, 24).atTime(23, 59);
        LocalDate now = LocalDate.of(2026, 8, 5);
        LocalDateTime expectedDeadline = now.plusDays(7).atTime(16, 0);

        when(time.now()).thenReturn(now.atStartOfDay());
        doThrow(RuntimeException.class).when(workingDayIndicator).isPublicHoliday(any());

        LocalDateTime deadline = reconsiderationDeadlineService.calculateReconsiderationDeadline(providedDate);

        assertThat(deadline).isTheSame(expectedDeadline);
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
            CaseData caseData = caseData(responseClaimTrack, totalClaimAmount, option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

            assertTrue(reconsiderationDeadlineService.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceTrueCases")
        void shouldReturnTrue_ForGiven_whenWelshEnabledForMainCase(String responseClaimTrack,
                                                                   BigDecimal totalClaimAmount,
                                                                   DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = caseData(responseClaimTrack, totalClaimAmount, option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

            assertTrue(reconsiderationDeadlineService.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceTrueCases")
        void shouldReturnFalse_ForGiven_whenFeatureTogglesAreFalse(String responseClaimTrack,
                                                                   BigDecimal totalClaimAmount,
                                                                   DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = caseData(responseClaimTrack, totalClaimAmount, option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(false);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

            assertFalse(reconsiderationDeadlineService.isEligibleForReconsideration(caseData));
        }

        @ParameterizedTest
        @MethodSource("provideCsvSourceFalseCases")
        void shouldReturnFalse_ForGiven_whenFeatureToggleTrue(String responseClaimTrack,
                                                              BigDecimal totalClaimAmount,
                                                              DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = caseData(responseClaimTrack, totalClaimAmount, option);

            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(BASE_LOCATION)).thenReturn(true);

            assertFalse(reconsiderationDeadlineService.isEligibleForReconsideration(caseData));
        }

        private CaseData caseData(String responseClaimTrack,
                                  BigDecimal totalClaimAmount,
                                  DecisionOnRequestReconsiderationOptions option) {
            CaseData caseData = new CaseDataBuilder()
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION))
                .build();
            caseData.setResponseClaimTrack(responseClaimTrack);
            caseData.setTotalClaimAmount(totalClaimAmount);
            caseData.setDecisionOnRequestReconsiderationOptions(option);
            return caseData;
        }
    }
}
