package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class CaseFlagsHearingsUtilsTest {

    @Test
    void shouldReturnAllActiveCaseFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateRespondentFullDefence()
            .withRespondent1Flags()
            .withApplicant1Flags()
            .withRespondent1LitigationFriendFlags()
            .withRespondent1WitnessFlags()
            .withRespondent1ExpertFlags()
            .build();

        List<Flags> expectedFlags = new ArrayList<>();

        expectedFlags.add(getRespondent1Flags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getApplicant1Flags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent1LitFriendFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent1ExpertsFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent1WitnessFlags(caseData, getAllActiveFlagDetails()));

        List<Flags> actualFlags = CaseFlagsHearingsUtils.getAllActiveFlags(caseData);

        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    @Test
    void shouldReturnAllHearingRelevantCaseFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateRespondentFullDefence()
            .withRespondent1Flags()
            .withApplicant1Flags()
            .withRespondent1LitigationFriendFlags()
            .withRespondent1WitnessFlags()
            .withRespondent1ExpertFlags()
            .build();

        List<Flags> expectedFlags = new ArrayList<>();

        expectedFlags.add(getRespondent1Flags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getApplicant1Flags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent1LitFriendFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent1ExpertsFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent1WitnessFlags(caseData, getAllActiveHearingRelevantFlagDetails()));

        List<Flags> activeFlags = CaseFlagsHearingsUtils.getAllActiveFlags(caseData);
        List<Flags> actualFlags = CaseFlagsHearingsUtils.getAllHearingRelevantCaseFlags(activeFlags);

        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    @Test
    void shouldReturnAllCaseFlags_withSMFlagCode() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateRespondentFullDefence()
            .withRespondent1Flags()
            .withApplicant1Flags()
            .withRespondent1LitigationFriendFlags()
            .withRespondent1WitnessFlags()
            .withRespondent1ExpertFlags()
            .build();

        List<Flags> expectedFlags = new ArrayList<>();

        expectedFlags.add(getRespondent1Flags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getApplicant1Flags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent1LitFriendFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent1ExpertsFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent1WitnessFlags(caseData, getAllActiveSMCodeFlagDetails()));

        List<Flags> activeFlags = CaseFlagsHearingsUtils.getAllActiveFlags(caseData);
        List<Flags> actualFlags = CaseFlagsHearingsUtils.getSMCodeFlags(activeFlags);

        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    @Test
    void shouldReturnAllCaseFlags_withRAFlagCode() {
        CaseData caseData = CaseDataBuilder.builder()
            .addRespondent1LitigationFriend()
            .atStateRespondentFullDefence()
            .withRespondent1Flags()
            .withApplicant1Flags()
            .withRespondent1LitigationFriendFlags()
            .withRespondent1WitnessFlags()
            .withRespondent1ExpertFlags()
            .build();

        List<Flags> expectedFlags = new ArrayList<>();

        expectedFlags.add(getRespondent1Flags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getApplicant1Flags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent1LitFriendFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent1ExpertsFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent1WitnessFlags(caseData, getAllActiveRACodeFlagDetails()));

        List<Flags> activeFlags = CaseFlagsHearingsUtils.getAllActiveFlags(caseData);
        List<Flags> actualFlags = CaseFlagsHearingsUtils.getRACodeFlags(activeFlags);

        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    private Flags getRespondent1Flags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty(caseData.getRespondent1().getPartyName(), "Respondent 1", details);
    }

    private Flags getApplicant1Flags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty(caseData.getApplicant1().getPartyName(), "Applicant 1", details);
    }

    private Flags getRespondent1LitFriendFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty(caseData.getRespondent1LitigationFriend().getFullName(), "Respondent 1 Litigation Friend", details);
    }

    private Flags getRespondent1WitnessFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("W First W Last", "Respondent 1 Witness", details);
    }

    private Flags getRespondent1ExpertsFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("E First E Last", "Respondent 1 Expert", details);
    }

    private Flags getFlagsForParty(String name, String role, List<Element<FlagDetail>> details) {
        return Flags.builder()
            .partyName(name)
            .roleOnCase(role)
            .details(details)
            .build();
    }

    private List<Element<FlagDetail>> getAllFlagDetails() {
        FlagDetail details1 = FlagDetail.builder()
            .name("Vulnerable user")
            .flagComment("comment")
            .flagCode("AB001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        FlagDetail details2 = FlagDetail.builder()
            .name("Flight risk")
            .flagComment("comment")
            .flagCode("SM001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        FlagDetail details3 = FlagDetail.builder()
            .name("Audio/Video evidence")
            .flagComment("comment")
            .flagCode("RA001")
            .hearingRelevant(NO)
            .status("Active")
            .build();

        FlagDetail details4 = FlagDetail.builder()
            .name("Other")
            .flagComment("comment")
            .flagCode("AB001")
            .hearingRelevant(YES)
            .status("Inactive")
            .build();

        return wrapElements(details1, details2, details3, details4);
    }

    private List<Element<FlagDetail>> getAllActiveFlagDetails() {
        FlagDetail details1 = FlagDetail.builder()
            .name("Vulnerable user")
            .flagComment("comment")
            .flagCode("AB001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        FlagDetail details2 = FlagDetail.builder()
            .name("Flight risk")
            .flagComment("comment")
            .flagCode("SM001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        FlagDetail details3 = FlagDetail.builder()
            .name("Audio/Video evidence")
            .flagComment("comment")
            .flagCode("RA001")
            .hearingRelevant(NO)
            .status("Active")
            .build();

        return wrapElements(details1, details2, details3);
    }

    private List<Element<FlagDetail>> getAllActiveHearingRelevantFlagDetails() {
        FlagDetail details1 = FlagDetail.builder()
            .name("Vulnerable user")
            .flagComment("comment")
            .flagCode("AB001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        FlagDetail details2 = FlagDetail.builder()
            .name("Flight risk")
            .flagComment("comment")
            .flagCode("SM001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        return wrapElements(details1, details2);
    }

    private List<Element<FlagDetail>> getAllActiveSMCodeFlagDetails() {
        FlagDetail details2 = FlagDetail.builder()
            .name("Flight risk")
            .flagComment("comment")
            .flagCode("SM001")
            .hearingRelevant(YES)
            .status("Active")
            .build();

        return wrapElements(details2);
    }

    private List<Element<FlagDetail>> getAllActiveRACodeFlagDetails() {
        FlagDetail details3 = FlagDetail.builder()
            .name("Audio/Video evidence")
            .flagComment("comment")
            .flagCode("RA001")
            .hearingRelevant(NO)
            .status("Active")
            .build();

        return wrapElements(details3);
    }
}
