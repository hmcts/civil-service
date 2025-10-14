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
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.DisposalHearingSdoStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.FastTrackSdoStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.NihlSdoStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.SdoPartyStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.SdoTrialStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.SmallClaimsSdoStrategy;
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

    private final SdoHelper sdoHelper = new SdoHelper(
        new SmallClaimsSdoStrategy(),
        new FastTrackSdoStrategy(),
        new NihlSdoStrategy(),
        new DisposalHearingSdoStrategy(),
        new SdoTrialStrategy(),
        new SdoPartyStrategy()
    );

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

            assertThat(sdoHelper.isSmallClaimsTrack(caseData)).isTrue();
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

            assertThat(sdoHelper.isSmallClaimsTrack(caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenNotSmallClaimsPath1or2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.isSmallClaimsTrack(caseData)).isFalse();
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

            assertThat(sdoHelper.isFastTrack(caseData)).isTrue();
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

            assertThat(sdoHelper.isFastTrack(caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenNotFastTrackPath1or2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.isFastTrack(caseData)).isFalse();
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

            assertThat(sdoHelper.isNihlFastTrack(caseData)).isTrue();
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

            assertThat(sdoHelper.isNihlFastTrack(caseData)).isTrue();
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
            assertThat(sdoHelper.getHearingLocationNihl(caseData)).isNotNull();
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
            assertThat(sdoHelper.getPhysicalTrialTextNihl(caseData)).isNotEmpty();
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
            assertThat(sdoHelper.getPhysicalTrialTextNihl(caseData)).isEmpty();
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
            assertThat(sdoHelper.isRestrictPagesNihl(caseData)).isEqualTo(isPages);
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
            assertThat(sdoHelper.isRestrictWitnessNihl(caseData)).isEqualTo(isWitness);
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
            assertThat(sdoHelper.isApplicationToRelyOnFurtherNihl(caseData)).isEqualTo(toRely ? "Yes" : "No");
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
            assertThat(sdoHelper.isClaimForPecuniaryLossNihl(caseData)).isEqualTo(isClaim);
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
            assertThat(sdoHelper.getSdoTrialMethodOfHearing(caseData)).isNotEmpty();
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
            assertThat(sdoHelper.getSdoTrialHearingTimeAllocated(caseData)).isEqualTo(isOther ? "4 days, 4 hours and 4 minutes" : "4 hours");
        }
    }

    @Nested
    class GetSmallClaimsAdditionalDirectionEnumTests {
        @Test
        void shouldReturnCorrectEnum_whenValidAdditionalDirectionName() {
            assertThat(sdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimCreditHire"))
                .isEqualTo(SmallTrack.smallClaimCreditHire);
            assertThat(sdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimRoadTrafficAccident"))
                .isEqualTo(SmallTrack.smallClaimRoadTrafficAccident);
            assertThat(sdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimDisputeResolutionHearing"))
                .isEqualTo(SmallTrack.smallClaimDisputeResolutionHearing);
            assertThat(sdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimFlightDelay"))
                .isEqualTo(SmallTrack.smallClaimFlightDelay);
        }

        @Test
        void shouldReturnNull_whenInvalidAdditionalDirectionName() {
            assertThat(sdoHelper.getSmallClaimsAdditionalDirectionEnum("test")).isEqualTo(null);
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

            assertThat(sdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimCreditHire"))
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

            assertThat(sdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident"))
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

            assertThat(sdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimDisputeResolutionHearing"))
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

            assertThat(sdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident"))
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

            assertThat(sdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimFlightDelay"))
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

            assertThat(sdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo("four hours");
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

            assertThat(sdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo("15 minutes");
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

            assertThat(sdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
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

            assertThat(sdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
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

            assertThat(sdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
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

            assertThat(sdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
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

            assertThat(sdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getDisposalHearingTimeLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getFastClaimsHearingTimeLabel(caseData)).isEqualTo("4 hours");
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

            assertThat(sdoHelper.getFastClaimsHearingTimeLabel(caseData)).isEqualTo(expected);
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getSmallClaimsMethodTelephoneHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getSmallClaimsMethodTelephoneHearingLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getSmallClaimsMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getSmallClaimsMethodVideoConferenceHearingLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.showCarmMediationSection(caseData, true)).isTrue();
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

            assertThat(sdoHelper.showCarmMediationSection(caseData, false)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenCarmEnabledAndStatementNotExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .smallClaimsMediationSectionStatement(
                    SmallClaimsMediation.builder().build())
                .build();

            assertThat(sdoHelper.showCarmMediationSection(caseData, false)).isFalse();
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

            assertThat(sdoHelper.getSmallClaimsMediationText(caseData)).isEqualTo("small claims mediation text");
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

            assertThat(sdoHelper.getSmallClaimsMediationText(caseData)).isNull();
        }

        @Test
        void shouldReturnNull_whenStatementDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build();

            assertThat(sdoHelper.getSmallClaimsMediationText(caseData)).isNull();
        }
    }

    @Nested
    class HasSharedVariableTest {
        @Test
        void shouldReturnTrue_whenApplicant2IsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build();

            assertThat(sdoHelper.hasSharedVariable(caseData, "applicant2")).isTrue();
        }

        @Test
        void shouldReturnTrue_whenRespondent2IsNotNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            assertThat(sdoHelper.hasSharedVariable(caseData, "respondent2")).isTrue();
        }

        @Test
        void shouldReturnFalse_whenNotMultiPartyOrInvalidInput() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.hasSharedVariable(caseData, "applicant2")).isFalse();
            assertThat(sdoHelper.hasSharedVariable(caseData, "respondent2")).isFalse();
            assertThat(sdoHelper.hasSharedVariable(caseData, "invalid input")).isFalse();
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

            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")).isTrue();
            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsMethodToggle")).isTrue();
            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")).isTrue();
            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle"))
                .isTrue();
            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsFlightDelayToggle"))
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

            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")).isTrue();
        }

        @Test
        void shouldReturnFalse_whenVariablesDoNotExistOrInvalidInput() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")).isFalse();
            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")).isFalse();
            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle"))
                .isFalse();
            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")).isFalse();
            assertThat(sdoHelper.hasSmallClaimsVariable(caseData, "invalid input")).isFalse();
        }
    }

    @Nested
    class GetFastTrackAdditionalDirectionEnumTests {
        @Test
        void shouldReturnCorrectEnum_whenValidAdditionalDirectionName() {
            assertThat(sdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimBuildingDispute"))
                .isEqualTo(FastTrack.fastClaimBuildingDispute);
            assertThat(sdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimClinicalNegligence"))
                .isEqualTo(FastTrack.fastClaimClinicalNegligence);
            assertThat(sdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimCreditHire"))
                .isEqualTo(FastTrack.fastClaimCreditHire);
            assertThat(sdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimEmployersLiability"))
                .isEqualTo(FastTrack.fastClaimEmployersLiability);
            assertThat(sdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimHousingDisrepair"))
                .isEqualTo(FastTrack.fastClaimHousingDisrepair);
            assertThat(sdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimPersonalInjury"))
                .isEqualTo(FastTrack.fastClaimPersonalInjury);
            assertThat(sdoHelper.getFastTrackAdditionalDirectionEnum("fastClaimRoadTrafficAccident"))
                .isEqualTo(FastTrack.fastClaimRoadTrafficAccident);
        }

        @Test
        void shouldReturnNull_whenInvalidAdditionalDirectionName() {
            assertThat(sdoHelper.getFastTrackAdditionalDirectionEnum("test")).isEqualTo(null);
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

            assertThat(sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimBuildingDispute"))
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

            assertThat(sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimClinicalNegligence"))
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

            assertThat(sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimCreditHire"))
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

            assertThat(sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimEmployersLiability"))
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

            assertThat(sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimHousingDisrepair"))
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

            assertThat(sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimPersonalInjury"))
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

            assertThat(sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimRoadTrafficAccident"))
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

            assertThat(sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimBuildingDispute"))
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

            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackAltDisputeResolutionToggle"))
                .isTrue();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackVariationOfDirectionsToggle"))
                .isTrue();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackSettlementToggle")).isTrue();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackDisclosureOfDocumentsToggle"))
                .isTrue();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackWitnessOfFactToggle")).isTrue();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackSchedulesOfLossToggle")).isTrue();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackCostsToggle")).isTrue();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialToggle")).isTrue();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackMethodToggle")).isTrue();
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

            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialDateToToggle")).isTrue();
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

            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingDateToToggle")).isTrue();
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

            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackAddNewDirections")).isTrue();
        }

        @Test
        void shouldReturnFalse_whenVariablesDoNotExistOrInvalidInput() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackAltDisputeResolutionToggle"))
                .isFalse();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackVariationOfDirectionsToggle"))
                .isFalse();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackSettlementToggle")).isFalse();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackDisclosureOfDocumentsToggle"))
                .isFalse();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackWitnessOfFactToggle")).isFalse();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackSchedulesOfLossToggle")).isFalse();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackCostsToggle")).isFalse();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialToggle")).isFalse();

            assertThat(sdoHelper.hasFastTrackVariable(caseData, "fastTrackAddNewDirections")).isFalse();
            assertThat(sdoHelper.hasFastTrackVariable(caseData, "invalid input")).isFalse();
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

            assertThat(sdoHelper.getFastTrackMethodTelephoneHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getFastTrackMethodTelephoneHearingLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getFastTrackMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getFastTrackMethodVideoConferenceHearingLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnEmptyString_whenNoTypesSelected() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getFastTrackTrialBundleTypeText(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getFastTrackAllocation(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackAllocation(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackAllocation(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getFastTrackAllocation(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getDisposalHearingFinalDisposalHearingTimeLabel(caseData)).isEqualTo("30 minutes");
        }

        @Test
        void shouldReturnEmptyString_whenFinalDisposalHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getDisposalHearingFinalDisposalHearingTimeLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getDisposalHearingMethodTelephoneHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getDisposalHearingMethodTelephoneHearingLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getDisposalHearingMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the court");
        }

        @Test
        void shouldReturnEmptyString_whenHearingIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getDisposalHearingMethodVideoConferenceHearingLabel(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
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

            assertThat(sdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnEmptyString_whenNoTypesSelected() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.getDisposalHearingBundleTypeText(caseData)).isEqualTo("");
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

            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle"))
                .isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle"))
                .isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle"))
                .isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle"))
                .isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle"))
                .isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle"))
                .isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingMethodToggle")).isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingBundleToggle")).isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle"))
                .isTrue();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingCostsToggle")).isTrue();
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

            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingAddNewDirections")).isTrue();
        }

        @Test
        void shouldReturnFalse_whenVariablesDoNotExistOrInvalidInput() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle"))
                .isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle"))
                .isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle"))
                .isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle"))
                .isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle"))
                .isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "fastTrackSchedulesOfLossToggle")).isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle"))
                .isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingBundleToggle")).isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle"))
                .isFalse();
            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingCostsToggle")).isFalse();

            assertThat(sdoHelper.hasDisposalVariable(caseData, "disposalHearingAddNewDirections")).isFalse();

            assertThat(sdoHelper.hasDisposalVariable(caseData, "invalid input")).isFalse();
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

            assertThat(sdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)).isTrue();
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

            assertThat(sdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)).isTrue();
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
                assertThat(sdoHelper.getHearingLocationDrh(caseData).getValue().getCode()).isEqualTo(location);
            } else {
                assertThat(sdoHelper.getHearingLocationDrh(caseData).getValue().getCode()).isEqualTo("alt_location");
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

            assertThat(sdoHelper.hasSdoR2HearingTrialWindow(caseData)).isEqualTo(window);
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
                assertThat(sdoHelper.getSdoR2HearingTime(caseData)).isEqualTo(selection.getLabel());
            } else {
                assertThat(sdoHelper.getSdoR2HearingTime(caseData)).isEqualTo("1 days, 2 hours, 3 minutes");
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
                assertThat(sdoHelper.getSdoR2SmallClaimsPhysicalTrialBundleTxt(caseData)).isEqualTo("None");
            } else {
                assertThat(sdoHelper.getSdoR2SmallClaimsPhysicalTrialBundleTxt(caseData)).isEqualTo("test_text");
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
                assertThat(sdoHelper.getSdoR2SmallClaimsHearingMethod(caseData)).isEqualTo("by telephone");
            } else if (key.equals("VID")) {
                assertThat(sdoHelper.getSdoR2SmallClaimsHearingMethod(caseData)).isEqualTo("by video");
            } else if (key.equals("INTER")) {
                assertThat(sdoHelper.getSdoR2SmallClaimsHearingMethod(caseData)).isEqualTo("in person");
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
                assertThat(sdoHelper.getSdoTrialMethodOfHearing(caseData)).isEqualTo("by telephone");
            } else if (key.equals("VID")) {
                assertThat(sdoHelper.getSdoTrialMethodOfHearing(caseData)).isEqualTo("by video conference");
            } else if (key.equals("INTER")) {
                assertThat(sdoHelper.getSdoTrialMethodOfHearing(caseData)).isEqualTo("in person");
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

            assertThat(sdoHelper.showCarmMediationSectionDRH(caseData, true)).isTrue();
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

            assertThat(sdoHelper.showCarmMediationSectionDRH(caseData, false)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenCarmEnabledAndStatementNotExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build().toBuilder()
                .sdoR2SmallClaimsMediationSectionStatement(
                    SdoR2SmallClaimsMediation.builder().build())
                .build();

            assertThat(sdoHelper.showCarmMediationSectionDRH(caseData, false)).isFalse();
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

            assertThat(sdoHelper.getSmallClaimsMediationTextDRH(caseData)).isEqualTo("small claims mediation text");
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

            assertThat(sdoHelper.getSmallClaimsMediationTextDRH(caseData)).isNull();
        }

        @Test
        void shouldReturnNull_whenStatementDoesNotExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build();

            assertThat(sdoHelper.getSmallClaimsMediationTextDRH(caseData)).isNull();
        }
    }

}
