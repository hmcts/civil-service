package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingLocationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingWindowModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.JudiciaryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PanelRequirementsModel;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.hearing.HMCLocationType.COURT;

public class HearingDetailsMapperTest {

    @Test
    void shouldReturnEmptyString_whenHearingTypeInvoked() {
        assertThat(HearingDetailsMapper.getHearingType()).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyObject_whenHearingWindowInvoked() {
        HearingWindowModel expected = HearingWindowModel.builder().build();
        assertThat(HearingDetailsMapper.getHearingWindow()).isEqualTo(expected);
    }

    @Test
    void shouldReturnValue_whenDurationInvoked() {
        assertThat(HearingDetailsMapper.getDuration()).isEqualTo(0);
    }

    @Test
    void shouldReturnHearingPriorityType_whenInvoked() {
        assertThat(HearingDetailsMapper.getHearingPriorityType()).isEqualTo("Standard");
    }

    @Test
    void shouldReturn0_whenNumberOfPhysicalAttendeesInvoked() {
        assertThat(HearingDetailsMapper.getNumberOfPhysicalAttendees()).isEqualTo(0);
    }

    @Test
    void shouldReturnFalse_whenHearingInWelshFlagInvoked() {
        assertThat(HearingDetailsMapper.getHearingInWelshFlag()).isEqualTo(false);
    }

    @Test
    void shouldReturnObjectList_whenHearingLocationsInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation("12345")
                                        .build())
            .build();

        List<HearingLocationModel> expected = List.of(HearingLocationModel.builder()
                                                       .locationId("12345")
                                                       .locationType(COURT)
                                                       .build());

        List<HearingLocationModel> actual = HearingDetailsMapper.getHearingLocations(caseData);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnFacilitiesRequired_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        assertThat(HearingDetailsMapper.getFacilitiesRequired(caseData)).isEqualTo(null);
    }

    @Test
    void shouldReturnListingComments_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        assertThat(HearingDetailsMapper.getListingComments(caseData)).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyString_whenHearingRequesterInvoked() {
        assertThat(HearingDetailsMapper.getHearingRequester()).isEqualTo("");
    }

    @Test
    void shouldReturnFalse_whenPrivateHearingRequiredFlagInvoked() {
        assertThat(HearingDetailsMapper.getPrivateHearingRequiredFlag()).isEqualTo(false);
    }

    @Test
    void shouldReturnCaseInterpreterRequired_whenInvoked() {
        assertThat(HearingDetailsMapper.getCaseInterpreterRequiredFlag()).isEqualTo(false);
    }

    @Test
    void shouldReturnPanelRequirements_whenInvoked() {
        PanelRequirementsModel expected = PanelRequirementsModel.builder().build();
        assertThat(HearingDetailsMapper.getPanelRequirements()).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyString_whenLeadJudgeContractTypeInvoked() {
        assertThat(HearingDetailsMapper.getLeadJudgeContractType()).isEqualTo("");
    }

    @Test
    void shouldReturnJudiciaryObject_whenInvoked() {
        JudiciaryModel expected = JudiciaryModel.builder().build();
        assertThat(HearingDetailsMapper.getJudiciary()).isEqualTo(expected);
    }

    @Test
    void shouldReturnFalse_whenHearingIsLinkedFlagInvoked() {
        assertThat(HearingDetailsMapper.getHearingIsLinkedFlag()).isEqualTo(false);
    }

    @Test
    void shouldReturnList_whenHearingChannelsInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        assertThat(HearingDetailsMapper.getHearingChannels(caseData)).isEqualTo(null);
    }
}
