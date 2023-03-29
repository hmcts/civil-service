package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseFlagsHearingsUtils {

    private CaseFlagsHearingsUtils() {
        //NO-OP
    }

    private static final String SPECIAL_MEASURES_FLAG_CODE = "SM";
    private static final String REASONABLE_ADJUSTMENTS_FLAG_CODE = "RA";

    public static List<Flags> getAllActiveFlags(CaseData caseData) {
        List<Flags> nonEmptyFlags = new ArrayList<>();

        getNonEmptyPartyFlags(caseData, nonEmptyFlags);

        getNonEmptyLitigationFriendFlags(caseData, nonEmptyFlags);

        getNonEmptyExpertAndWitnessFlags(caseData, nonEmptyFlags);

        findActiveFlags(nonEmptyFlags);

        return nonEmptyFlags;
    }

    private static void getNonEmptyExpertAndWitnessFlags(CaseData caseData, List<Flags> nonEmptyFlags) {
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

    private static void getNonEmptyLitigationFriendFlags(CaseData caseData, List<Flags> nonEmptyFlags) {
        if (caseData.getRespondent1LitigationFriend() != null) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getRespondent1LitigationFriend().getFlags());
        }
        if (caseData.getRespondent2LitigationFriend() != null) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getRespondent2LitigationFriend().getFlags());
        }

        if (caseData.getApplicant1LitigationFriend() != null) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getApplicant1LitigationFriend().getFlags());
        }
        if (caseData.getApplicant2LitigationFriend() != null) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getApplicant2LitigationFriend().getFlags());
        }
    }

    private static void getNonEmptyPartyFlags(CaseData caseData, List<Flags> nonEmptyFlags) {
        findNonEmptyFlags(nonEmptyFlags, caseData.getRespondent1().getFlags());
        if (YES.equals(caseData.getAddRespondent2())) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getRespondent2().getFlags());
        }
        findNonEmptyFlags(nonEmptyFlags, caseData.getApplicant1().getFlags());
        if (YES.equals(caseData.getAddApplicant2())) {
            findNonEmptyFlags(nonEmptyFlags, caseData.getApplicant2().getFlags());
        }
    }

    private static void findNonEmptyPartyFlagsForExpertsAndWitnesses(
        List<Element<PartyFlagStructure>> partyFlagStructure,
        List<Flags> nonEmptyFlags) {
        for (Element<PartyFlagStructure> partyFlagStructureElement : partyFlagStructure) {
            findNonEmptyFlags(nonEmptyFlags, partyFlagStructureElement.getValue().getFlags());
        }
    }

    private static void findNonEmptyFlags(List<Flags> nonEmptyFlags, Flags flags) {
        if (flags != null
            && flags.getDetails() != null) {
            nonEmptyFlags.add(flags);
        }
    }

    private static void findActiveFlags(List<Flags> nonEmptyFlags) {
        if (nonEmptyFlags != null && !nonEmptyFlags.isEmpty()) {
            nonEmptyFlags.forEach(f -> f.getDetails().removeIf(d -> !d.getValue().getStatus().equals("Active")));
        }
    }

    public static List<Flags> getAllHearingRelevantCaseFlags(List<Flags> flags) {
        if (flags != null && !flags.isEmpty()) {
            flags.forEach(f -> f.getDetails().removeIf(d -> !YES.equals(d.getValue().getHearingRelevant())));
        }
        return flags;
    }

    public static List<Flags> getSMCodeFlags(List<Flags> flags) {
        if (flags != null && !flags.isEmpty()) {
            flags.forEach(f -> f.getDetails().removeIf(d -> !d.getValue().getFlagCode().contains(
                SPECIAL_MEASURES_FLAG_CODE)));
        }
        return flags;
    }

    public static List<Flags> getRACodeFlags(List<Flags> flags) {
        if (flags != null && !flags.isEmpty()) {
            flags.forEach(f -> f.getDetails().removeIf(d -> !d.getValue().getFlagCode().contains(
                REASONABLE_ADJUSTMENTS_FLAG_CODE)));
        }
        return flags;
    }
}
