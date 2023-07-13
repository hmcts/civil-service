package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.caseflags.PartyFlags;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseFlagsHearingsUtils {

    private CaseFlagsHearingsUtils() {
        //NO-OP
    }

    private static final String SPECIAL_MEASURES_FLAG_CODE = "SM";
    private static final String REASONABLE_ADJUSTMENTS_FLAG_CODE = "RA";
    private static final String DETAINED_INDIVIDUAL_FLAG_CODE = "PF0019";

    public static List<PartyFlags> getAllActiveFlags(CaseData caseData) {
        List<PartyFlags> nonEmptyFlags = new ArrayList<>();

        getNonEmptyPartyFlags(caseData, nonEmptyFlags);

        getNonEmptyLitigationFriendFlags(caseData, nonEmptyFlags);

        getNonEmptyExpertAndWitnessFlags(caseData, nonEmptyFlags);

        findActiveFlags(nonEmptyFlags);

        return nonEmptyFlags;
    }

    private static void getNonEmptyExpertAndWitnessFlags(CaseData caseData, List<PartyFlags> nonEmptyFlags) {
        if (caseData.getApplicantExperts() != null) {
            findNonEmptyPartyFlagsForExpertsAndWitnesses(caseData.getApplicantExperts(), nonEmptyFlags);
        }

        if (caseData.getApplicantWitnesses() != null) {
            findNonEmptyPartyFlagsForExpertsAndWitnesses(caseData.getApplicantWitnesses(), nonEmptyFlags);
        }

        if (caseData.getRespondent1Experts() != null) {
            findNonEmptyPartyFlagsForExpertsAndWitnesses(caseData.getRespondent1Experts(), nonEmptyFlags);
        }

        if (caseData.getRespondent1Witnesses() != null) {
            findNonEmptyPartyFlagsForExpertsAndWitnesses(caseData.getRespondent1Witnesses(), nonEmptyFlags);
        }

        if (caseData.getRespondent2Experts() != null) {
            findNonEmptyPartyFlagsForExpertsAndWitnesses(caseData.getRespondent2Experts(), nonEmptyFlags);
        }

        if (caseData.getRespondent2Witnesses() != null) {
            findNonEmptyPartyFlagsForExpertsAndWitnesses(caseData.getRespondent2Witnesses(), nonEmptyFlags);
        }
    }

    private static void getNonEmptyLitigationFriendFlags(CaseData caseData, List<PartyFlags> nonEmptyFlags) {
        if (caseData.getRespondent1LitigationFriend() != null) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getRespondent1LitigationFriend().getFlags(), caseData.getRespondent1LitigationFriend().getPartyID());
        }
        if (caseData.getRespondent2LitigationFriend() != null) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getRespondent2LitigationFriend().getFlags(), caseData.getRespondent2LitigationFriend().getPartyID());
        }

        if (caseData.getApplicant1LitigationFriend() != null) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getApplicant1LitigationFriend().getFlags(), caseData.getApplicant1LitigationFriend().getPartyID());
        }
        if (caseData.getApplicant2LitigationFriend() != null) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getApplicant2LitigationFriend().getFlags(), caseData.getApplicant2LitigationFriend().getPartyID());
        }
    }

    private static void getNonEmptyPartyFlags(CaseData caseData, List<PartyFlags> nonEmptyFlags) {
        findNonEmptyFlags(nonEmptyFlags, caseData.getRespondent1().getFlags(), caseData.getRespondent1().getPartyID());
        if (YES.equals(caseData.getAddRespondent2())) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getRespondent2().getFlags(), caseData.getRespondent2().getPartyID());
        }
        findNonEmptyFlags(nonEmptyFlags, caseData.getApplicant1().getFlags(), caseData.getApplicant1().getPartyID());
        if (YES.equals(caseData.getAddApplicant2())) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getApplicant2().getFlags(),  caseData.getApplicant2().getPartyID());
        }
    }

    private static void findNonEmptyPartyFlagsForExpertsAndWitnesses(
        List<Element<PartyFlagStructure>> partyFlagStructure,
        List<PartyFlags> nonEmptyFlags) {
        for (Element<PartyFlagStructure> partyFlagStructureElement : partyFlagStructure) {
            findNonEmptyFlags(nonEmptyFlags, partyFlagStructureElement.getValue().getFlags(), partyFlagStructureElement.getValue().getPartyID());
        }
    }

    private static void findNonEmptyFlags(List<PartyFlags> nonEmptyFlags, Flags flags, String partyId) {
        if (flags != null
            && flags.getDetails() != null) {
            nonEmptyFlags.add(PartyFlags.from(flags).toBuilder().partyId(partyId).build());
        }
    }

    private static void findActiveFlags(List<PartyFlags> nonEmptyFlags) {
        if (nonEmptyFlags != null && !nonEmptyFlags.isEmpty()) {
            nonEmptyFlags.forEach(f -> f.getDetails().removeIf(d -> !d.getValue().getStatus().equals("Active")));
        }
    }

    public static List<PartyFlags> getAllHearingRelevantCaseFlags(List<PartyFlags> flags) {
        if (flags != null && !flags.isEmpty()) {
            flags.forEach(f -> f.getDetails().removeIf(d -> !YES.equals(d.getValue().getHearingRelevant())));
        }
        return flags;
    }

    public static List<PartyFlags> getSMCodeFlags(List<PartyFlags> flags) {
        if (flags != null && !flags.isEmpty()) {
            flags.forEach(f -> f.getDetails().removeIf(d -> !d.getValue().getFlagCode().contains(
                SPECIAL_MEASURES_FLAG_CODE)));
        }
        return flags;
    }

    public static List<PartyFlags> getRACodeFlags(List<PartyFlags> flags) {
        if (flags != null && !flags.isEmpty()) {
            flags.forEach(f -> f.getDetails().removeIf(d -> !d.getValue().getFlagCode().contains(
                REASONABLE_ADJUSTMENTS_FLAG_CODE)));
        }
        return flags;
    }

    public static boolean detainedIndividualFlagExist(CaseData caseData) {
        List<PartyFlags> flags = getAllActiveFlags(caseData);
        return flags.stream()
            .flatMap(flag -> flag.getDetails().stream())
            .anyMatch(detail -> detail.getValue().getFlagCode().equals(DETAINED_INDIVIDUAL_FLAG_CODE));
    }
}
