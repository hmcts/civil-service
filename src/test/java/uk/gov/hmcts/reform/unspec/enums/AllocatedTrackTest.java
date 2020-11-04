package uk.gov.hmcts.reform.unspec.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.getAllocatedTrack;

class AllocatedTrackTest {

    @Nested
    class PersonalInjuryClaims {

        @ParameterizedTest(name = "{0} has small claim track when claim value is less than 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldAllocatePersonalInjuryClaimTypesBelow1000ToSmallClaim(ClaimType claimType) {
            AllocatedTrack track = getAllocatedTrack(BigDecimal.valueOf(999), claimType);

            assertThat(track).isEqualTo(SMALL_CLAIM);
        }

        @ParameterizedTest(name = "{0} has fast claim track when claim value is 1000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldAllocatePersonalInjuryClaimTypesOf1000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(1000), claimType)).isEqualTo(FAST_CLAIM);
        }

        @ParameterizedTest(name = "{0} has fast claim track when claim value is more than 1000 but less than 25001")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldAllocatePersonalInjuryClaimTypesAbove1000AndBelow25000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(25000), claimType)).isEqualTo(FAST_CLAIM);
        }

        @ParameterizedTest(name = "{0} has multi claim track when claim value is more than 25000")
        @EnumSource(
            value = ClaimType.class,
            names = {"PERSONAL_INJURY", "CLINICAL_NEGLIGENCE", "PROFESSIONAL_NEGLIGENCE"})
        void shouldAllocatePersonalInjuryClaimTypesAbove25000ToMultiClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(25001), claimType)).isEqualTo(MULTI_CLAIM);
        }
    }

    @Nested
    class OtherClaims {

        @ParameterizedTest(name = "{0} has small claim track when claim value is less than 10000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldAllocateOtherClaimTypesBelow1000ToSmallClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(9999), claimType)).isEqualTo(SMALL_CLAIM);
        }

        @ParameterizedTest(name = "{0} has fast claim track when claim value is 10000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldAllocateOtherClaimTypesOf10000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(10000), claimType)).isEqualTo(FAST_CLAIM);
        }

        @ParameterizedTest(name = "{0} has fast claim track if claim value is more than 10000 but less/equal to 25000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldAllocateOtherClaimTypesAbove10000AndBelow25000ToFastClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(25000), claimType)).isEqualTo(FAST_CLAIM);
        }

        @ParameterizedTest(name = "{0} has multi claim track when claim value is more than 25000")
        @EnumSource(
            value = ClaimType.class,
            names = {"BREACH_OF_CONTRACT", "CONSUMER", "CONSUMER_CREDIT", "OTHER"})
        void shouldAllocateOtherClaimTypesAbove25000ToMultiClaim(ClaimType claimType) {
            assertThat(getAllocatedTrack(BigDecimal.valueOf(25001), claimType)).isEqualTo(MULTI_CLAIM);
        }
    }
}
