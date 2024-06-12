package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.caseflags.PartyFlags;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;
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
            .withApplicant1LRIndividualFlags()
            .withRespondent1LRIndividualFlags()
            .withRespondent2LRIndividualFlags()
            .withApplicant1OrgIndividualFlags()
            .withApplicant2OrgIndividualFlags()
            .withRespondent1OrgIndividualFlags()
            .withRespondent2OrgIndividualFlags()
            .build();

        List<Flags> expectedFlags = new ArrayList<>();

        expectedFlags.add(getRespondent1Flags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getApplicant1Flags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent1LitFriendFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent1ExpertsFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent1WitnessFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getApplicant1OrgIndividualFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getApplicant2OrgIndividualFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent1OrgIndividualFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent2OrgIndividualFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getApplicant1LRIndividualFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent1LRIndividualFlags(caseData, getAllActiveFlagDetails()));
        expectedFlags.add(getRespondent2LRIndividualFlags(caseData, getAllActiveFlagDetails()));

        List<PartyFlags> actualFlags = CaseFlagsHearingsUtils.getAllActiveFlags(caseData);

        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    @SuppressWarnings("checkstyle:CommentsIndentation")
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
            .withApplicant1LRIndividualFlags()
            .withRespondent1LRIndividualFlags()
            .withRespondent2LRIndividualFlags()
            .withApplicant1OrgIndividualFlags()
            .withApplicant2OrgIndividualFlags()
            .withRespondent1OrgIndividualFlags()
            .withRespondent2OrgIndividualFlags()
            .build();

        List<Flags> expectedFlags = new ArrayList<>();

        expectedFlags.add(getRespondent1Flags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getApplicant1Flags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent1LitFriendFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent1ExpertsFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent1WitnessFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getApplicant1OrgIndividualFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getApplicant2OrgIndividualFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent1OrgIndividualFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent2OrgIndividualFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getApplicant1LRIndividualFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent1LRIndividualFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        expectedFlags.add(getRespondent2LRIndividualFlags(caseData, getAllActiveHearingRelevantFlagDetails()));
        List<PartyFlags> activeFlags = CaseFlagsHearingsUtils.getAllActiveFlags(caseData);
        List<PartyFlags> actualFlags = CaseFlagsHearingsUtils.getAllHearingRelevantCaseFlags(activeFlags);

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
            .withApplicant1OrgIndividualFlags()
            .withApplicant2OrgIndividualFlags()
            .withRespondent1OrgIndividualFlags()
            .withRespondent2OrgIndividualFlags()
            .withApplicant1LRIndividualFlags()
            .withRespondent1LRIndividualFlags()
            .withRespondent2LRIndividualFlags()
            .build();

        List<Flags> expectedFlags = new ArrayList<>();

        expectedFlags.add(getRespondent1Flags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getApplicant1Flags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent1LitFriendFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent1ExpertsFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent1WitnessFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getApplicant1OrgIndividualFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getApplicant2OrgIndividualFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent1OrgIndividualFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent2OrgIndividualFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getApplicant1LRIndividualFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent1LRIndividualFlags(caseData, getAllActiveSMCodeFlagDetails()));
        expectedFlags.add(getRespondent2LRIndividualFlags(caseData, getAllActiveSMCodeFlagDetails()));

        List<PartyFlags> activeFlags = CaseFlagsHearingsUtils.getAllActiveFlags(caseData);
        List<PartyFlags> actualFlags = CaseFlagsHearingsUtils.getSMCodeFlags(activeFlags);

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
            .withApplicant1OrgIndividualFlags()
            .withApplicant2OrgIndividualFlags()
            .withRespondent1OrgIndividualFlags()
            .withRespondent2OrgIndividualFlags()
            .withApplicant1LRIndividualFlags()
            .withRespondent1LRIndividualFlags()
            .withRespondent2LRIndividualFlags()
            .build();

        List<PartyFlags> expectedFlags = new ArrayList<>();

        expectedFlags.add(getRespondent1Flags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getApplicant1Flags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent1LitFriendFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent1ExpertsFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent1WitnessFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getApplicant1OrgIndividualFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getApplicant2OrgIndividualFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent1OrgIndividualFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent2OrgIndividualFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getApplicant1LRIndividualFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent1LRIndividualFlags(caseData, getAllActiveRACodeFlagDetails()));
        expectedFlags.add(getRespondent2LRIndividualFlags(caseData, getAllActiveRACodeFlagDetails()));

        List<PartyFlags> activeFlags = CaseFlagsHearingsUtils.getAllActiveFlags(caseData);
        List<PartyFlags> actualFlags = CaseFlagsHearingsUtils.getRACodeFlags(activeFlags);

        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    @Nested
    class DetainedIndividualFlags {
        List<Element<FlagDetail>> flags;

        @BeforeEach
        void setup() {
            FlagDetail details = FlagDetail.builder()
                .name("Detained individual")
                .flagComment("comment")
                .flagCode("PF0019")
                .hearingRelevant(YES)
                .status("Active")
                .build();

            flags = wrapElements(details);
        }

        @Test
        void shouldReturnTrue_whenDetainedIndividualFlagExistsInAllUsers() {
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .atStateRespondentFullDefence()
                .withRespondent1Flags(flags)
                .withApplicant1Flags(flags)
                .withRespondent1LitigationFriendFlags(flags)
                .withRespondent1WitnessFlags()
                .withRespondent1ExpertFlags()
                .build();

            assertThat(CaseFlagsHearingsUtils.detainedIndividualFlagExist(caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenDetainedIndividualFlagExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .atStateRespondentFullDefence()
                .withRespondent1Flags()
                .withApplicant1Flags()
                .withRespondent1LitigationFriendFlags(flags)
                .withRespondent1WitnessFlags()
                .withRespondent1ExpertFlags()
                .build();

            assertThat(CaseFlagsHearingsUtils.detainedIndividualFlagExist(caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenDetainedIndividualFlagDoesNotExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .atStateRespondentFullDefence()
                .withRespondent1Flags()
                .withApplicant1Flags()
                .withRespondent1LitigationFriendFlags()
                .withRespondent1WitnessFlags()
                .withRespondent1ExpertFlags()
                .build();

            assertThat(CaseFlagsHearingsUtils.detainedIndividualFlagExist(caseData)).isFalse();
        }
    }

    @Nested
    class CaseLevelFlags {

        @Test
        void shouldReturnAllActiveCaseLevelFlags() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .withCaseLevelFlags()
                .build();

            List<FlagDetail> actual = CaseFlagsHearingsUtils.getAllActiveCaseLevelFlags(caseData);

            assertThat(actual.size()).isEqualTo(1);
            assertThat(actual).containsOnly(FlagDetail.builder()
                                                .flagCode("123")
                                                .status("Active")
                                                .build());
        }

        @Test
        void shouldReturnEmptyList_whenNoActiveCaseLevelFlags() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();

            List<FlagDetail> actual = CaseFlagsHearingsUtils.getAllActiveCaseLevelFlags(caseData);

            assertThat(actual.size()).isEqualTo(0);
        }
    }

    private PartyFlags getRespondent1Flags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty(caseData.getRespondent1().getPartyName(), "Defendant 1", details, caseData.getRespondent1().getPartyID());
    }

    private PartyFlags getApplicant1Flags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty(caseData.getApplicant1().getPartyName(), "Claimant 1", details, caseData.getApplicant1().getPartyID());
    }

    private PartyFlags getRespondent1LitFriendFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty(caseData.getRespondent1LitigationFriend().getFullName(),
                                "Defendant 1 Litigation Friend", details, caseData.getRespondent1LitigationFriend().getPartyID());
    }

    private PartyFlags getRespondent1WitnessFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("W First W Last", "Defendant 1 Witness", details,
                                "res-1-witness-party-id");
    }

    private PartyFlags getRespondent1ExpertsFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("E First E Last", "Defendant 1 Expert", details,
                                "res-1-expert-party-id");
    }

    private PartyFlags getApplicant1OrgIndividualFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("First Last", "App 1 Org Individual", details,
                                "app-1-org-individual-party-id");
    }

    private PartyFlags getApplicant2OrgIndividualFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("First Last", "App 2 Org Individual", details,
                                "app-2-org-individual-party-id");
    }

    private PartyFlags getRespondent1OrgIndividualFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("First Last", "Res 1 Org Individual", details,
                                "res-1-org-individual-party-id");
    }

    private PartyFlags getRespondent2OrgIndividualFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("First Last", "Res 2 Org Individual", details,
                                "res-2-org-individual-party-id");
    }

    private PartyFlags getApplicant1LRIndividualFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("First Last", "App 1 Lr Individual", details,
                                "app-1-lr-individual-party-id");
    }

    private PartyFlags getRespondent1LRIndividualFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("First Last", "Res 1 Lr Individual", details,
                                "res-1-lr-individual-party-id");
    }

    private PartyFlags getRespondent2LRIndividualFlags(CaseData caseData, List<Element<FlagDetail>> details) {
        return getFlagsForParty("First Last", "Res 2 Lr Individual", details,
                                "res-2-lr-individual-party-id");
    }

    private PartyFlags getFlagsForParty(String name, String role, List<Element<FlagDetail>> details, String partyId) {
        return PartyFlags.builder()
            .partyId(partyId)
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
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
            .build();

        FlagDetail details2 = FlagDetail.builder()
            .name("Flight risk")
            .flagComment("comment")
            .flagCode("SM001")
            .hearingRelevant(YES)
            .status("Active")
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
            .build();

        FlagDetail details3 = FlagDetail.builder()
            .name("Audio/Video evidence")
            .flagComment("comment")
            .flagCode("RA001")
            .hearingRelevant(NO)
            .status("Active")
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
            .build();

        FlagDetail details4 = FlagDetail.builder()
            .name("Other")
            .flagComment("comment")
            .flagCode("AB001")
            .hearingRelevant(YES)
            .status("Inactive")
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
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
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
            .build();

        FlagDetail details2 = FlagDetail.builder()
            .name("Flight risk")
            .flagComment("comment")
            .flagCode("SM001")
            .hearingRelevant(YES)
            .status("Active")
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
            .build();

        FlagDetail details3 = FlagDetail.builder()
            .name("Audio/Video evidence")
            .flagComment("comment")
            .flagCode("RA001")
            .hearingRelevant(NO)
            .status("Active")
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
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
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
            .build();

        FlagDetail details2 = FlagDetail.builder()
            .name("Flight risk")
            .flagComment("comment")
            .flagCode("SM001")
            .hearingRelevant(YES)
            .status("Active")
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
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
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
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
            .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
            .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
            .build();

        return wrapElements(details3);
    }
}
