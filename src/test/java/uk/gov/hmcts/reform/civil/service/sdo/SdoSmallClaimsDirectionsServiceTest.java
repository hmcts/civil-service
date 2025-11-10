package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoSmallClaimsDirectionsServiceTest {

    private final SdoSmallClaimsDirectionsService service = new SdoSmallClaimsDirectionsService();

    @Test
    void shouldResolveAdditionalDirections() {
        CaseData viaStandardList = CaseData.builder()
            .smallClaims(List.of(SmallTrack.smallClaimCreditHire))
            .build();

        CaseData viaDrawDirections = CaseData.builder()
            .drawDirectionsOrderSmallClaimsAdditionalDirections(List.of(SmallTrack.smallClaimRoadTrafficAccident))
            .build();

        assertThat(service.hasSmallAdditionalDirections(viaStandardList, "smallClaimCreditHire")).isTrue();
        assertThat(service.hasSmallAdditionalDirections(viaStandardList, "smallClaimRoadTrafficAccident")).isFalse();
        assertThat(service.hasSmallAdditionalDirections(viaDrawDirections, "smallClaimRoadTrafficAccident")).isTrue();
    }

    @Test
    void shouldFormatHearingTimeLabels() {
        CaseData otherEstimate = CaseData.builder()
            .smallClaimsHearing(SmallClaimsHearing.builder()
                                    .time(SmallClaimsTimeEstimate.OTHER)
                                    .otherHours(BigDecimal.valueOf(2))
                                    .otherMinutes(BigDecimal.valueOf(45))
                                    .build())
            .build();

        CaseData standardEstimate = CaseData.builder()
            .smallClaimsHearing(SmallClaimsHearing.builder()
                                    .time(SmallClaimsTimeEstimate.THREE_HOURS)
                                    .build())
            .build();

        assertThat(service.getSmallClaimsHearingTimeLabel(otherEstimate)).isEqualTo("2 hours 45 minutes");
        assertThat(service.getSmallClaimsHearingTimeLabel(standardEstimate)).isEqualTo("three hours");
    }

    @Test
    void shouldReturnMethodLabels() {
        CaseData caseData = CaseData.builder()
            .smallClaimsMethodTelephoneHearing(SmallClaimsMethodTelephoneHearing.telephoneTheClaimant)
            .smallClaimsMethodVideoConferenceHearing(SmallClaimsMethodVideoConferenceHearing.videoTheDefendant)
            .build();

        assertThat(service.getSmallClaimsMethodTelephoneHearingLabel(caseData)).isEqualTo("the claimant");
        assertThat(service.getSmallClaimsMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldDetectVariableStates() {
        CaseData caseData = CaseData.builder()
            .smallClaimsHearingToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsDocumentsToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsWitnessStatementToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsWitnessStatement(SmallClaimsWitnessStatement.builder()
                                             .smallClaimsNumberOfWitnessesToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
                                             .build())
            .smallClaimsFlightDelayToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .smallClaimsAddNewDirections(List.of())
            .sdoR2SmallClaimsUseOfWelshToggle(List.of(OrderDetailsPagesSectionsToggle.SHOW))
            .build();

        assertThat(service.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, "smallClaimsNumberOfWitnessesToggle")).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, "sdoR2SmallClaimsUseOfWelshToggle")).isTrue();
        assertThat(service.hasSmallClaimsVariable(caseData, "unknownToggle")).isFalse();
    }

    @Test
    void shouldHandleMediationSections() {
        SmallClaimsMediation standardMediation = SmallClaimsMediation.builder()
            .input("Schedule ADR")
            .build();
        SdoR2SmallClaimsMediation drhMediation = SdoR2SmallClaimsMediation.builder()
            .input("Schedule ADR")
            .build();

        CaseData caseData = CaseData.builder()
            .smallClaimsMediationSectionStatement(standardMediation)
            .sdoR2SmallClaimsMediationSectionStatement(drhMediation)
            .build();

        assertThat(service.getSmallClaimsMediationText(caseData)).isEqualTo("Schedule ADR");
        assertThat(service.showCarmMediationSection(caseData, true)).isTrue();
        assertThat(service.showCarmMediationSection(caseData, false)).isFalse();

        assertThat(service.getSmallClaimsMediationTextDrh(caseData)).isEqualTo("Schedule ADR");
        assertThat(service.showCarmMediationSectionDrh(caseData, true)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenMediationMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(service.showCarmMediationSection(caseData, true)).isFalse();
        assertThat(service.showCarmMediationSectionDrh(caseData, true)).isFalse();
        assertThat(service.getSmallClaimsMediationText(caseData)).isNull();
        assertThat(service.getSmallClaimsMediationTextDrh(caseData)).isNull();
    }
}
