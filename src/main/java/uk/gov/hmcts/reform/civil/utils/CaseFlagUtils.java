package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.appendWithNewPartyId;

@Slf4j
public class CaseFlagUtils {

    public static final String RESPONDENT_SOLICITOR_ONE_WITNESS = "Defendant solicitor 1 witness";
    public static final String RESPONDENT_SOLICITOR_ONE_EXPERT = "Defendant solicitor 1 expert";
    public static final String RESPONDENT_SOLICITOR_TWO_WITNESS = "Defendant solicitor 2 witness";
    public static final String RESPONDENT_SOLICITOR_TWO_EXPERT = "Defendant solicitor 2 expert";
    public static final String APPLICANT_SOLICITOR_WITNESS = "Claimant solicitor witness";
    public static final String APPLICANT_SOLICITOR_EXPERT = "Claimant solicitor expert";
    public static final String APPLICANT_ONE = "Claimant 1";
    public static final String APPLICANT_TWO = "Claimant 2";
    public static final String APPLICANT_ONE_LITIGATION_FRIEND = "Claimant 1 Litigation Friend";
    public static final String APPLICANT_TWO_LITIGATION_FRIEND = "Claimant 2 Litigation Friend";
    public static final String RESPONDENT_ONE = "Defendant 1";
    public static final String RESPONDENT_TWO = "Defendant 2";
    public static final String RESPONDENT_ONE_LITIGATION_FRIEND = "Defendant 1 Litigation Friend";
    public static final String RESPONDENT_TWO_LITIGATION_FRIEND = "Defendant 2 Litigation Friend";

    private CaseFlagUtils() {
        //NO-OP
    }

    public static Flags createFlags(String flagsPartyName, String roleOnCase) {
        return new Flags()
            .setPartyName(flagsPartyName)
            .setRoleOnCase(roleOnCase)
            .setDetails(List.of());
    }

    private static PartyFlagStructure createPartiesCaseFlagsField(String partyId, String firstName, String lastName,
                                                                  String email, String phone, String roleOnCase) {
        String partyName = formattedPartyNameForFlags(firstName, lastName);
        return new PartyFlagStructure()
            .setPartyID(partyId)
            .setFirstName(firstName)
            .setLastName(lastName)
            .setEmail(email)
            .setPhone(phone)
            .setFlags(createFlags(partyName, roleOnCase))
            ;
    }

    public static Party updateParty(String roleOnCase, Party partyToUpdate) {
        return partyToUpdate != null ? partyToUpdate.getFlags() != null ? partyToUpdate :
            partyToUpdate.toBuilder().flags(createFlags(partyToUpdate.getPartyName(), roleOnCase)).build() : null;
    }

    public static LitigationFriend updateLitFriend(String roleOnCase, LitigationFriend litFriendToUpdate) {
        return litFriendToUpdate != null ? litFriendToUpdate.getFlags() != null ? litFriendToUpdate
            : litFriendToUpdate.copy().setFlags(createFlags(
            // LitigationFriend was updated to split fullName into firstname and lastname for H&L ==================
            // ToDo: Remove the use of fullName after H&L changes are default =====================================
            litFriendToUpdate.getFullName() != null ? litFriendToUpdate.getFullName()
                // ====================================================================================================
                : formattedPartyNameForFlags(litFriendToUpdate.getFirstName(), litFriendToUpdate.getLastName()),
            roleOnCase)) : null;
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

    public static void addRespondentDQPartiesFlagStructure(CaseData caseData) {
        addRespondent1ExpertAndWitnessFlagsStructure(caseData);
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData)) || (
            ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && NO.equals(caseData.getRespondentResponseIsSame())
            && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()))) {
            addRespondent2ExpertAndWitnessFlagsStructure(caseData);
        }
    }

    private static void addRespondent2ExpertAndWitnessFlagsStructure(CaseData caseData) {
        if (caseData.getRespondent2DQ() != null) {
            List<Witness> respondent2Witnesses = new ArrayList<>();
            if (caseData.getRespondent2DQ().getWitnesses() != null) {
                respondent2Witnesses = unwrapElements(caseData.getRespondent2DQ().getWitnesses().getDetails());
            }

            List<Expert> respondent2Experts = new ArrayList<>();
            if (caseData.getRespondent2DQ().getExperts() != null) {
                respondent2Experts = unwrapElements(caseData.getRespondent2DQ().getExperts().getDetails());
            }

            caseData.setRespondent2Witnesses(getTopLevelFieldForWitnessesWithFlagsStructure(respondent2Witnesses, RESPONDENT_SOLICITOR_TWO_WITNESS));
            caseData.setRespondent2Experts(getTopLevelFieldForExpertsWithFlagsStructure(respondent2Experts, RESPONDENT_SOLICITOR_TWO_EXPERT));
        }
    }

    private static void addRespondent1ExpertAndWitnessFlagsStructure(CaseData caseData) {
        if (caseData.getRespondent1DQ() != null) {
            List<Witness> respondent1Witnesses = new ArrayList<>();
            if (caseData.getRespondent1DQ().getWitnesses() != null) {
                respondent1Witnesses = unwrapElements(caseData.getRespondent1DQ().getWitnesses().getDetails());
            }

            List<Expert> respondent1Experts = new ArrayList<>();
            if (caseData.getRespondent1DQ().getExperts() != null) {
                respondent1Experts = unwrapElements(caseData.getRespondent1DQ().getExperts().getDetails());
            }
            caseData.setRespondent1Witnesses(getTopLevelFieldForWitnessesWithFlagsStructure(respondent1Witnesses, RESPONDENT_SOLICITOR_ONE_WITNESS));
            caseData.setRespondent1Experts(getTopLevelFieldForExpertsWithFlagsStructure(respondent1Experts, RESPONDENT_SOLICITOR_ONE_EXPERT));
        }
    }

    public static void addApplicantExpertAndWitnessFlagsStructure(CaseData caseData) {
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

            caseData.setApplicantWitnesses(getTopLevelFieldForWitnessesWithFlagsStructure(applicant1Witnesses, APPLICANT_SOLICITOR_WITNESS));
            caseData.setApplicantExperts(getTopLevelFieldForExpertsWithFlagsStructure(applicant1Experts, APPLICANT_SOLICITOR_EXPERT));
        }
    }

    public static void createOrUpdateFlags(CaseData caseData, OrganisationService organisationService) {
        String partyChosen = caseData.getUpdateDetailsForm().getPartyChosenId();
        // claimant/defendant
        updatePartyFlags(caseData, partyChosen);
        // litigation friend
        updateLitigationFriendFlags(caseData, partyChosen);
        // attending for org/company
        updateOrgIndividualsFlags(caseData, partyChosen);
        // attending for legal rep
        updateLRIndividualsFlags(caseData, partyChosen, organisationService);
        // experts
        updateExpertFlags(caseData, partyChosen);
        // witnesses
        updateWitnessFlags(caseData, partyChosen);
    }

    private static void updateLRIndividualsFlags(CaseData caseData, String partyChosen, OrganisationService organisationService) {
        if ((CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID).equals(partyChosen)) {
            String legalRepFirmName = getLegalRepFirmName(
                caseData.getApplicant1OrganisationPolicy(),
                organisationService,
                APPLICANT_ONE);
            caseData.setApplicant1LRIndividuals(updatePartyNameForPartyFlagStructures(caseData.getApplicant1LRIndividuals(), legalRepFirmName));
        }
        if ((DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID).equals(partyChosen)) {
            String legalRepFirmName = getLegalRepFirmName(
                caseData.getRespondent1OrganisationPolicy(),
                organisationService,
                RESPONDENT_ONE);
            caseData.setRespondent1LRIndividuals(updatePartyNameForPartyFlagStructures(caseData.getRespondent1LRIndividuals(), legalRepFirmName));
        }
        if ((DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID).equals(partyChosen)) {
            String legalRepFirmName = getLegalRepFirmName(
                caseData.getRespondent2OrganisationPolicy(),
                organisationService,
                RESPONDENT_TWO);
            caseData.setRespondent2LRIndividuals(updatePartyNameForPartyFlagStructures(caseData.getRespondent2LRIndividuals(), legalRepFirmName));
        }
    }

    private static void updateOrgIndividualsFlags(CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            caseData.setApplicant1OrgIndividuals(updatePartyNameForPartyFlagStructures(caseData.getApplicant1OrgIndividuals(), caseData.getApplicant1().getPartyName()));
        }
        if ((CLAIMANT_TWO_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            caseData.setApplicant2OrgIndividuals(updatePartyNameForPartyFlagStructures(caseData.getApplicant2OrgIndividuals(), caseData.getApplicant2().getPartyName()));
        }
        if ((DEFENDANT_ONE_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            caseData.setRespondent1OrgIndividuals(updatePartyNameForPartyFlagStructures(caseData.getRespondent1OrgIndividuals(), caseData.getRespondent1().getPartyName()));
        }
        if ((DEFENDANT_TWO_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            caseData.setRespondent2OrgIndividuals(updatePartyNameForPartyFlagStructures(caseData.getRespondent2OrgIndividuals(), caseData.getRespondent2().getPartyName()));
        }
    }

    private static void updateLitigationFriendFlags(CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_LITIGATION_FRIEND_ID).equals(partyChosen)) {
            caseData.setApplicant1LitigationFriend(updatePartyNameForLitigationFriendFlags(caseData.getApplicant1LitigationFriend()));
        }
        if ((CLAIMANT_TWO_LITIGATION_FRIEND_ID).equals(partyChosen)) {
            caseData.setApplicant2LitigationFriend(updatePartyNameForLitigationFriendFlags(caseData.getApplicant2LitigationFriend()));
        }
        if ((DEFENDANT_ONE_LITIGATION_FRIEND_ID).equals(partyChosen)) {
            caseData.setRespondent1LitigationFriend(updatePartyNameForLitigationFriendFlags(caseData.getRespondent1LitigationFriend()));
        }
        if ((DEFENDANT_TWO_LITIGATION_FRIEND_ID).equals(partyChosen)) {
            caseData.setRespondent2LitigationFriend(updatePartyNameForLitigationFriendFlags(caseData.getRespondent2LitigationFriend()));
        }
    }

    private static void updateExpertFlags(CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_EXPERTS_ID).equals(partyChosen)) {
            caseData.setApplicantExperts(updatePartyNameForPartyFlagStructures(caseData.getApplicantExperts(), APPLICANT_SOLICITOR_EXPERT));
        }
        if ((DEFENDANT_ONE_EXPERTS_ID).equals(partyChosen)) {
            caseData.setRespondent1Experts(updatePartyNameForPartyFlagStructures(caseData.getRespondent1Experts(), RESPONDENT_SOLICITOR_ONE_EXPERT));
        }
        if ((DEFENDANT_TWO_EXPERTS_ID).equals(partyChosen)) {
            caseData.setRespondent2Experts(updatePartyNameForPartyFlagStructures(caseData.getRespondent2Experts(), RESPONDENT_SOLICITOR_TWO_EXPERT));
        }
    }

    private static void updateWitnessFlags(CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_WITNESSES_ID).equals(partyChosen)) {
            caseData.setApplicantWitnesses(updatePartyNameForPartyFlagStructures(caseData.getApplicantWitnesses(), APPLICANT_SOLICITOR_WITNESS));
        }
        if ((DEFENDANT_ONE_WITNESSES_ID).equals(partyChosen)) {
            caseData.setRespondent1Witnesses(updatePartyNameForPartyFlagStructures(caseData.getRespondent1Witnesses(), RESPONDENT_SOLICITOR_ONE_WITNESS));
        }
        if ((DEFENDANT_TWO_WITNESSES_ID).equals(partyChosen)) {
            caseData.setRespondent2Witnesses(updatePartyNameForPartyFlagStructures(caseData.getRespondent2Witnesses(), RESPONDENT_SOLICITOR_TWO_WITNESS));
        }
    }

    private static void updatePartyFlags(CaseData caseData, String partyChosen) {
        if (CLAIMANT_ONE_ID.equals(partyChosen) && caseData.getApplicant1().getFlags() != null) {
            caseData.setApplicant1(updatePartyNameForFlags(caseData.getApplicant1()));
        }
        if (CLAIMANT_TWO_ID.equals(partyChosen) && caseData.getApplicant2().getFlags() != null) {
            caseData.setApplicant2(updatePartyNameForFlags(caseData.getApplicant2()));
        }
        if (DEFENDANT_ONE_ID.equals(partyChosen) && caseData.getRespondent1().getFlags() != null) {
            caseData.setRespondent1(updatePartyNameForFlags(caseData.getRespondent1()));
        }
        if (DEFENDANT_TWO_ID.equals(partyChosen) && caseData.getRespondent2().getFlags() != null) {
            caseData.setRespondent2(updatePartyNameForFlags(caseData.getRespondent2()));
        }
    }

    private static List<Element<PartyFlagStructure>> updatePartyNameForPartyFlagStructures(List<Element<PartyFlagStructure>> individuals,
                                                                                           String roleOnCase) {
        if (individuals != null && !individuals.isEmpty()) {
            List<PartyFlagStructure> partyFlagStructures = unwrapElements(individuals);
            List<PartyFlagStructure> updatedList = new ArrayList<>();
            for (PartyFlagStructure partyFlagStructure : partyFlagStructures) {
                String formattedPartyNameForFlags = formattedPartyNameForFlags(partyFlagStructure.getFirstName(), partyFlagStructure.getLastName());
                if (partyFlagStructure.getFlags() == null) {
                    // new party so initialise flags and party ID
                    updatedList.add(
                        appendWithNewPartyId(partyFlagStructure
                                                 .copy()
                                                 .setFlags(createFlags(formattedPartyNameForFlags, roleOnCase))));
                } else {
                    // existing party with flags so just update the name
                    Flags existingFlags = partyFlagStructure.getFlags();
                    Flags updatedFlags = new Flags()
                        .setPartyName(formattedPartyNameForFlags)
                        .setRoleOnCase(existingFlags != null ? existingFlags.getRoleOnCase() : null)
                        .setDetails(existingFlags != null ? existingFlags.getDetails() : null);
                    updatedList.add(
                        partyFlagStructure
                            .copy()
                            .setFlags(updatedFlags));
                }
            }
            return wrapElements(updatedList);
        }
        return null;
    }

    private static Party updatePartyNameForFlags(Party party) {
        Flags existingFlags = party.getFlags();
        Flags updatedFlags = existingFlags != null
            ? new Flags()
                .setPartyName(party.getPartyName())
                .setRoleOnCase(existingFlags.getRoleOnCase())
                .setDetails(existingFlags.getDetails())
            : createFlags(party.getPartyName(), null);
        return party.toBuilder().flags(updatedFlags).build();
    }

    private static LitigationFriend updatePartyNameForLitigationFriendFlags(LitigationFriend litigationFriend) {
        Flags existingFlags = litigationFriend.getFlags();
        String partyName = litigationFriend.getFullName() != null
            ? litigationFriend.getFullName()
            : formattedPartyNameForFlags(litigationFriend.getFirstName(), litigationFriend.getLastName());
        Flags updatedFlags = existingFlags != null
            ? new Flags()
                .setPartyName(partyName)
                .setRoleOnCase(existingFlags.getRoleOnCase())
                .setDetails(existingFlags.getDetails())
            : createFlags(partyName, null);
        return litigationFriend.copy()
            .setFlags(updatedFlags);
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
        flagCollection.addAll(getFlagDetails(caseData.getApplicant1OrgIndividuals()));
        flagCollection.addAll(getFlagDetails(caseData.getApplicant2OrgIndividuals()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent1OrgIndividuals()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent2OrgIndividuals()));
        flagCollection.addAll(getFlagDetails(caseData.getApplicant1LRIndividuals()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent1LRIndividuals()));
        flagCollection.addAll(getFlagDetails(caseData.getRespondent2LRIndividuals()));
        return flagCollection.stream().filter(Objects::nonNull).collect(Collectors.toList());
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

    private static String formattedPartyNameForFlags(String firstName, String lastName) {
        return String.format("%s %s", firstName, lastName);
    }

    private static String getLegalRepFirmName(OrganisationPolicy organisationPolicy,
                                              OrganisationService organisationService,
                                              String party) {
        String organisationID = organisationPolicy.getOrganisation().getOrganisationID();
        return organisationService.findOrganisationById(organisationID)
            .map(Organisation::getName)
            .orElse(String.format("legal representative for %s", party.toLowerCase()));
    }
}
