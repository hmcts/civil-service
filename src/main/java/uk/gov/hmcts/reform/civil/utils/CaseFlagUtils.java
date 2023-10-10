package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Witness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;

public class CaseFlagUtils {

    public static String RESPONDENT_SOLICITOR_ONE_WITNESS = "Respondent solicitor 1 witness";
    public static String RESPONDENT_SOLICITOR_ONE_EXPERT = "Respondent solicitor 1 expert";
    public static String RESPONDENT_SOLICITOR_TWO_WITNESS = "Respondent solicitor 2 witness";
    public static String RESPONDENT_SOLICITOR_TWO_EXPERT = "Respondent solicitor 2 expert";
    public static String APPLICANT_SOLICITOR_WITNESS = "Applicant solicitor witness";
    public static String APPLICANT_SOLICITOR_EXPERT = "Applicant solicitor expert";

    private CaseFlagUtils() {
        //NO-OP
    }

    public static Flags createFlags(String flagsPartyName, String roleOnCase) {
        return Flags.builder()
            .partyName(flagsPartyName)
            .roleOnCase(roleOnCase)
            .details(List.of())
            .build();
    }

    private static PartyFlagStructure createPartiesCaseFlagsField(String partyId, String firstName, String lastName,
                                                                  String email, String phone, String roleOnCase) {
        String partyName = String.format("%s %s", firstName, lastName);
        return PartyFlagStructure.builder()
            .partyID(partyId)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .phone(phone)
            .flags(createFlags(partyName, roleOnCase))
            .build();
    }

    public static Party updateParty(String roleOnCase, Party partyToUpdate) {
        return partyToUpdate != null ? partyToUpdate.getFlags() != null ? partyToUpdate :
            partyToUpdate.toBuilder().flags(createFlags(partyToUpdate.getPartyName(), roleOnCase)).build() : null;
    }

    public static LitigationFriend updateLitFriend(String roleOnCase, LitigationFriend litFriendToUpdate) {
        return litFriendToUpdate != null ? litFriendToUpdate.getFlags() != null ? litFriendToUpdate
            : litFriendToUpdate.toBuilder().flags(createFlags(
            // LitigationFriend was updated to split fullName into firstname and lastname for H&L ==================
            // ToDo: Remove the use of fullName after H&L changes are default =====================================
            litFriendToUpdate.getFullName() != null ? litFriendToUpdate.getFullName()
                // ====================================================================================================
                : String.format("%s %s", litFriendToUpdate.getFirstName(), litFriendToUpdate.getLastName()),
            roleOnCase)).build() : null;
    }

    private static List<Element<PartyFlagStructure>> getTopLevelFieldForWitnessesWithFlagsStructure(
        List<Witness> witnessList,
        String roleOnCase) {
        List<Element<PartyFlagStructure>> list = new ArrayList<>();
        for (Witness witness : witnessList) {
            PartyFlagStructure build = createPartiesCaseFlagsField(witness.getPartyID(), witness.getFirstName(), witness.getLastName(),
                                                                   witness.getEmailAddress(), witness.getPhoneNumber(),
                                                                   roleOnCase);
            list.add(element(build));
        }
        return list;
    }

    private static List<Element<PartyFlagStructure>> getTopLevelFieldForExpertsWithFlagsStructure(
        List<Expert> expertList,
        String roleOnCase) {
        List<Element<PartyFlagStructure>> list = new ArrayList<>();
        for (Expert expert : expertList) {
            PartyFlagStructure build = createPartiesCaseFlagsField(expert.getPartyID(), expert.getFirstName(), expert.getLastName(),
                                                                   expert.getEmailAddress(), expert.getPhoneNumber(),
                                                                   roleOnCase);
            list.add(element(build));
        }
        return list;
    }

    public static void addRespondentDQPartiesFlagStructure(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {
        addRespondent1ExpertAndWitnessFlagsStructure(builder, caseData);
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData)) || (
            ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && NO.equals(caseData.getRespondentResponseIsSame())
            && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()))) {
            addRespondent2ExpertAndWitnessFlagsStructure(builder, caseData);
        }
    }

    private static void addRespondent2ExpertAndWitnessFlagsStructure(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {
        if (caseData.getRespondent2DQ() != null) {
            List<Witness> respondent2Witnesses = new ArrayList<>();
            if (caseData.getRespondent2DQ().getWitnesses() != null) {
                respondent2Witnesses = unwrapElements(caseData.getRespondent2DQ().getWitnesses().getDetails());
            }

            List<Expert> respondent2Experts = new ArrayList<>();
            if (caseData.getRespondent2DQ().getExperts() != null) {
                respondent2Experts = unwrapElements(caseData.getRespondent2DQ().getExperts().getDetails());
            }

            builder.respondent2Witnesses(getTopLevelFieldForWitnessesWithFlagsStructure(respondent2Witnesses, RESPONDENT_SOLICITOR_TWO_WITNESS));
            builder.respondent2Experts(getTopLevelFieldForExpertsWithFlagsStructure(respondent2Experts, RESPONDENT_SOLICITOR_TWO_EXPERT));
        }
    }

    private static void addRespondent1ExpertAndWitnessFlagsStructure(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {
        if (caseData.getRespondent1DQ() != null) {
            List<Witness> respondent1Witnesses = new ArrayList<>();
            if (caseData.getRespondent1DQ().getWitnesses() != null) {
                respondent1Witnesses = unwrapElements(caseData.getRespondent1DQ().getWitnesses().getDetails());
            }

            List<Expert> respondent1Experts = new ArrayList<>();
            if (caseData.getRespondent1DQ().getExperts() != null) {
                respondent1Experts = unwrapElements(caseData.getRespondent1DQ().getExperts().getDetails());
            }
            builder.respondent1Witnesses(getTopLevelFieldForWitnessesWithFlagsStructure(respondent1Witnesses, RESPONDENT_SOLICITOR_ONE_WITNESS));
            builder.respondent1Experts(getTopLevelFieldForExpertsWithFlagsStructure(respondent1Experts, RESPONDENT_SOLICITOR_ONE_EXPERT));
        }
    }

    public static void addApplicantExpertAndWitnessFlagsStructure(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        if (caseData.getApplicant1DQ() != null) {
            List<Witness> applicant1Witnesses = new ArrayList<>();
            if (caseData.getApplicant1DQ().getWitnesses() != null) {
                applicant1Witnesses.addAll(unwrapElements(caseData.getApplicant1DQ().getWitnesses().getDetails()));
            }

            List<Expert> applicant1Experts = new ArrayList<>();
            if (caseData.getApplicant1DQ().getExperts() != null) {
                applicant1Experts.addAll(unwrapElements(caseData.getApplicant1DQ().getExperts().getDetails()));
            }

            if (getMultiPartyScenario(caseData) == TWO_V_ONE && caseData.getApplicant2DQ() != null) {
                if (caseData.getApplicant2DQ().getWitnesses() != null) {
                    applicant1Witnesses.addAll(unwrapElements(caseData.getApplicant2DQ().getWitnesses().getDetails()));
                }
                if (caseData.getApplicant2DQ().getExperts() != null) {
                    applicant1Experts.addAll(unwrapElements(caseData.getApplicant2DQ().getExperts().getDetails()));
                }
            }

            updatedData.applicantWitnesses(getTopLevelFieldForWitnessesWithFlagsStructure(applicant1Witnesses, APPLICANT_SOLICITOR_WITNESS));
            updatedData.applicantExperts(getTopLevelFieldForExpertsWithFlagsStructure(applicant1Experts, APPLICANT_SOLICITOR_EXPERT));
        }
    }

    public static List<FlagDetail> getAllCaseFlags(CaseData caseData) {
        var flagCollection = new ArrayList<FlagDetail>();
        flagCollection.addAll(getFlagDetails(caseData.getCaseFlags()));
        flagCollection.addAll(getFlagDetails(caseData.getApplicant1()));
        flagCollection.addAll(getFlagDetails(caseData.getApplicant2()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent1()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent2()));
        flagCollection.addAll(getFlagDetails(caseData.getApplicant1LitigationFriend()));
        flagCollection.addAll(getFlagDetails(caseData.getApplicant2LitigationFriend()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent1LitigationFriend()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent2LitigationFriend()));
        flagCollection.addAll(getFlagDetails(caseData.getApplicantExperts()));
        flagCollection.addAll(getFlagDetails(caseData.getApplicantWitnesses()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent1Experts()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent1Witnesses()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent2Experts()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent2Witnesses()));
        return flagCollection.stream().filter(flags -> flags != null).collect(Collectors.toList());
    }

    public static List<FlagDetail> getFlagDetails(Flags flags) {
        return flags != null && flags.getDetails() != null
            ? flags.getDetails().stream().map(Element::getValue).collect(Collectors.toList()) : Collections.emptyList();
    }

    public static List<FlagDetail> getFlagDetails(Party party) {
        return party != null ? getFlagDetails(party.getFlags()) : Collections.emptyList();
    }

    public static List<FlagDetail> getFlagDetails(LitigationFriend litigationFriend) {
        return litigationFriend != null ? getFlagDetails(litigationFriend.getFlags()) : Collections.emptyList();
    }

    public static List<FlagDetail> getFlagDetails(PartyFlagStructure partyStructure) {
        return partyStructure != null ? getFlagDetails(partyStructure.getFlags()) : Collections.emptyList();
    }

    public static List<FlagDetail> getFlagDetails(List<Element<PartyFlagStructure>> partyStructures) {
        return partyStructures != null
            ? partyStructures.stream()
                .map(party -> getFlagDetails(party.getValue().getFlags()))
                .flatMap(List::stream)
                .collect(Collectors.toList()) : Collections.emptyList();
    }

    @SafeVarargs
    public static List<FlagDetail> filter(List<FlagDetail> flagDetails, Predicate<FlagDetail>... predicates) {
        return flagDetails.stream()
            .filter(List.of(predicates).stream().reduce(Predicate::and).orElse(x -> true))
            .collect(Collectors.toList());
    }
}
