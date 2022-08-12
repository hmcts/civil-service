package uk.gov.hmcts.reform.civil.helpers.sdo;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SdoHelperTest {
    @Nested
    class IsSmallClaimsTrackTests {
        @Test
        void shouldReturnTrue_whenSmallClaimsPath1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
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
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
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
                .drawDirectionsOrderRequired(YesOrNo.NO)
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
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
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
    class GetSmallClaimsAdditionalDirectionEnumTests {
        @Test
        void shouldReturnCorrectEnum_whenValidAdditionalDirectionName() {
            assertThat(SdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimCreditHire"))
                .isEqualTo(SmallTrack.smallClaimCreditHire);
            assertThat(SdoHelper.getSmallClaimsAdditionalDirectionEnum("smallClaimRoadTrafficAccident"))
                .isEqualTo(SmallTrack.smallClaimRoadTrafficAccident);
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
                .drawDirectionsOrderRequired(YesOrNo.NO)
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
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .smallClaims(directions)
                .build();

            assertThat(SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident"))
                .isTrue();
        }

        @Test
        void shouldReturnFalse_ifHasNoAdditionalDirections() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();

            assertThat(SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident"))
                .isFalse();
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

            assertThat(SdoHelper.getSmallClaimsHearingTimeLabel(caseData)).isEqualTo("Four hours");
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
                .build();

            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")).isTrue();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsMethodToggle")).isTrue();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")).isTrue();
            assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle"))
                .isTrue();
        }
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
        assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsMethodToggle")).isFalse();
        assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")).isFalse();
        assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle"))
            .isFalse();
        assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")).isFalse();
        assertThat(SdoHelper.hasSmallClaimsVariable(caseData, "invalid input")).isFalse();
    }
}
