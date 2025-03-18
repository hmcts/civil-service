package uk.gov.hmcts.reform.civil.helpers.sdo;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialHearingLengthOther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsBundleOfDocs;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingLengthOther;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate.FIFTEEN_MINUTES;
import static uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate.OTHER;

public class SdoHelperTest {

    @Nested
    class IsSmallClaimsTrackTests {
        @Test
        void shouldReturnTrue_whenSmallClaimsPath1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();

            assertThat(SdoHelper.isSmallClaimsTrack(caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenSmallClaimsPath2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YES)
                .drawDirectionsOrderSmallClaims(YES)
                .build();

            assertThat(SdoHelper.isSmallClaimsTrack(caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenNotSmallClaimsPath1or2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.isSmallClaimsTrack(caseData)).isFalse();
        }
    }

    @Nested
    class IsFastTrackTests {
        @Test
        void shouldReturnTrue_whenFastTrackPath1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .build();

            assertThat(SdoHelper.isFastTrack(caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenFastTrackPath2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YES)
                .drawDirectionsOrderSmallClaims(NO)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();

            assertThat(SdoHelper.isFastTrack(caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenNotFastTrackPath1or2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.isFastTrack(caseData)).isFalse();
        }
    }

    @Nested
    class IsNihlTests {
        @Test
        void shouldReturnTrue_whenNihlFastTrack1() {
            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimBuildingDispute);
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .orderType(OrderType.DECIDE_DAMAGES)
                .claimsTrack(ClaimsTrack.fastTrack)
                .trialAdditionalDirectionsForFastTrack(fastTrackList)
                .build();

            assertThat(SdoHelper.isNihlFastTrack(caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenNihlFastTrackPath2() {
            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(fastTrackList)
                .build();

            assertThat(SdoHelper.isNihlFastTrack(caseData)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturnLocationForNIHL(boolean isAltLoc) {
            DynamicList options = DynamicList.builder()
                .listItems(List.of(
                               DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                               DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                               DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                           )
                )
                .build();
            List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
            DynamicListElement selectedCourt = DynamicListElement.builder()
                .code("00002").label("court 2 - 2 address - Y02 7RB").build();
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2Trial(SdoR2Trial.builder()
                                .hearingCourtLocationList(isAltLoc ? null : (options.toBuilder().value(selectedCourt).build()))
                                .altHearingCourtLocationList(isAltLoc ? (options.toBuilder().value(selectedCourt).build()) : null)
                                .build())
                .build();
            assertThat(SdoHelper.getHearingLocationNihl(caseData)).isNotNull();
        }

        @Test
        void shouldreturn_physicalbundletextWhenBundleOptionIsParty() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2Trial(SdoR2Trial.builder()
                                .physicalBundlePartyTxt("Test")
                                .physicalBundleOptions(PhysicalTrialBundleOptions.PARTY).build())
                .build();
            assertThat(SdoHelper.getPhysicalTrialTextNihl(caseData)).isNotEmpty();
        }

        @Test
        void shouldreturn_emptyPhysicalbundletextWhenBundleOptionIsNone() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2Trial(SdoR2Trial.builder()
                                .physicalBundlePartyTxt("Test")
                                .physicalBundleOptions(PhysicalTrialBundleOptions.NONE).build())
                .build();
            assertThat(SdoHelper.getPhysicalTrialTextNihl(caseData)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturn_restricPages(boolean isPages) {
            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                                          .sdoRestrictPages(SdoR2RestrictPages.builder()
                                                                .isRestrictPages(isPages ? YES : NO).build())
                                          .build())
                .build();
            assertThat(SdoHelper.isRestrictPagesNihl(caseData)).isEqualTo(isPages);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturn_restricWitness(boolean isWitness) {
            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                                          .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                                                    .isRestrictWitness(isWitness ? YES : NO).build())
                                          .build())
                .build();
            assertThat(SdoHelper.isRestrictWitnessNihl(caseData)).isEqualTo(isWitness);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturn_isApplicationToRelyOnFurtherNihl(boolean toRely) {
            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                                                  .sdoApplicationToRelyOnFurther(
                                                      SdoR2ApplicationToRelyOnFurther.builder()
                                                          .doRequireApplicationToRely(toRely ? YES : NO)
                                                          .build())
                                                  .build())
                .build();
            assertThat(SdoHelper.isApplicationToRelyOnFurtherNihl(caseData)).isEqualTo(toRely ? "Yes" : "No");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturn_isClaimForPecuniaryLossNihl(boolean isClaim) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder().sdoR2ScheduleOfLossClaimantText(
                        SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT)
                                         .isClaimForPecuniaryLoss(isClaim ? YES : NO)
                                         .build())
                .build();
            assertThat(SdoHelper.isClaimForPecuniaryLossNihl(caseData)).isEqualTo(isClaim);
        }

        @Test
        void shouldReturn_method_of_hearing() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2Trial(SdoR2Trial.builder()
                                .methodOfHearing((getHearingMethodList("INTER", "In Person"))).build())
                .build();
            assertThat(SdoHelper.getSdoTrialMethodOfHearing(caseData)).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturn_getSdoTrialHearingTimeAllocated(boolean isOther) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2Trial(SdoR2Trial.builder()
                                .lengthList(isOther ? FastTrackHearingTimeEstimate.OTHER : FastTrackHearingTimeEstimate.FOUR_HOURS)
                                .lengthListOther(isOther ? SdoR2TrialHearingLengthOther.builder().trialLengthDays(4).trialLengthHours(4).trialLengthMinutes(4).build() : null)
                                .build())
                .build();
            assertThat(SdoHelper.getSdoTrialHearingTimeAllocated(caseData)).isEqualTo(isOther ? "4 days, 4 hours and 4 minutes" : "4 hours");
        }
    }

    @Nested
    class GetSmallClaimsAdditionalDirectionEnumTests {
        @Test
        void shouldReturnCorrectEnum_whenValidAdditionalDirectionName() {
            assertThat(SdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimCreditHire"))
                .isEqualTo(SmallTrack.smallClaimCreditHire);
            assertThat(SdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimRoadTrafficAccident"))
                .isEqualTo(SmallTrack.smallClaimRoadTrafficAccident);
            assertThat(SdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimDisputeResolutionHearing"))
                .isEqualTo(SmallTrack.smallClaimDisputeResolutionHearing);
            assertThat(SdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimFlightDelay"))
                .isEqualTo(SmallTrack.smallClaimFlightDelay);
        }

        @Test
        void shouldReturnNull_whenInvalidAdditionalDirectionName() {
            assertThat(SdoHelper.getSmallClaimsAdditionalDirectionEnum("test")).isEqualTo(null);
        }
    }

    @Nested
    class HasSmallAdditionalDirections {
        @Test
        void shouldReturnTrue_ifHasCreditHire() {
            List<SmallTrack> directions = List.of(SmallTrack.smallClaimCreditHire);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .smallClaims(directions)
                .build();

            assertThat(SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimCreditHire"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_ifHasRoadTrafficAccident() {
            List<SmallTrack> directions = List.of(SmallTrack.smallClaimRoadTrafficAccident);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .smallClaims(directions)
                .build();

            assertThat(SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_ifHasDisputeResolutionHearing() {
            List<SmallTrack> directions = List.of(SmallTrack.smallClaimDisputeResolutionHearing);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .smallClaims(directions)
                .build();

            assertThat(SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimDisputeResolutionHearing"))
                .isTrue();
        }

        @Test
        void shouldReturnFalse_ifHasNoAdditionalDirections() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();

            assertThat(SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident"))
                .isFalse();
        }

        @Test
        void shouldReturnTrue_ifHasFlightDelay() {
            List<SmallTrack> directions = List.of(SmallTrack.smallClaimFlightDelay);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .smallClaims(directions)
                .build();

            assertThat(SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimFlightDelay"))
                .isTrue();
        }
    }

    @Nested
    class GetSmallClaimsHearingTimeLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            SmallClaimsHearing smallClaimsHearing = SmallClaimsHearing.builder()
                .input1("input1")
                .input2("input2")
                .time(SmallClaimsTimeEstimate.FOUR_HOURS)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .smallClaimsHearing(smallClaimsHearing)
                .build();

            assertThat(SdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo("four hours");
        }

        @Test
        void shouldReturnLabel_whenHearingOther() {
            var expected = "6 hours 20 minutes";
            SmallClaimsHearing smallClaimsHearing = SmallClaimsHearing.builder()
                .input1("input1")
                .input2("input2")
                .time(SmallClaimsTimeEstimate.OTHER)
                .otherHours(new BigDecimal(6))
                .otherMinutes(new BigDecimal(20))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .smallClaimsHearing(smallClaimsHearing)
                .build();

            assertThat(SdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetDisposalHearingTimeLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            DisposalHearingHearingTime disposalHearingHearingTime = DisposalHearingHearingTime.builder()
                .input("input")
                .time(FIFTEEN_MINUTES)
                .dateFrom(LocalDate.parse("2022-01-01"))
                .dateFrom(LocalDate.parse("2022-01-02"))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingHearingTime(disposalHearingHearingTime)
                .build();

            assertThat(SdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo("15 minutes");
        }

        @Test
        void shouldReturnLabel_whenHearingOther() {
            var expected = "6 hours 20 minutes";
            DisposalHearingHearingTime disposalHearingHearingTime = DisposalHearingHearingTime.builder()
                .input("input")
                .time(OTHER)
                .dateFrom(LocalDate.parse("2022-01-01"))
                .dateFrom(LocalDate.parse("2022-01-02"))
                .otherHours("6")
                .otherMinutes("20")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingHearingTime(disposalHearingHearingTime)
                .build();

            assertThat(SdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnLabel_whenHearingOtherAsOneHourOneMinute() {
            var expected = "1 hour 1 minute";
            DisposalHearingHearingTime disposalHearingHearingTime = DisposalHearingHearingTime.builder()
                .input("input")
                .time(OTHER)
                .dateFrom(LocalDate.parse("2022-01-01"))
                .dateFrom(LocalDate.parse("2022-01-02"))
                .otherHours("1")
                .otherMinutes("1")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingHearingTime(disposalHearingHearingTime)
                .build();

            assertThat(SdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnLabel_whenHearingOtherHourZero() {
            var expected = "20 minutes";
            DisposalHearingHearingTime disposalHearingHearingTime = DisposalHearingHearingTime.builder()
                .input("input")
                .time(OTHER)
                .dateFrom(LocalDate.parse("2022-01-01"))
                .dateFrom(LocalDate.parse("2022-01-02"))
                .otherHours("0")
                .otherMinutes("20")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingHearingTime(disposalHearingHearingTime)
                .build();

            assertThat(SdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnLabel_whenHearingOtherMinuteZero() {
            var expected = "6 hours";
            DisposalHearingHearingTime disposalHearingHearingTime = DisposalHearingHearingTime.builder()
                .input("input")
                .time(OTHER)
                .dateFrom(LocalDate.parse("2022-01-01"))
                .dateFrom(LocalDate.parse("2022-01-02"))
                .otherHours("6")
                .otherMinutes("0")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingHearingTime(disposalHearingHearingTime)
                .build();

            assertThat(SdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnLabel_whenHearingOtherMinuteZeroOneHour() {
            var expected = "1 hour";
            DisposalHearingHearingTime disposalHearingHearingTime = DisposalHearingHearingTime.builder()
                .input("input")
                .time(OTHER)
                .dateFrom(LocalDate.parse("2022-01-01"))
                .dateFrom(LocalDate.parse("2022-01-02"))
                .otherHours("1")
                .otherMinutes("0")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingHearingTime(disposalHearingHearingTime)
                .build();

            assertThat(SdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetFastClaimsHearingTimeLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            FastTrackHearingTime fastTrackHearingTime = FastTrackHearingTime.builder()
                .helpText1("helpText1")
                .hearingDuration(FastTrackHearingTimeEstimate.FOUR_HOURS)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackHearingTime(fastTrackHearingTime)
                .build();

            assertThat(SdoHelper.getFastClaimsHearingTimeLabel(caseData)).isEqualTo("4 hours");
        }

        @Test
        void shouldReturnLabel_whenHearingOther() {
            var expected = "6 hours 20 minutes";
            FastTrackHearingTime fastTrackHearingTime = FastTrackHearingTime.builder()
                .helpText1("helpText1")
                .hearingDuration(FastTrackHearingTimeEstimate.OTHER)
                .otherHours("6")
                .otherMinutes("20")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackHearingTime(fastTrackHearingTime)
                .build();

            assertThat(SdoHelper.getFastClaimsHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetSmallClaimsMethodTelephoneHearingLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .smallClaimsMethodTelephoneHearing(SmallClaimsMethodTelephoneHearing.telephoneTheCourt)
                .build();

            assertThat(SdoHelper.getSmallClaimsMethodTelephoneHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getSmallClaimsMethodTelephoneHearingLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetSmallClaimsMethodVideoConferenceHearingLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .smallClaimsMethodVideoConferenceHearing(SmallClaimsMethodVideoConferenceHearing.videoTheCourt)
                .build();

            assertThat(SdoHelper.getSmallClaimsMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getSmallClaimsMethodVideoConferenceHearingLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class CarmMediationSectionTest {

        @Test
        void shouldReturnTrue_whenCarmEnabledAndStatementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .smallClaimsMediationSectionStatement(
                    SmallClaimsMediation.builder()
                        .input("small claims mediation text")
                        .build())
                .build();

            assertThat(SdoHelper.showCarmMediationSection(caseData, true)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenCarmNotEnabledAndStatementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .smallClaimsMediationSectionStatement(
                    SmallClaimsMediation.builder()
                        .input("small claims mediation text")
                        .build())
                .build();

            assertThat(SdoHelper.showCarmMediationSection(caseData, false)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenCarmEnabledAndStatementNotExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .smallClaimsMediationSectionStatement(
                    SmallClaimsMediation.builder().build())
                .build();

            assertThat(SdoHelper.showCarmMediationSection(caseData, false)).isFalse();
        }

        @Test
        void shouldReturnText_whenStatementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .smallClaimsMediationSectionStatement(
                    SmallClaimsMediation.builder()
                        .input("small claims mediation text")
                        .build())
                .build();

            assertThat(SdoHelper.getSmallClaimsMediationText(caseData)).isEqualTo("small claims mediation text");
        }

        @Test
        void shouldReturnNull_whenStatementExistsTextIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .smallClaimsMediationSectionStatement(
                    SmallClaimsMediation.builder()
                        .build())
                .build();

            assertThat(SdoHelper.getSmallClaimsMediationText(caseData)).isNull();
        }

        @Test
        void shouldReturnNull_whenStatementDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build();

            assertThat(SdoHelper.getSmallClaimsMediationText(caseData)).isNull();
        }
    }

    @Nested
    class HasSharedVariableTest {
        @Test
        void shouldReturnTrue_whenApplicant2IsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build();

            assertThat(SdoHelper.hasSharedVariable(caseData, "applicant2")).isTrue();
        }

        @Test
        void shouldReturnTrue_whenRespondent2IsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            assertThat(SdoHelper.hasSharedVariable(caseData, "respondent2")).isTrue();
        }

        @Test
        void shouldReturnFalse_whenNotMultiPartyOrInvalidInput() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.hasSharedVariable(caseData, "applicant2")).isFalse();
            assertThat(SdoHelper.hasSharedVariable(caseData, "respondent2")).isFalse();
            assertThat(SdoHelper.hasSharedVariable(caseData, "invalid input")).isFalse();
        }
    }

    @Nested
    class HasSmallClaimsVariableTest {
        @Test
        void shouldReturnTrue_whenTogglesExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .smallClaimsHearingToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .smallClaimsMethodToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .smallClaimsDocumentsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .smallClaimsWitnessStatementToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .smallClaimsFlightDelayToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .build();

            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")).isTrue();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsMethodToggle")).isTrue();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")).isTrue();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle"))
                .isTrue();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsFlightDelayToggle"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_whenNewDirectionsExist() {
            SmallClaimsAddNewDirections smallClaimsAddNewDirections = SmallClaimsAddNewDirections.builder()
                .directionComment("test")
                .build();

            Element<SmallClaimsAddNewDirections> smallClaimsAddNewDirectionsElement =
                Element.<SmallClaimsAddNewDirections>builder()
                    .value(smallClaimsAddNewDirections)
                    .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .smallClaimsAddNewDirections(List.of(smallClaimsAddNewDirectionsElement))
                .build();

            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")).isTrue();
        }

        @Test
        void shouldReturnFalse_whenVariablesDoNotExistOrInvalidInput() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")).isFalse();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")).isFalse();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle"))
                .isFalse();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")).isFalse();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "invalid input")).isFalse();
        }
    }

    @Nested
    class GetFastTrackAdditionalDirectionEnumTests {
        @Test
        void shouldReturnCorrectEnum_whenValidAdditionalDirectionName() {
            assertThat(SdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimBuildingDispute"))
                .isEqualTo(FastTrack.fastClaimBuildingDispute);
            assertThat(SdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimClinicalNegligence"))
                .isEqualTo(FastTrack.fastClaimClinicalNegligence);
            assertThat(SdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimCreditHire"))
                .isEqualTo(FastTrack.fastClaimCreditHire);
            assertThat(SdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimEmployersLiability"))
                .isEqualTo(FastTrack.fastClaimEmployersLiability);
            assertThat(SdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimHousingDisrepair"))
                .isEqualTo(FastTrack.fastClaimHousingDisrepair);
            assertThat(SdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimPersonalInjury"))
                .isEqualTo(FastTrack.fastClaimPersonalInjury);
            assertThat(SdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimRoadTrafficAccident"))
                .isEqualTo(FastTrack.fastClaimRoadTrafficAccident);
        }

        @Test
        void shouldReturnNull_whenInvalidAdditionalDirectionName() {
            assertThat(SdoHelper.getFastTrackAdditionalDirectionEnum("test")).isEqualTo(null);
        }
    }

    @Nested
    class HasFastAdditionalDirections {
        @Test
        void shouldReturnTrue_ifHasBuildingDispute() {
            List<FastTrack> directions = List.of(FastTrack.fastClaimBuildingDispute);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(directions)
                .build();

            assertThat(SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimBuildingDispute"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_ifHasClinicalNegligence() {
            List<FastTrack> directions = List.of(FastTrack.fastClaimClinicalNegligence);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(directions)
                .build();

            assertThat(SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimClinicalNegligence"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_ifHasCreditHire() {
            List<FastTrack> directions = List.of(FastTrack.fastClaimCreditHire);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(directions)
                .build();

            assertThat(SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimCreditHire"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_ifHasEmployersLiability() {
            List<FastTrack> directions = List.of(FastTrack.fastClaimEmployersLiability);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(directions)
                .build();

            assertThat(SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimEmployersLiability"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_ifHasHousingDisrepair() {
            List<FastTrack> directions = List.of(FastTrack.fastClaimHousingDisrepair);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(directions)
                .build();

            assertThat(SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimHousingDisrepair"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_ifHasPersonalInjury() {
            List<FastTrack> directions = List.of(FastTrack.fastClaimPersonalInjury);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(directions)
                .build();

            assertThat(SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimPersonalInjury"))
                .isTrue();
        }

        @Test
        void shouldReturnTrue_ifHasRoadTrafficAccident() {
            List<FastTrack> directions = List.of(FastTrack.fastClaimRoadTrafficAccident);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(directions)
                .build();

            assertThat(SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimRoadTrafficAccident"))
                .isTrue();
        }

        @Test
        void shouldReturnFalse_ifHasNoAdditionalDirections() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .build();

            assertThat(SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimBuildingDispute"))
                .isFalse();
        }
    }

    @Nested
    class HasFastTrackVariableTest {
        @Test
        void shouldReturnTrue_whenTogglesExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackAltDisputeResolutionToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .fastTrackVariationOfDirectionsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .fastTrackSettlementToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .fastTrackDisclosureOfDocumentsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .fastTrackWitnessOfFactToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .fastTrackSchedulesOfLossToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .fastTrackCostsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .fastTrackMethodToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .fastTrackTrialToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .build();

            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackAltDisputeResolutionToggle"))
                .isTrue();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackVariationOfDirectionsToggle"))
                .isTrue();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackSettlementToggle")).isTrue();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackDisclosureOfDocumentsToggle"))
                .isTrue();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackWitnessOfFactToggle")).isTrue();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackSchedulesOfLossToggle")).isTrue();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackCostsToggle")).isTrue();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialToggle")).isTrue();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackMethodToggle")).isTrue();
        }

        @Test
        void shouldReturnTrue_whenFastDateToTogglesExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
                .atStateSdoFastTrackTrial()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .build();

            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialDateToToggle")).isTrue();
        }

        @Test
        void shouldReturnTrue_whenDisposalHearingDateToToggleExist() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .atStateClaimIssuedTrialHearing()
                .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
                .atStateClaimIssuedTrialSDOInPersonHearing()
                .atStateClaimIssuedTrialLocationInPerson()
                .atStateSdoTrialDj()
                .build();

            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingDateToToggle")).isTrue();
        }

        @Test
        void shouldReturnTrue_whenNewDirectionsExist() {
            FastTrackAddNewDirections fastTrackAddNewDirections = FastTrackAddNewDirections.builder()
                .directionComment("test")
                .build();

            Element<FastTrackAddNewDirections> fastTrackAddNewDirectionsElement =
                Element.<FastTrackAddNewDirections>builder()
                .value(fastTrackAddNewDirections)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackAddNewDirections(List.of(fastTrackAddNewDirectionsElement))
                .build();

            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackAddNewDirections")).isTrue();
        }

        @Test
        void shouldReturnFalse_whenVariablesDoNotExistOrInvalidInput() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackAltDisputeResolutionToggle"))
                .isFalse();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackVariationOfDirectionsToggle"))
                .isFalse();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackSettlementToggle")).isFalse();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackDisclosureOfDocumentsToggle"))
                .isFalse();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackWitnessOfFactToggle")).isFalse();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackSchedulesOfLossToggle")).isFalse();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackCostsToggle")).isFalse();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialToggle")).isFalse();

            assertThat(SdoHelper.hasFastTrackVariable(caseData, "fastTrackAddNewDirections")).isFalse();
            assertThat(SdoHelper.hasFastTrackVariable(caseData, "invalid input")).isFalse();
        }
    }

    @Nested
    class GetFastTrackMethodTelephoneHearingLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackMethodTelephoneHearing(FastTrackMethodTelephoneHearing.telephoneTheCourt)
                .build();

            assertThat(SdoHelper.getFastTrackMethodTelephoneHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getFastTrackMethodTelephoneHearingLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetFastTrackMethodVideoConferenceHearingLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackMethodVideoConferenceHearing(FastTrackMethodVideoConferenceHearing.videoTheCourt)
                .build();

            assertThat(SdoHelper.getFastTrackMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getFastTrackMethodVideoConferenceHearingLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetFastTrackTrialBundleTypeText {
        @Test
        void shouldReturnText_whenAllThreeTypesSelected() {
            List<FastTrackTrialBundleType> fastTrackTrialBundleTypes = List.of(
                FastTrackTrialBundleType.DOCUMENTS,
                FastTrackTrialBundleType.ELECTRONIC,
                FastTrackTrialBundleType.SUMMARY
            );

            FastTrackTrial fastTrackTrial = FastTrackTrial.builder()
                .input1("test1")
                .date1(LocalDate.now())
                .date2(LocalDate.now())
                .input2("test2")
                .input3("test3")
                .type(fastTrackTrialBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackTrial(fastTrackTrial)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / an electronic bundle of digital documents"
                + " / a case summary containing no more than 500 words";

            assertThat(SdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenDocumentsAndElectronicTypesSelected() {
            List<FastTrackTrialBundleType> fastTrackTrialBundleTypes = List.of(
                FastTrackTrialBundleType.DOCUMENTS,
                FastTrackTrialBundleType.ELECTRONIC
            );

            FastTrackTrial fastTrackTrial = FastTrackTrial.builder()
                .input1("test1")
                .date1(LocalDate.now())
                .date2(LocalDate.now())
                .input2("test2")
                .input3("test3")
                .type(fastTrackTrialBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackTrial(fastTrackTrial)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / an electronic bundle of digital documents";

            assertThat(SdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenDocumentsAndSummaryTypesSelected() {
            List<FastTrackTrialBundleType> fastTrackTrialBundleTypes = List.of(
                FastTrackTrialBundleType.DOCUMENTS,
                FastTrackTrialBundleType.SUMMARY
            );

            FastTrackTrial fastTrackTrial = FastTrackTrial.builder()
                .input1("test1")
                .date1(LocalDate.now())
                .date2(LocalDate.now())
                .input2("test2")
                .input3("test3")
                .type(fastTrackTrialBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackTrial(fastTrackTrial)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / a case summary containing no more than 500 words";

            assertThat(SdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenElectronicAndSummaryTypesSelected() {
            List<FastTrackTrialBundleType> fastTrackTrialBundleTypes = List.of(
                FastTrackTrialBundleType.ELECTRONIC,
                FastTrackTrialBundleType.SUMMARY
            );

            FastTrackTrial fastTrackTrial = FastTrackTrial.builder()
                .input1("test1")
                .date1(LocalDate.now())
                .date2(LocalDate.now())
                .input2("test2")
                .input3("test3")
                .type(fastTrackTrialBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackTrial(fastTrackTrial)
                .build();

            String expectedText = "an electronic bundle of digital documents"
                + " / a case summary containing no more than 500 words";

            assertThat(SdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlyDocumentsTypeSelected() {
            List<FastTrackTrialBundleType> fastTrackTrialBundleTypes = List.of(
                FastTrackTrialBundleType.DOCUMENTS
            );

            FastTrackTrial fastTrackTrial = FastTrackTrial.builder()
                .input1("test1")
                .date1(LocalDate.now())
                .date2(LocalDate.now())
                .input2("test2")
                .input3("test3")
                .type(fastTrackTrialBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackTrial(fastTrackTrial)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered";

            assertThat(SdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlyElectronicTypeSelected() {
            List<FastTrackTrialBundleType> fastTrackTrialBundleTypes = List.of(
                FastTrackTrialBundleType.ELECTRONIC
            );

            FastTrackTrial fastTrackTrial = FastTrackTrial.builder()
                .input1("test1")
                .date1(LocalDate.now())
                .date2(LocalDate.now())
                .input2("test2")
                .input3("test3")
                .type(fastTrackTrialBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackTrial(fastTrackTrial)
                .build();

            String expectedText = "an electronic bundle of digital documents";

            assertThat(SdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlySummaryTypeSelected() {
            List<FastTrackTrialBundleType> fastTrackTrialBundleTypes = List.of(
                FastTrackTrialBundleType.SUMMARY
            );

            FastTrackTrial fastTrackTrial = FastTrackTrial.builder()
                .input1("test1")
                .date1(LocalDate.now())
                .date2(LocalDate.now())
                .input2("test2")
                .input3("test3")
                .type(fastTrackTrialBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackTrial(fastTrackTrial)
                .build();

            String expectedText = "a case summary containing no more than 500 words";

            assertThat(SdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnEmptyString_whenNoTypesSelected() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetFastTrackAllocationText {
        @Test
        void shouldReturnText_whenComplexityBandChosenWithReasons() {
            FastTrackAllocation fastTrackAllocation = FastTrackAllocation.builder()
                .assignComplexityBand(YES)
                .band(ComplexityBand.BAND_2)
                .reasons("some reasons")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackAllocation(fastTrackAllocation)
                .build();

            String expectedText = "The claim is allocated to the Fast Track and is assigned to complexity band 2 because some reasons";

            assertThat(SdoHelper.getFastTrackAllocation(caseData, true)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenComplexityBandChosenNoReasons() {
            FastTrackAllocation fastTrackAllocation = FastTrackAllocation.builder()
                .assignComplexityBand(YES)
                .band(ComplexityBand.BAND_2)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackAllocation(fastTrackAllocation)
                .build();

            String expectedText = "The claim is allocated to the Fast Track and is assigned to complexity band 2";

            assertThat(SdoHelper.getFastTrackAllocation(caseData, true)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenNoComplexityBandChosenNoReasons() {
            FastTrackAllocation fastTrackAllocation = FastTrackAllocation.builder()
                .assignComplexityBand(NO)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackAllocation(fastTrackAllocation)
                .build();

            String expectedText = "The claim is allocated to the Fast Track and is not assigned to a complexity band";

            assertThat(SdoHelper.getFastTrackAllocation(caseData, true)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenNoComplexityBandChosenWithReasons() {
            FastTrackAllocation fastTrackAllocation = FastTrackAllocation.builder()
                .assignComplexityBand(NO)
                .reasons("some more reasons")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .fastTrackAllocation(fastTrackAllocation)
                .build();

            String expectedText = "The claim is allocated to the Fast Track and is not assigned to a complexity band because some more reasons";

            assertThat(SdoHelper.getFastTrackAllocation(caseData, true)).isEqualTo(expectedText);
        }
    }

    @Nested
    class GetDisposalHearingFinalDisposalHearingTimeLabel {
        @Test
        void shouldReturnLabel_whenFinalDisposalHearingIsNotNull() {
            DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearing =
                DisposalHearingFinalDisposalHearing.builder()
                    .input("test")
                    .date(LocalDate.now())
                    .time(DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES)
                    .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingFinalDisposalHearing(disposalHearingFinalDisposalHearing)
                .build();

            assertThat(SdoHelper.getDisposalHearingFinalDisposalHearingTimeLabel(caseData)).isEqualTo("30 minutes");
        }

        @Test
        void shouldReturnEmptyString_whenFinalDisposalHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getDisposalHearingFinalDisposalHearingTimeLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetDisposalHearingMethodTelephoneHearingLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingMethodTelephoneHearing(DisposalHearingMethodTelephoneHearing.telephoneTheCourt)
                .build();

            assertThat(SdoHelper.getDisposalHearingMethodTelephoneHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getDisposalHearingMethodTelephoneHearingLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetDisposalHearingMethodVideoConferenceHearingLabelTest {
        @Test
        void shouldReturnLabel_whenHearingIsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingMethodVideoConferenceHearing(DisposalHearingMethodVideoConferenceHearing.videoTheCourt)
                .build();

            assertThat(SdoHelper.getDisposalHearingMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getDisposalHearingMethodVideoConferenceHearingLabel(caseData)).isEqualTo("");
        }
    }

    @Nested
    class GetDisposalHearingBundleTypeText {
        @Test
        void shouldReturnText_whenAllThreeTypesSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.DOCUMENTS,
                DisposalHearingBundleType.ELECTRONIC,
                DisposalHearingBundleType.SUMMARY
            );

            DisposalHearingBundle disposalHearingBundle = DisposalHearingBundle.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundle(disposalHearingBundle)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / an electronic bundle of digital documents"
                + " / a case summary containing no more than 500 words";

            assertThat(SdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenDocumentsAndElectronicTypesSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.DOCUMENTS,
                DisposalHearingBundleType.ELECTRONIC
            );

            DisposalHearingBundle disposalHearingBundle = DisposalHearingBundle.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundle(disposalHearingBundle)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / an electronic bundle of digital documents";

            assertThat(SdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenDocumentsAndSummaryTypesSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.DOCUMENTS,
                DisposalHearingBundleType.SUMMARY
            );

            DisposalHearingBundle disposalHearingBundle = DisposalHearingBundle.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundle(disposalHearingBundle)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / a case summary containing no more than 500 words";

            assertThat(SdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenElectronicAndSummaryTypesSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.ELECTRONIC,
                DisposalHearingBundleType.SUMMARY
            );

            DisposalHearingBundle disposalHearingBundle = DisposalHearingBundle.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundle(disposalHearingBundle)
                .build();

            String expectedText = "an electronic bundle of digital documents"
                + " / a case summary containing no more than 500 words";

            assertThat(SdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlyDocumentsTypeSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.DOCUMENTS
            );

            DisposalHearingBundle disposalHearingBundle = DisposalHearingBundle.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundle(disposalHearingBundle)
                .build();

            String expectedText = "an indexed bundle of documents, with each page clearly numbered";

            assertThat(SdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlyElectronicTypeSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.ELECTRONIC
            );

            DisposalHearingBundle disposalHearingBundle = DisposalHearingBundle.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundle(disposalHearingBundle)
                .build();

            String expectedText = "an electronic bundle of digital documents";

            assertThat(SdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenOnlySummaryTypeSelected() {
            List<DisposalHearingBundleType> disposalHearingBundleTypes = List.of(
                DisposalHearingBundleType.SUMMARY
            );

            DisposalHearingBundle disposalHearingBundle = DisposalHearingBundle.builder()
                .input("test")
                .type(disposalHearingBundleTypes)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundle(disposalHearingBundle)
                .build();

            String expectedText = "a case summary containing no more than 500 words";

            assertThat(SdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnEmptyString_whenNoTypesSelected() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo("");
        }
    }

    @Nested
    class HasDisposalVariableTest {
        @Test
        void shouldReturnTrue_whenTogglesExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingDisclosureOfDocumentsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingWitnessOfFactToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingMedicalEvidenceToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingQuestionsToExpertsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingSchedulesOfLossToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingFinalDisposalHearingToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingMethodToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingBundleToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingClaimSettlingToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .disposalHearingCostsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                .build();

            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle"))
                .isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle"))
                .isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle"))
                .isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle"))
                .isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle"))
                .isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle"))
                .isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingMethodToggle")).isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingBundleToggle")).isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle"))
                .isTrue();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingCostsToggle")).isTrue();
        }

        @Test
        void shouldReturnTrue_whenNewDirectionsExist() {
            DisposalHearingAddNewDirections disposalHearingAddNewDirections = DisposalHearingAddNewDirections.builder()
                .directionComment("test")
                .build();

            Element<DisposalHearingAddNewDirections> disposalHearingAddNewDirectionsElement =
                Element.<DisposalHearingAddNewDirections>builder()
                    .value(disposalHearingAddNewDirections)
                    .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingAddNewDirections(List.of(disposalHearingAddNewDirectionsElement))
                .build();

            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingAddNewDirections")).isTrue();
        }

        @Test
        void shouldReturnFalse_whenVariablesDoNotExistOrInvalidInput() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle"))
                .isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle"))
                .isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle"))
                .isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle"))
                .isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle"))
                .isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "fastTrackSchedulesOfLossToggle")).isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle"))
                .isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingBundleToggle")).isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle"))
                .isFalse();
            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingCostsToggle")).isFalse();

            assertThat(SdoHelper.hasDisposalVariable(caseData, "disposalHearingAddNewDirections")).isFalse();

            assertThat(SdoHelper.hasDisposalVariable(caseData, "invalid input")).isFalse();
        }
    }

    @Nested
    class SmallClaimsDrhTests {
        @Test
        void shouldReturnTrue_whenSmallClaimsDrhCase_Path1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .build();

            assertThat(SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenSmallClaimsDrhCase_Path2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(YES)
                .drawDirectionsOrderSmallClaimsAdditionalDirections(
                    List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .build();

            assertThat(SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)).isTrue();
        }

        @ParameterizedTest
        @CsvSource({"hearing_location", "OTHER_LOCATION"})
        void shouldReturnHearingLocationsForDrh(String location) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().hearingCourtLocationList(DynamicList.builder().value(
                    DynamicListElement.builder().code(location).build()
                    ).build()).altHearingCourtLocationList(DynamicList.builder().value(
                    DynamicListElement.builder().code("alt_location").build()
                ).build()).build())
                .build();
            if (location.equals("hearing_location")) {
                assertThat(SdoHelper.getHearingLocationDrh(caseData).getValue().getCode()).isEqualTo(location);
            } else {
                assertThat(SdoHelper.getHearingLocationDrh(caseData).getValue().getCode()).isEqualTo("alt_location");
            }
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldCheckForSdoR2HearingTrialWindow(Boolean window) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().trialOnOptions(window ? HearingOnRadioOptions.HEARING_WINDOW :
                                                                                              HearingOnRadioOptions.OPEN_DATE).build())
                .build();

            assertThat(SdoHelper.hasSdoR2HearingTrialWindow(caseData)).isEqualTo(window);
        }

        @ParameterizedTest
        @EnumSource(value = SmallClaimsSdoR2TimeEstimate.class)
        void shouldReturnHearingTime(SmallClaimsSdoR2TimeEstimate selection) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().lengthList(selection).lengthListOther(
                    SdoR2SmallClaimsHearingLengthOther.builder()
                        .trialLengthDays(1).trialLengthHours(2).trialLengthMinutes(3).build()).build())
                .build();

            if (selection.getLabel() != SmallClaimsSdoR2TimeEstimate.OTHER.getLabel()) {
                assertThat(SdoHelper.getSdoR2HearingTime(caseData)).isEqualTo(selection.getLabel());
            } else {
                assertThat(SdoHelper.getSdoR2HearingTime(caseData)).isEqualTo("1 days, 2 hours, 3 minutes");
            }
        }

        @ParameterizedTest
        @EnumSource(value = SmallClaimsSdoR2PhysicalTrialBundleOptions.class)
        void shouldReturnPhysicalTrialBundleTxt(SmallClaimsSdoR2PhysicalTrialBundleOptions option) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().physicalBundleOptions(option).sdoR2SmallClaimsBundleOfDocs(
                    SdoR2SmallClaimsBundleOfDocs.builder().physicalBundlePartyTxt("test_text").build()).build())
                .build();

            if (option == SmallClaimsSdoR2PhysicalTrialBundleOptions.NO) {
                assertThat(SdoHelper.getSdoR2SmallClaimsPhysicalTrialBundleTxt(caseData)).isEqualTo("None");
            } else {
                assertThat(SdoHelper.getSdoR2SmallClaimsPhysicalTrialBundleTxt(caseData)).isEqualTo("test_text");
            }
        }

        @ParameterizedTest
        @CsvSource({"VID, Video", "INTER, In Person", "TEL, Telephone"})
        void shouldReturnHearingMethodsForDrh(String key, String value) {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().methodOfHearing(getHearingMethodList(key, value)).build())
                .build();

            if (key.equals("TEL")) {
                assertThat(SdoHelper.getSdoR2SmallClaimsHearingMethod(caseData)).isEqualTo("by telephone");
            } else if (key.equals("VID")) {
                assertThat(SdoHelper.getSdoR2SmallClaimsHearingMethod(caseData)).isEqualTo("by video");
            } else if (key.equals("INTER")) {
                assertThat(SdoHelper.getSdoR2SmallClaimsHearingMethod(caseData)).isEqualTo("in person");
            }
        }

        @ParameterizedTest
        @CsvSource({"VID, Video", "INTER, In Person", "TEL, Telephone"})
        void shouldReturn_method_of_hearingForNIHL(String key, String value) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .sdoR2Trial(SdoR2Trial.builder().methodOfHearing(getHearingMethodList(key, value)).build())
                .build();

            if (key.equals("TEL")) {
                assertThat(SdoHelper.getSdoTrialMethodOfHearing(caseData)).isEqualTo("by telephone");
            } else if (key.equals("VID")) {
                assertThat(SdoHelper.getSdoTrialMethodOfHearing(caseData)).isEqualTo("by video conference");
            } else if (key.equals("INTER")) {
                assertThat(SdoHelper.getSdoTrialMethodOfHearing(caseData)).isEqualTo("in person");
            }
        }

    }

    private DynamicList getHearingMethodList(String key, String value) {
        Category category = Category.builder().categoryKey("HearingChannel").key(key).valueEn(value).activeFlag("Y").build();
        DynamicList hearingMethodList = DynamicList.fromList(List.of(category), Category::getValueEn, null, false);
        hearingMethodList.setValue(hearingMethodList.getListItems().get(0));
        return hearingMethodList;
    }

    @Nested
    class CarmMediationSectionTestDRH {

        @Test
        void shouldReturnTrue_whenCarmEnabledAndStatementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .sdoR2SmallClaimsMediationSectionStatement(
                    SdoR2SmallClaimsMediation.builder()
                        .input("small claims mediation text")
                        .build())
                .build();

            assertThat(SdoHelper.showCarmMediationSectionDRH(caseData, true)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenCarmNotEnabledAndStatementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .sdoR2SmallClaimsMediationSectionStatement(
                    SdoR2SmallClaimsMediation.builder()
                        .input("small claims mediation text")
                        .build())
                .build();

            assertThat(SdoHelper.showCarmMediationSectionDRH(caseData, false)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenCarmEnabledAndStatementNotExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .sdoR2SmallClaimsMediationSectionStatement(
                    SdoR2SmallClaimsMediation.builder().build())
                .build();

            assertThat(SdoHelper.showCarmMediationSectionDRH(caseData, false)).isFalse();
        }

        @Test
        void shouldReturnText_whenStatementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .sdoR2SmallClaimsMediationSectionStatement(
                    SdoR2SmallClaimsMediation.builder()
                        .input("small claims mediation text")
                        .build())
                .build();

            assertThat(SdoHelper.getSmallClaimsMediationTextDRH(caseData)).isEqualTo("small claims mediation text");
        }

        @Test
        void shouldReturnNull_whenStatementExistsTextIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .sdoR2SmallClaimsMediationSectionStatement(
                    SdoR2SmallClaimsMediation.builder()
                        .build())
                .build();

            assertThat(SdoHelper.getSmallClaimsMediationTextDRH(caseData)).isNull();
        }

        @Test
        void shouldReturnNull_whenStatementDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build();

            assertThat(SdoHelper.getSmallClaimsMediationTextDRH(caseData)).isNull();
        }
    }

}
