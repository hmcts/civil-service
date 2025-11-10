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
        if (partyToUpdate == null) {
            return null;
        }
        if (partyToUpdate.getFlags() != null) {
            return partyToUpdate;
        }

        partyToUpdate.setFlags(createFlags(partyToUpdate.getPartyName(), roleOnCase));
        return partyToUpdate;
    }

    public static LitigationFriend updateLitFriend(String roleOnCase, LitigationFriend litFriendToUpdate) {
        if (litFriendToUpdate == null) {
            return null;
        }
        if (litFriendToUpdate.getFlags() != null) {
            return litFriendToUpdate;
        }
        // LitigationFriend was updated to split fullName into firstname and lastname for H&L ==================
        // ToDo: Remove the use of fullName after H&L changes are default =====================================
        String partyName = litFriendToUpdate.getFullName() != null ? litFriendToUpdate.getFullName()
            // ====================================================================================================
            : formattedPartyNameForFlags(litFriendToUpdate.getFirstName(), litFriendToUpdate.getLastName());

        litFriendToUpdate.setFlags(createFlags(partyName, roleOnCase));
        return litFriendToUpdate;
    }

    private static List<Element<PartyFlagStructure>> getTopLevelFieldForWitnessesWithFlagsStructure(
        List<Witness> witnessList,
        String roleOnCase) {
        List<Element<PartyFlagStructure>> list = new ArrayList<>();
        for (Witness witness : witnessList) {
            PartyFlagStructure build = createPartiesCaseFlagsField(
                witness.getPartyID(), witness.getFirstName(), witness.getLastName(),
                witness.getEmailAddress(), witness.getPhoneNumber(),
                roleOnCase
            );
            list.add(element(build));
        }
        return list;
    }

    private static List<Element<PartyFlagStructure>> getTopLevelFieldForExpertsWithFlagsStructure(
        List<Expert> expertList,
        String roleOnCase) {
        List<Element<PartyFlagStructure>> list = new ArrayList<>();
        for (Expert expert : expertList) {
            PartyFlagStructure build = createPartiesCaseFlagsField(
                expert.getPartyID(), expert.getFirstName(), expert.getLastName(),
                expert.getEmailAddress(), expert.getPhoneNumber(),
                roleOnCase
            );
            list.add(element(build));
        }
        return list;
    }

    public static void addRespondentDQPartiesFlagStructure(CaseData caseData) {
        if (caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getRespondent1DQExperts() != null) {
            caseData.setRespondent1Experts(
                getTopLevelFieldForExpertsWithFlagsStructure(
                    unwrapElements(caseData.getRespondent1DQ().getRespondent1DQExperts().getDetails()),
                    RESPONDENT_SOLICITOR_ONE_EXPERT
                )
            );
        }
        if (caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getRespondent1DQWitnesses() != null) {
            caseData.setRespondent1Witnesses(
                getTopLevelFieldForWitnessesWithFlagsStructure(
                    unwrapElements(caseData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()),
                    RESPONDENT_SOLICITOR_ONE_WITNESS
                )
            );
        }
        var multiPartyScenario = getMultiPartyScenario(caseData);
        if ((multiPartyScenario.equals(ONE_V_TWO_ONE_LEGAL_REP)
            || multiPartyScenario.equals(ONE_V_TWO_TWO_LEGAL_REP))
            && (caseData.getRespondent2DQ() != null || caseData.getRespondent2() != null)) {
            if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQExperts() != null) {
                caseData.setRespondent2Experts(
                    getTopLevelFieldForExpertsWithFlagsStructure(
                        unwrapElements(caseData.getRespondent2DQ().getRespondent2DQExperts().getDetails()),
                        RESPONDENT_SOLICITOR_TWO_EXPERT
                    )
                );
            }
            if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null) {
                caseData.setRespondent2Witnesses(
                    getTopLevelFieldForWitnessesWithFlagsStructure(
                        unwrapElements(caseData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()),
                        RESPONDENT_SOLICITOR_TWO_WITNESS
                    )
                );
            }
        } else if ((multiPartyScenario.equals(ONE_V_TWO_TWO_LEGAL_REP))
            && !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            && caseData.getRespondentResponseIsSame() != null
            && caseData.getRespondentResponseIsSame().equals(NO)) {
            if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQExperts() != null) {
                caseData.setRespondent2Experts(
                    getTopLevelFieldForExpertsWithFlagsStructure(
                        unwrapElements(caseData.getRespondent2DQ().getRespondent2DQExperts().getDetails()),
                        RESPONDENT_SOLICITOR_TWO_EXPERT
                    )
                );
            }
            if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null) {
                caseData.setRespondent2Witnesses(
                    getTopLevelFieldForWitnessesWithFlagsStructure(
                        unwrapElements(caseData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()),
                        RESPONDENT_SOLICITOR_TWO_WITNESS
                    )
                );
            }
        }
    }

    public static void addApplicantExpertAndWitnessFlagsStructure(CaseData caseData) {
        if (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getApplicant1DQExperts() != null) {
            caseData.setApplicantExperts(
                getTopLevelFieldForExpertsWithFlagsStructure(
                    unwrapElements(caseData.getApplicant1DQ().getApplicant1DQExperts().getDetails()),
                    APPLICANT_SOLICITOR_EXPERT
                )
            );
        }
        if (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getApplicant1DQWitnesses() != null) {
            caseData.setApplicantWitnesses(
                getTopLevelFieldForWitnessesWithFlagsStructure(
                    unwrapElements(caseData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails()),
                    APPLICANT_SOLICITOR_WITNESS
                )
            );
        }
        var multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(TWO_V_ONE)) {
            if (caseData.getApplicant2DQ() != null && caseData.getApplicant2DQ().getApplicant2DQExperts() != null) {
                var expertsAppl1 = caseData.getApplicantExperts() == null ? new ArrayList<Element<PartyFlagStructure>>()
                    : new ArrayList<>(caseData.getApplicantExperts());
                expertsAppl1.addAll(getTopLevelFieldForExpertsWithFlagsStructure(
                    unwrapElements(caseData.getApplicant2DQ().getApplicant2DQExperts().getDetails()),
                    APPLICANT_SOLICITOR_EXPERT
                ));
                caseData.setApplicantExperts(expertsAppl1);
            }
            if (caseData.getApplicant2DQ() != null && caseData.getApplicant2DQ().getApplicant2DQWitnesses() != null) {
                var witnessesAppl1 = caseData.getApplicantWitnesses() == null ? new ArrayList<Element<PartyFlagStructure>>()
                    : new ArrayList<>(caseData.getApplicantWitnesses());
                witnessesAppl1.addAll(getTopLevelFieldForWitnessesWithFlagsStructure(
                    unwrapElements(caseData.getApplicant2DQ().getApplicant2DQWitnesses().getDetails()),
                    APPLICANT_SOLICITOR_WITNESS
                ));
                caseData.setApplicantWitnesses(witnessesAppl1);
            }
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
                APPLICANT_ONE
            );
            caseData.setApplicant1LRIndividuals(updatePartyNameForPartyFlagStructures(
                caseData.getApplicant1LRIndividuals(),
                legalRepFirmName
            ));
        }
        if ((DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID).equals(partyChosen)) {
            String legalRepFirmName = getLegalRepFirmName(
                caseData.getRespondent1OrganisationPolicy(),
                organisationService,
                RESPONDENT_ONE
            );
            caseData.setRespondent1LRIndividuals(updatePartyNameForPartyFlagStructures(
                caseData.getRespondent1LRIndividuals(),
                legalRepFirmName
            ));
        }
        if ((DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID).equals(partyChosen)) {
            String legalRepFirmName = getLegalRepFirmName(
                caseData.getRespondent2OrganisationPolicy(),
                organisationService,
                RESPONDENT_TWO
            );
            caseData.setRespondent2LRIndividuals(updatePartyNameForPartyFlagStructures(
                caseData.getRespondent2LRIndividuals(),
                legalRepFirmName
            ));
        }
    }

    private static void updateOrgIndividualsFlags(CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            caseData.setApplicant1OrgIndividuals(updatePartyNameForPartyFlagStructures(
                caseData.getApplicant1OrgIndividuals(),
                caseData.getApplicant1().getPartyName()
            ));
        }
        if ((CLAIMANT_TWO_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            caseData.setApplicant2OrgIndividuals(updatePartyNameForPartyFlagStructures(
                caseData.getApplicant2OrgIndividuals(),
                caseData.getApplicant2().getPartyName()
            ));
        }
        if ((DEFENDANT_ONE_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            caseData.setRespondent1OrgIndividuals(updatePartyNameForPartyFlagStructures(
                caseData.getRespondent1OrgIndividuals(),
                caseData.getRespondent1().getPartyName()
            ));
        }
        if ((DEFENDANT_TWO_ORG_INDIVIDUALS_ID).equals(partyChosen)) {
            caseData.setRespondent2OrgIndividuals(updatePartyNameForPartyFlagStructures(
                caseData.getRespondent2OrgIndividuals(),
                caseData.getRespondent2().getPartyName()
            ));
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
            caseData.setApplicantExperts(updatePartyNameForPartyFlagStructures(
                caseData.getApplicantExperts(),
                APPLICANT_SOLICITOR_EXPERT
            ));
        }
        if ((DEFENDANT_ONE_EXPERTS_ID).equals(partyChosen)) {
            caseData.setRespondent1Experts(updatePartyNameForPartyFlagStructures(
                caseData.getRespondent1Experts(),
                RESPONDENT_SOLICITOR_ONE_EXPERT
            ));
        }
        if ((DEFENDANT_TWO_EXPERTS_ID).equals(partyChosen)) {
            caseData.setRespondent2Experts(updatePartyNameForPartyFlagStructures(
                caseData.getRespondent2Experts(),
                RESPONDENT_SOLICITOR_TWO_EXPERT
            ));
        }
    }

    private static void updateWitnessFlags(CaseData caseData, String partyChosen) {
        if ((CLAIMANT_ONE_WITNESSES_ID).equals(partyChosen)) {
            caseData.setApplicantWitnesses(updatePartyNameForPartyFlagStructures(
                caseData.getApplicantWitnesses(),
                APPLICANT_SOLICITOR_WITNESS
            ));
        }
        if ((DEFENDANT_ONE_WITNESSES_ID).equals(partyChosen)) {
            caseData.setRespondent1Witnesses(updatePartyNameForPartyFlagStructures(
                caseData.getRespondent1Witnesses(),
                RESPONDENT_SOLICITOR_ONE_WITNESS
            ));
        }
        if ((DEFENDANT_TWO_WITNESSES_ID).equals(partyChosen)) {
            caseData.setRespondent2Witnesses(updatePartyNameForPartyFlagStructures(
                caseData.getRespondent2Witnesses(),
                RESPONDENT_SOLICITOR_TWO_WITNESS
            ));
        }
    }

    private static void updatePartyFlags(CaseData caseData, String partyChosen) {
        if (CLAIMANT_ONE_ID.equals(partyChosen) && caseData.getApplicant1() != null && caseData.getApplicant1().getFlags() != null) {
            caseData.setApplicant1(updatePartyNameForFlags(caseData.getApplicant1()));
        }
        if (CLAIMANT_TWO_ID.equals(partyChosen) && caseData.getApplicant2() != null && caseData.getApplicant2().getFlags() != null) {
            caseData.setApplicant2(updatePartyNameForFlags(caseData.getApplicant2()));
        }
        if (DEFENDANT_ONE_ID.equals(partyChosen) && caseData.getRespondent1() != null && caseData.getRespondent1().getFlags() != null) {
            caseData.setRespondent1(updatePartyNameForFlags(caseData.getRespondent1()));
        }
        if (DEFENDANT_TWO_ID.equals(partyChosen) && caseData.getRespondent2() != null && caseData.getRespondent2().getFlags() != null) {
            caseData.setRespondent2(updatePartyNameForFlags(caseData.getRespondent2()));
        }
    }

    private static List<Element<PartyFlagStructure>> updatePartyNameForPartyFlagStructures(List<Element<PartyFlagStructure>> individuals,
                                                                                           String roleOnCase) {
        if (individuals != null && !individuals.isEmpty()) {
            List<PartyFlagStructure> partyFlagStructures = unwrapElements(individuals);
            List<PartyFlagStructure> updatedList = new ArrayList<>();
            for (PartyFlagStructure partyFlagStructure : partyFlagStructures) {
                String formattedPartyNameForFlags = formattedPartyNameForFlags(
                    partyFlagStructure.getFirstName(),
                    partyFlagStructure.getLastName()
                );
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
        if (party != null && party.getFlags() != null) {
            party.getFlags().setPartyName(party.getPartyName());
        }
        return party;
    }

    private static LitigationFriend updatePartyNameForLitigationFriendFlags(LitigationFriend litigationFriend) {
        if (litigationFriend != null && litigationFriend.getFlags() != null) {
            String partyName = litigationFriend.getFullName() != null
                ? litigationFriend.getFullName()
                : formattedPartyNameForFlags(litigationFriend.getFirstName(), litigationFriend.getLastName());
            litigationFriend.getFlags().setPartyName(partyName);
        }
        return litigationFriend;
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
