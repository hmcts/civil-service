package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
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

import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

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
        return Flags.builder()
            .partyName(flagsPartyName)
            .roleOnCase(roleOnCase)
            .details(List.of())
            .build();
    }

    private static PartyFlagStructure createPartiesCaseFlagsField(String partyId, String firstName, String lastName,
                                                                  String email, String phone, String roleOnCase) {
        String partyName = formattedPartyNameForFlags(firstName, lastName);
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
                : formattedPartyNameForFlags(litFriendToUpdate.getFirstName(), litFriendToUpdate.getLastName()),
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

    public static void createOrUpdateFlags(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData, OrganisationService organisationService) {
        String partyChosen = caseData.getUpdateDetailsForm().getPartyChosenId();
        // claimant/defendant
        updatePartyFlags(builder, caseData, partyChosen);
        // litigation friend
        updateLitigationFriendFlags(builder, caseData, partyChosen);
        // attending for org/company
        updateOrgIndividualsFlags(builder, caseData, partyChosen);
        // attending for legal rep
        updateLRIndividualsFlags(builder, caseData, partyChosen, organisationService);
        // experts
        updateExpertFlags(builder, caseData, partyChosen);
        // witnesses
        updateWitnessFlags(builder, caseData, partyChosen);
    }

    private static void updateLRIndividualsFlags(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData, String partyChosen, OrganisationService organisationService) {
        if ((CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID).equals(partyChosen)) {
            String legalRepFirmName = getLegalRepFirmName(
                caseData.getApplicant1OrganisationPolicy(),
                organisationService,
                APPLICANT_ONE);
            builder.applicant1LRIndividuals(updatePartyNameForPartyFlagStructures(caseData.getApplicant1LRIndividuals(), legalRepFirmName));
        }
        if ((DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID).equals(partyChosen)) {
            String legalRepFirmName = getLegalRepFirmName(
                caseData.getRespondent1OrganisationPolicy(),
                organisationService,
                RESPONDENT_ONE);
            builder.respondent1LRIndividuals(updatePartyNameForPartyFlagStructures(caseData.getRespondent1LRIndividuals(), legalRepFirmName));
        }
        if ((DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID).equals(partyChosen)) {
            String legalRepFirmName = getLegalRepFirmName(
                caseData.getRespondent2OrganisationPolicy(),
                organisationService,
                RESPONDENT_TWO);
            builder.respondent2LRIndividuals(updatePartyNameForPartyFlagStructures(caseData.getRespondent2LRIndividuals(), legalRepFirmName));
        }
    }

    private static void updateOrgIndividualsFlags(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            builder.applicant1OrgIndividuals(updatePartyNameForPartyFlagStructures(caseData.getApplicant1OrgIndividuals(), caseData.getApplicant1().getPartyName()));
        }
        if ((CLAIMANT_TWO_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            builder.applicant2OrgIndividuals(updatePartyNameForPartyFlagStructures(caseData.getApplicant2OrgIndividuals(), caseData.getApplicant2().getPartyName()));
        }
        if ((DEFENDANT_ONE_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            builder.respondent1OrgIndividuals(updatePartyNameForPartyFlagStructures(caseData.getRespondent1OrgIndividuals(), caseData.getRespondent1().getPartyName()));
        }
        if ((DEFENDANT_TWO_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            builder.respondent2OrgIndividuals(updatePartyNameForPartyFlagStructures(caseData.getRespondent2OrgIndividuals(), caseData.getRespondent2().getPartyName()));
        }
    }

    private static void updateLitigationFriendFlags(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_LITIGATION_FRIEND_ID).equals(partyChosen)) {
            builder.applicant1LitigationFriend(updatePartyNameForLitigationFriendFlags(caseData.getApplicant1LitigationFriend()));
        }
        if ((CLAIMANT_TWO_LITIGATION_FRIEND_ID).equals(partyChosen)) {
            builder.applicant2LitigationFriend(updatePartyNameForLitigationFriendFlags(caseData.getApplicant2LitigationFriend()));
        }
        if ((DEFENDANT_ONE_LITIGATION_FRIEND_ID).equals(partyChosen)) {
            builder.respondent1LitigationFriend(updatePartyNameForLitigationFriendFlags(caseData.getRespondent1LitigationFriend()));
        }
        if ((DEFENDANT_TWO_LITIGATION_FRIEND_ID).equals(partyChosen)) {
            builder.respondent2LitigationFriend(updatePartyNameForLitigationFriendFlags(caseData.getRespondent2LitigationFriend()));
        }
    }

    private static void updateExpertFlags(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_EXPERTS_ID).equals(partyChosen)) {
            builder.applicantExperts(updatePartyNameForPartyFlagStructures(caseData.getApplicantExperts(), APPLICANT_SOLICITOR_EXPERT));
        }
        if ((DEFENDANT_ONE_EXPERTS_ID).equals(partyChosen)) {
            builder.respondent1Experts(updatePartyNameForPartyFlagStructures(caseData.getRespondent1Experts(), RESPONDENT_SOLICITOR_ONE_EXPERT));
        }
        if ((DEFENDANT_TWO_EXPERTS_ID).equals(partyChosen)) {
            builder.respondent2Experts(updatePartyNameForPartyFlagStructures(caseData.getRespondent2Experts(), RESPONDENT_SOLICITOR_TWO_EXPERT));
        }
    }

    private static void updateWitnessFlags(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_WITNESSES_ID).equals(partyChosen)) {
            builder.applicantWitnesses(updatePartyNameForPartyFlagStructures(caseData.getApplicantWitnesses(), APPLICANT_SOLICITOR_WITNESS));
        }
        if ((DEFENDANT_ONE_WITNESSES_ID).equals(partyChosen)) {
            builder.respondent1Witnesses(updatePartyNameForPartyFlagStructures(caseData.getRespondent1Witnesses(), RESPONDENT_SOLICITOR_ONE_WITNESS));
        }
        if ((DEFENDANT_TWO_WITNESSES_ID).equals(partyChosen)) {
            builder.respondent2Witnesses(updatePartyNameForPartyFlagStructures(caseData.getRespondent2Witnesses(), RESPONDENT_SOLICITOR_TWO_WITNESS));
        }
    }

    private static void updatePartyFlags(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_ID).equals(partyChosen)) {
            builder.applicant1(updatePartyNameForFlags(caseData.getApplicant1()));
        }
        if ((CLAIMANT_TWO_ID).equals(partyChosen)) {
            builder.applicant2(updatePartyNameForFlags(caseData.getApplicant2()));
        }
        if ((DEFENDANT_ONE_ID).equals(partyChosen)) {
            builder.respondent1(updatePartyNameForFlags(caseData.getRespondent1()));
        }
        if ((DEFENDANT_TWO_ID).equals(partyChosen)) {
            builder.respondent2(updatePartyNameForFlags(caseData.getRespondent2()));
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
                                                 .toBuilder()
                                                 .flags(createFlags(formattedPartyNameForFlags, roleOnCase)).build()));
                } else {
                    // existing party with flags so just update the name
                    updatedList.add(
                        partyFlagStructure
                            .toBuilder()
                            .flags(partyFlagStructure.getFlags().toBuilder()
                                       .partyName(formattedPartyNameForFlags)
                                       .build()).build());
                }
            }
            return wrapElements(updatedList);
        }
        return null;
    }

    private static Party updatePartyNameForFlags(Party party) {
        return party.toBuilder().flags(party.getFlags().toBuilder()
                                           .partyName(party.getPartyName())
                                           .build()).build();
    }

    private static LitigationFriend updatePartyNameForLitigationFriendFlags(LitigationFriend litigationFriend) {
        return litigationFriend.toBuilder()
            .flags(litigationFriend.getFlags().toBuilder()
                       .partyName(litigationFriend.getFullName() != null ? litigationFriend.getFullName()
                                      : formattedPartyNameForFlags(litigationFriend.getFirstName(), litigationFriend.getLastName()))
                       .build()).build();
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
