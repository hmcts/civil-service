package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;

class AllocatedTrackTest {

    @Nested
    class PersonalInjuryClaims {

        @ParameterizedTest(name = "{0} has small claim track when claim value is less than 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldAllocatePersonalInjuryClaimTypesBelow1000ToSmallClaim(ClaimType claimType) {
            AllocatedTrack track = getAllocatedTrack(BigDecimal.valueOf(999), claimType, null);

            assertThat(track).isEqualTo(SMALL_CLAIM);
        }

        @ParameterizedTest(name = "{0} has small claim track when claim value is less than 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldReturnCorrectTrackForEmailSmallClaim(ClaimType claimType) {
            AllocatedTrack track = getAllocatedTrack(BigDecimal.valueOf(999), claimType, null);

            assertThat(toStringValueForEmail(track)).isEqualTo("Small Claim Track");
        }

        @ParameterizedTest(name = "{0} has small claim track when claim value is 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PROFESSIONAL_NEGLIGENCE"})
        void shouldAllocateProfessionalNegligenceClaimTypesOf1000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(1000), claimType, null)).isEqualTo(SMALL_CLAIM);
        }

        @ParameterizedTest(name = "{0} has small claim track when claim value is 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE"})
        void shouldAllocatePersonalInjuryClaimTypesOf1000ToSmallClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(1000), claimType, null)).isEqualTo(SMALL_CLAIM);
        }

        @ParameterizedTest(name = "{0} has small claim track when claim value is 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PROFESSIONAL_NEGLIGENCE"})
        void shouldReturnCorrectTrackForEmailFastClaimTypeProfessionalNegligence(ClaimType claimType) {
            assertThat(toStringValueForEmail(getAllocatedTrack(BigDecimal.valueOf(1000), claimType, null)))
                .isEqualTo("Small Claim Track");
        }

        @ParameterizedTest(name = "{0} has fast claim track when claim value is 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE"})
        void shouldReturnCorrectTrackForEmailFastClaim(ClaimType claimType) {
            assertThat(toStringValueForEmail(getAllocatedTrack(BigDecimal.valueOf(1000), claimType, null)))
                .isEqualTo("Small Claim Track");
        }

        @ParameterizedTest(name = "{0} has fast claim track when claim value is more than 1000 but less than 25001")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldAllocatePersonalInjuryClaimTypesAbove1000AndBelow25000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(25000), claimType, null)).isEqualTo(FAST_CLAIM);
        }

        @ParameterizedTest(name = "{0} has multi claim track when claim value is more than 25000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldAllocatePersonalInjuryClaimTypesAbove25000ToMultiClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(25001), claimType, null)).isEqualTo(MULTI_CLAIM);
        }

        @ParameterizedTest(name = "{0} has multi claim track when claim value is more than 25000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldReturnCorrectTrackForEmailMultiClaim(ClaimType claimType) {
            assertThat(toStringValueForEmail(getAllocatedTrack(BigDecimal.valueOf(25001), claimType, null)))
                .isEqualTo("Multi Track");
        }

        @ParameterizedTest(name = "{0} has fast track when NIHL type")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY"})
        void shouldReturnCorrectTrackForNoiseInducedHearingLossClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(null, claimType, PersonalInjuryType.NOISE_INDUCED_HEARING_LOSS))
                .isEqualTo(FAST_CLAIM);
        }
    }

    @Nested
    class OtherClaims {

        @ParameterizedTest(name = "{0} has small claim track when claim value is less than 10000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldAllocateOtherClaimTypesBelow1000ToSmallClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(9999), claimType, null)).isEqualTo(SMALL_CLAIM);
        }

        @ParameterizedTest(name = "{0} has small claim track when claim value is less than 10000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldReturnCorrectTrackForEmailSmallClaim(ClaimType claimType) {
            assertThat(toStringValueForEmail(getAllocatedTrack(BigDecimal.valueOf(9999), claimType, null)))
                .isEqualTo("Small Claim Track");
        }

        @ParameterizedTest(name = "{0} has small claim track when claim value is 10000")
        @EnumSource(
            value = ClaimType.class,
            names = {"CONSUMER"})
        void shouldAllocateConsumerClaimTypesOf10000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(10000), claimType, null)).isEqualTo(SMALL_CLAIM);
        }

        @ParameterizedTest(name = "{0} has fast claim track when claim value is 10000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER_CREDIT", "OTHER"})
        void shouldAllocateOtherClaimTypesOf10000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(10000), claimType, null)).isEqualTo(SMALL_CLAIM);
        }

        @ParameterizedTest(name = "{0} has small claim track when claim value is 10000")
        @EnumSource(
            value = ClaimType.class,
            names = {"CONSUMER"})
        void shouldReturnCorrectTrackForEmailFastClaimForTypeConsumer(ClaimType claimType) {
            assertThat(toStringValueForEmail(getAllocatedTrack(BigDecimal.valueOf(10000), claimType, null)))
                .isEqualTo("Small Claim Track");
        }

        @ParameterizedTest(name = "{0} has small claim track when claim value is 10000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER_CREDIT", "OTHER"})
        void shouldReturnCorrectTrackForEmailFastClaim(ClaimType claimType) {
            assertThat(toStringValueForEmail(getAllocatedTrack(BigDecimal.valueOf(10000), claimType, null)))
                .isEqualTo("Small Claim Track");
        }

        @ParameterizedTest(name = "{0} has fast claim track if claim value is more than 10000 but less/equal to 25000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldAllocateOtherClaimTypesAbove10000AndBelow25000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(25000), claimType, null)).isEqualTo(FAST_CLAIM);
        }

        @ParameterizedTest(name = "{0} has multi claim track when claim value is more than 25000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldAllocateOtherClaimTypesAbove25000ToMultiClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(25001), claimType, null)).isEqualTo(MULTI_CLAIM);
        }

        @ParameterizedTest(name = "{0} has multi claim track when claim value is more than 25000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldReturnCorrectTrackForEmailMultiClaim(ClaimType claimType) {
            assertThat(toStringValueForEmail(getAllocatedTrack(BigDecimal.valueOf(25001), claimType, null)))
                .isEqualTo("Multi Track");
        }

        @ParameterizedTest(name = "{0} has small track when Flight Delay type is less than 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"FLIGHT_DELAY"})
        void shouldReturnCorrectTrackForFlightDelayClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(9999), claimType, null)).isEqualTo(SMALL_CLAIM);
        }
    }
}
