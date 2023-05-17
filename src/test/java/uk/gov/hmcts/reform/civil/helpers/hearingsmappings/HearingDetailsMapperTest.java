package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingLocationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingWindowModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.JudiciaryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PanelRequirementsModel;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.hearing.HMCLocationType.COURT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

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
    void shouldReturnFalse_whenHearingInWelshFlagInvokedAndCaseManagementLocationNull() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHearingInWelshFlagInvokedAndRegionInCaseManagementLocationNull() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().caseManagementLocation(
            CaseLocationCivil.builder().build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHearingInWelshFlagInvokedAndRegionNotWales() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().caseManagementLocation(
            CaseLocationCivil.builder().region("2").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHearingInWelshFlagInvokedAndWelshLanguageRequirementsInRespondent1DQNull() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(Respondent1DQ.builder().build())
            .build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHearingInWelshFlagInvokedAndWelshLanguageRequirementsInRespondent2DQNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(Respondent2DQ.builder().build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHearingInWelshFlagInvokedAndWelshLanguageRequirementsInApplicant1DQNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(Applicant1DQ.builder().build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHearingInWelshFlagInvokedAndWelshLanguageRequirementsInDQsNotWelsh() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ)
            .build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isFalse();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndWelshLanguageRequirementsIsWelshForApplicantDQ() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndWelshLanguageRequirementsIsWelshForRespondent2DQ() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndWelshLanguageRequirementsIsWelshForRespondent1DQ() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndWelshLanguageRequirementsIsWelshForRespondent1And2DQ() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndAllWelshLanguageRequirementsIsWelshForAllDQ() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndWelshLanguageRequirementsIsBothForRespondent1DQ() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.BOTH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndWelshLanguageRequirementsIsBothForRespondent1And2DQ() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.BOTH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.BOTH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndLanguageRequirementIsBothForAllDQ() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.BOTH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.BOTH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.BOTH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndWelshLanguageRequirementsInAnyDQBothAndWelshCombo1() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.BOTH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHearingInWelshFlagInvokedAndRegionIsWalesAndWelshLanguageRequirementsInAnyDQBothAndWelshCombo2() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.BOTH).build()).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ).applicant1DQ(applicant1DQ).caseManagementLocation(
                CaseLocationCivil.builder().region("7").build()).build();
        assertThat(HearingDetailsMapper.getHearingInWelshFlag(caseData)).isTrue();
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
        assertThat(HearingDetailsMapper.getFacilitiesRequired(caseData)).isNull();
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
    void shouldReturnNull_whenHearingChannelIsNull() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        assertThat(HearingDetailsMapper.getHearingChannels(caseData)).isEqualTo(null);
    }

    @Test
    void shouldReturnList_whenSDOFastTrackHearingChannelSelected() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedFastTrackSDOInPersonHearing()
            .build();
        assertThat(HearingDetailsMapper.getHearingChannels(caseData)).isEqualTo(List.of("In Person"));
    }

    @Test
    void shouldReturnList_whenSDOSmallClaimsHearingChannelSelected() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedSmallClaimsSDOInPersonHearing()
            .build();
        assertThat(HearingDetailsMapper.getHearingChannels(caseData)).isEqualTo(List.of("In Person"));
    }

    @Test
    void shouldReturnList_whenSDODisposalHearingChannelSelected() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
            .build();
        assertThat(HearingDetailsMapper.getHearingChannels(caseData)).isEqualTo(List.of("In Person"));
    }

    @Test
    void shouldReturnList_whenDJDisposalHearingChannelSelected() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedDisposalDJVideoCallNew()
            .build();
        assertThat(HearingDetailsMapper.getHearingChannels(caseData)).isEqualTo(List.of("Video"));
    }

    @Test
    void shouldReturnList_whenDJTrialHearingChannelSelected() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedTrialDJInPersonHearingNew()
            .build();
        assertThat(HearingDetailsMapper.getHearingChannels(caseData)).isEqualTo(List.of("In Person"));
    }

    @Test
    void getFacilitiesRequired_shouldReturnNull_whenNoDetainedIndividualFlagExist() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        assertThat(HearingDetailsMapper.getFacilitiesRequired(caseData)).isEqualTo(null);
    }

    @Test
    void getFacilitiesRequired_shouldReturnList_whenDetainedIndividualFlagExists() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .withRespondent1Flags(wrapElements(
                FlagDetail.builder()
                    .name("Detained individual")
                    .flagComment("comment")
                    .flagCode("PF0019")
                    .hearingRelevant(YES)
                    .status("Active")
                    .build()))
            .build();
        assertThat(HearingDetailsMapper.getFacilitiesRequired(caseData)).isEqualTo(List.of("11"));
    }
}
