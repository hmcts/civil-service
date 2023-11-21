package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.ORGANISATION;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class ManageContactInformationUtils {

    private ManageContactInformationUtils() {
        // NO-OP
    }

    private static final String CLAIMANT_ONE = "CLAIMANT 1:";
    private static final String CLAIMANT_TWO = "CLAIMANT 2:";
    private static final String CLAIMANTS = "CLAIMANTS:";

    private static final String DEFENDANT_ONE = "DEFENDANT 1:";
    private static final String DEFENDANT_TWO = "DEFENDANT 2:";
    private static final String DEFENDANTS = "DEFENDANTS:";

    private static final String WITNESSES = "Witnesses";
    private static final String EXPERTS = "Experts";

    private static final String LITIGATION_FRIEND = "Litigation Friend:";
    private static final String ORG_INDIVIDUALS = "Individuals attending for the organisation";
    private static final String LEGAL_REP_INDIVIDUALS = "Individuals attending for the legal representative";

    public static final String CLAIMANT_ONE_ID = "CLAIMANT_1";
    public static final String CLAIMANT_ONE_LITIGATION_FRIEND_ID = "CLAIMANT_1_LITIGATION_FRIEND";
    public static final String CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID = "CLAIMANT_1_LR_INDIVIDUALS";
    public static final String CLAIMANT_ONE_ORG_INDIVIDUALS_ID = "CLAIMANT_1_ORGANISATION_INDIVIDUALS";
    public static final String CLAIMANT_ONE_WITNESSES_ID = "CLAIMANT_1_WITNESSES";
    public static final String CLAIMANT_ONE_EXPERTS_ID = "CLAIMANT_1_EXPERTS";

    public static final String CLAIMANT_TWO_ID = "CLAIMANT_2";
    public static final String CLAIMANT_TWO_LITIGATION_FRIEND_ID = "CLAIMANT_2_LITIGATION_FRIEND";
    public static final String CLAIMANT_TWO_ORG_INDIVIDUALS_ID = "CLAIMANT_2_ORGANISATION_INDIVIDUALS";

    public static final String DEFENDANT_ONE_ID = "DEFENDANT_1";
    public static final String DEFENDANT_ONE_LITIGATION_FRIEND_ID = "DEFENDANT_1_LITIGATION_FRIEND";
    public static final String DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID = "DEFENDANT_1_LR_INDIVIDUALS";
    public static final String DEFENDANT_ONE_ORG_INDIVIDUALS_ID = "DEFENDANT_1_ORGANISATION_INDIVIDUALS";
    public static final String DEFENDANT_ONE_WITNESSES_ID = "DEFENDANT_1_WITNESSES";
    public static final String DEFENDANT_ONE_EXPERTS_ID = "DEFENDANT_1_EXPERTS";

    public static final String DEFENDANT_TWO_ID = "DEFENDANT_2";
    public static final String DEFENDANT_TWO_LITIGATION_FRIEND_ID = "DEFENDANT_2_LITIGATION_FRIEND";
    public static final String DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID = "DEFENDANT_2_LR_INDIVIDUALS";
    public static final String DEFENDANT_TWO_ORG_INDIVIDUALS_ID = "DEFENDANT_2_ORGANISATION_INDIVIDUALS";
    public static final String DEFENDANT_TWO_WITNESSES_ID = "DEFENDANT_2_WITNESSES";
    public static final String DEFENDANT_TWO_EXPERTS_ID = "DEFENDANT_2_EXPERTS";

    public static void addApplicant1Options(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        addApplicant1PartyOptions(list, caseData);
        addApplicantLegalRepIndividuals(list, false);
        addApplicant1ExpertsAndWitnesses(list, caseData, isAdmin);
    }

    public static void addApplicantOptions2v1(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        addApplicant1PartyOptions(list, caseData);
        addApplicant2PartyOptions(list, caseData);
        addApplicantLegalRepIndividuals(list, true);
        addApplicantExpertsAndWitnesses2v1(list, caseData, isAdmin);
    }

    public static void addDefendantOptions1v2SameSolicitor(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        addDefendant1PartyOptions(list, caseData);
        addDefendant2PartyOptions(list, caseData);
        addDefendantLegalRepIndividuals1v2Same(list);
        addDefendantExpertsAndWitnesses1v2SameSolicitor(list, caseData, isAdmin);
    }

    public static void addDefendant1Options(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        addDefendant1PartyOptions(list, caseData);
        if (YES.equals(caseData.getRespondent1Represented())) {
            addLegalRepIndividuals(list, DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID, DEFENDANT_ONE);
        }
        addDefendant1ExpertsAndWitnesses(list, caseData, isAdmin);
    }

    public static void addDefendant2Options(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        addDefendant2PartyOptions(list, caseData);
        if (YES.equals(caseData.getRespondent2Represented())) {
            addLegalRepIndividuals(list, DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID, DEFENDANT_TWO);
        }
        addDefendant2ExpertsAndWitnesses(list, caseData, isAdmin);
    }

    public static String appendUserAndType(String partyChosen, CaseData caseData, boolean isAdmin) {
        String user = isAdmin ? "ADMIN" : "LR";

        switch (partyChosen) {
            case (CLAIMANT_ONE_ID): {
                return formatId(partyChosen, user, caseData.getApplicant1());
            }
            case(CLAIMANT_TWO_ID): {
                return formatId(partyChosen, user, caseData.getApplicant2());
            }
            case (DEFENDANT_ONE_ID): {
                return formatId(partyChosen, user, caseData.getRespondent1());
            }
            case(DEFENDANT_TWO_ID): {
                return formatId(partyChosen, user, caseData.getRespondent2());
            }
            case(CLAIMANT_ONE_LITIGATION_FRIEND_ID):
            case(CLAIMANT_TWO_LITIGATION_FRIEND_ID):
            case(DEFENDANT_ONE_LITIGATION_FRIEND_ID):
            case(DEFENDANT_TWO_LITIGATION_FRIEND_ID): {
                return formatId(partyChosen, user);
            }
            default: {
                throw new IllegalArgumentException("Manage Contact Information party chosen ID does not exist");
            }
        }
    }

    public static List<Element<UpdatePartyDetailsForm>> mapExpertsToUpdatePartyDetailsForm(List<Element<Expert>> experts) {
        List<Element<UpdatePartyDetailsForm>> newExperts = new ArrayList<>();

        if (experts != null) {
            for (Element<Expert> party : experts) {
                Expert expert = party.getValue();
                newExperts.addAll(wrapElements(UpdatePartyDetailsForm.builder()
                                                   .firstName(expert.getFirstName())
                                                   .lastName(expert.getLastName())
                                                   .emailAddress(expert.getEmailAddress())
                                                   .phoneNumber(expert.getPhoneNumber())
                                                   .fieldOfExpertise(expert.getFieldOfExpertise())
                                                   .partyId(expert.getPartyID()) //this will need to be added in new ticket
                                                   .build()));
            }
        }
        return newExperts;
    }

    public static List<Element<Expert>> mapUpdatePartyDetailsFormToDQExperts(List<Element<Expert>> existingDQExperts, List<Element<UpdatePartyDetailsForm>> formExperts) {
        List<Element<Expert>> newExperts = new ArrayList<>();
        List<Expert> experts = unwrapElements(existingDQExperts);

        if (formExperts != null) {
            for (Element<UpdatePartyDetailsForm> form : formExperts) {
                UpdatePartyDetailsForm formExpert = form.getValue();

                Expert dqExpert = experts.stream()
                    .filter(e -> e.getPartyID().equals(formExpert.getPartyId()))
                    .findFirst()
                    .orElse(null);

                if (dqExpert != null && dqExpert.getPartyID() != null) {
                    // if id already exists in dq
                    newExperts.addAll(wrapElements(dqExpert.toBuilder()
                                                       .firstName(formExpert.getFirstName())
                                                       .lastName(formExpert.getLastName())
                                                       .emailAddress(formExpert.getEmailAddress())
                                                       .phoneNumber(formExpert.getPhoneNumber())
                                                       .fieldOfExpertise(formExpert.getFieldOfExpertise())
                                                       .build()));
                } else {
                    // if id doesn't exist in dq means it is a newly added expert
                    newExperts.addAll(wrapElements(Expert.builder()
                                                       .firstName(formExpert.getFirstName())
                                                       .lastName(formExpert.getLastName())
                                                       .emailAddress(formExpert.getEmailAddress())
                                                       .phoneNumber(formExpert.getPhoneNumber())
                                                       .fieldOfExpertise(formExpert.getFieldOfExpertise())
                                                       .dateAdded(LocalDate.now())
                                                       .eventAdded("Manage Contact Information Event")
                                                       .partyID(null) //CIV-10382
                                                       .build()));
                    // Replace above to this in CIV-10382
                    // newExperts.addAll(wrapElements(appendWithNewPartyIds(Expert.builder()
                    //                                   .firstName(formExpert.getFirstName())
                    //                                  .lastName(formExpert.getLastName())
                    //                                  .emailAddress(formExpert.getEmailAddress())
                    //                                  .phoneNumber(formExpert.getPhoneNumber())
                    //                                  .fieldOfExpertise(formExpert.getFieldOfExpertise())
                    //                                  .dateAdded(LocalDate.now())
                    //                                  .eventAdded("Manage Contact Information Event")
                    //                                  .build())));
                }
            }
        }

        return newExperts;
    }

    public static List<Element<UpdatePartyDetailsForm>> mapWitnessesToUpdatePartyDetailsForm(List<Element<Witness>> witnesses) {
        List<Element<UpdatePartyDetailsForm>> newWitnesses = new ArrayList<>();

        if (witnesses != null) {
            for (Element<Witness> party : witnesses) {
                Witness witness = party.getValue();
                newWitnesses.addAll(wrapElements(UpdatePartyDetailsForm.builder()
                                                   .firstName(witness.getFirstName())
                                                   .lastName(witness.getLastName())
                                                   .emailAddress(witness.getEmailAddress())
                                                   .phoneNumber(witness.getPhoneNumber())
                                                   .partyId(witness.getPartyID())
                                                   .build()));
            }
        }
        return newWitnesses;
    }

    public static List<Element<Witness>> mapUpdatePartyDetailsFormToDQWitnesses(List<Element<Witness>> existingDQWitnesses, List<Element<UpdatePartyDetailsForm>> formWitnesses) {
        List<Element<Witness>> newWitnesses = new ArrayList<>();
        List<Witness> witnesses = unwrapElements(existingDQWitnesses);

        if (formWitnesses != null) {
            for (Element<UpdatePartyDetailsForm> form : formWitnesses) {
                UpdatePartyDetailsForm formWitness = form.getValue();

                Witness dqWitness = witnesses.stream()
                    .filter(w -> w.getPartyID().equals(formWitness.getPartyId()))
                    .findFirst()
                    .orElse(null);

                // if id already exists in dq
                if (dqWitness != null && dqWitness.getPartyID() != null) {
                    newWitnesses.addAll(wrapElements(dqWitness.toBuilder()
                                                       .firstName(formWitness.getFirstName())
                                                       .lastName(formWitness.getLastName())
                                                       .emailAddress(formWitness.getEmailAddress())
                                                       .phoneNumber(formWitness.getPhoneNumber())
                                                       .build()));
                } else {
                    // if id doesn't exist in dq means it is a newly added witness
                    newWitnesses.addAll(wrapElements(Witness.builder()
                                                       .firstName(formWitness.getFirstName())
                                                       .lastName(formWitness.getLastName())
                                                       .emailAddress(formWitness.getEmailAddress())
                                                       .phoneNumber(formWitness.getPhoneNumber())
                                                       .dateAdded(LocalDate.now())
                                                       .eventAdded("Manage Contact Information Event")
                                                       .partyID(null) //CIV-10382
                                                       .build()));
                    // Replace above to this in CIV-10382
                    // newWitnesses.addAll(wrapElements(appendWithNewPartyIds(Witness.builder()
                    //                                  .firstName(formWitness.getFirstName())
                    //                                  .lastName(formWitness.getLastName())
                    //                                  .emailAddress(formWitness.getEmailAddress())
                    //                                  .phoneNumber(formWitness.getPhoneNumber())
                    //                                  .dateAdded(LocalDate.now())
                    //                                  .eventAdded("Manage Contact Information Event")
                    //                                   .build())));
                }
            }
        }

        return newWitnesses;
    }

    private static String formatId(String partyChosen, String isAdmin, Party party) {
        return String.format("%s_%s_%s", partyChosen, isAdmin, party.getType().toString());
    }

    private static String formatId(String partyChosen, String isAdmin) {
        return String.format("%s_%s", partyChosen, isAdmin);
    }

    private static void addApplicant1PartyOptions(List<DynamicListElement> list, CaseData caseData) {
        // applicant 1 party name
        list.add(dynamicElementFromCode(CLAIMANT_ONE_ID,
                                        String.format("%s %s", CLAIMANT_ONE, caseData.getApplicant1().getPartyName())));
        // applicant 1 litigation friend
        if (shouldAddLitigationFriend(caseData.getApplicant1().getType())) {
            if (caseData.getApplicant1LitigationFriend() != null) {
                addLitigationFriend(list, CLAIMANT_ONE_LITIGATION_FRIEND_ID, CLAIMANT_ONE,
                                    caseData.getApplicant1LitigationFriend().getFirstName(),
                                    caseData.getApplicant1LitigationFriend().getLastName());
            }
        } else {
            // applicant 1 org individuals
            addOrganisationIndividuals(list, CLAIMANT_ONE_ORG_INDIVIDUALS_ID, CLAIMANT_ONE);
        }
    }

    private static void addApplicant2PartyOptions(List<DynamicListElement> list, CaseData caseData) {
        // applicant 2 party name
        list.add(dynamicElementFromCode(CLAIMANT_TWO_ID, String.format("%s %s", CLAIMANT_TWO, caseData.getApplicant2().getPartyName())));
        // applicant 2 litigation friend
        if (shouldAddLitigationFriend(caseData.getApplicant2().getType())) {
            if (caseData.getApplicant2LitigationFriend() != null) {
                addLitigationFriend(list, CLAIMANT_TWO_LITIGATION_FRIEND_ID, CLAIMANT_TWO,
                                    caseData.getApplicant2LitigationFriend().getFirstName(),
                                    caseData.getApplicant2LitigationFriend().getLastName());
            }
        } else {
            // applicant 2 org individuals
            addOrganisationIndividuals(list, CLAIMANT_TWO_ORG_INDIVIDUALS_ID, CLAIMANT_ONE);
        }
    }

    private static void addDefendant1PartyOptions(List<DynamicListElement> list, CaseData caseData) {
        // defendant 1 party name
        list.add(dynamicElementFromCode(DEFENDANT_ONE_ID, String.format("%s %s", DEFENDANT_ONE, caseData.getRespondent1().getPartyName())));
        // defendant 1 litigation friend
        if (shouldAddLitigationFriend(caseData.getRespondent1().getType())) {
            if (caseData.getRespondent1LitigationFriend() != null) {
                addLitigationFriend(list, DEFENDANT_ONE_LITIGATION_FRIEND_ID, DEFENDANT_ONE,
                                    caseData.getRespondent1LitigationFriend().getFirstName(),
                                    caseData.getRespondent1LitigationFriend().getLastName());
            }
        } else {
            // defendant 1 org individuals
            addOrganisationIndividuals(list, DEFENDANT_ONE_ORG_INDIVIDUALS_ID, DEFENDANT_ONE);
        }
    }

    private static void addDefendant2PartyOptions(List<DynamicListElement> list, CaseData caseData) {
        // defendant 2 party name
        list.add(dynamicElementFromCode(DEFENDANT_TWO_ID, String.format("%s %s", DEFENDANT_TWO, caseData.getRespondent2().getPartyName())));
        // defendant 2 litigation friend
        if (shouldAddLitigationFriend(caseData.getRespondent2().getType())) {
            if (caseData.getRespondent2LitigationFriend() != null) {
                addLitigationFriend(list, DEFENDANT_TWO_LITIGATION_FRIEND_ID, DEFENDANT_TWO,
                                    caseData.getRespondent2LitigationFriend().getFirstName(),
                                    caseData.getRespondent2LitigationFriend().getLastName());
            }
        } else {
            // defendant 2 org individuals
            addOrganisationIndividuals(list, DEFENDANT_TWO_ORG_INDIVIDUALS_ID, DEFENDANT_TWO);
        }
    }

    private static void addApplicantLegalRepIndividuals(List<DynamicListElement> list, boolean is2v1) {
        addLegalRepIndividuals(list, CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID, is2v1 ? CLAIMANTS : CLAIMANT_ONE);
    }

    private static void addDefendantLegalRepIndividuals1v2Same(List<DynamicListElement> list) {
        addLegalRepIndividuals(list, DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID, DEFENDANTS);
    }

    private static void addApplicant1ExpertsAndWitnesses(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        if (caseData.getApplicant1ResponseDate() != null) {
            // show experts and witnesses if admin user or if it exists
            if (isAdmin || (caseData.getApplicant1DQ() != null && shouldAddWitnesses(caseData.getApplicant1DQ().getWitnesses()))) {
                addWitnesses(list, CLAIMANT_ONE_WITNESSES_ID, CLAIMANT_ONE);
            }
            if (isAdmin || (caseData.getApplicant1DQ() != null && shouldAddExperts(caseData.getApplicant1DQ().getExperts()))) {
                addExperts(list, CLAIMANT_ONE_EXPERTS_ID, CLAIMANT_ONE);
            }
        }
    }

    private static void addApplicantExpertsAndWitnesses2v1(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        if (caseData.getApplicant1ResponseDate() != null || caseData.getApplicant2ResponseDate() != null) {
            // show experts and witnesses if admin user or if it exists
            if (isAdmin
                || (caseData.getApplicant1DQ() != null && shouldAddWitnesses(caseData.getApplicant1DQ().getWitnesses()))
                || (caseData.getApplicant2DQ() != null && shouldAddWitnesses(caseData.getApplicant2DQ().getWitnesses()))) {
                addWitnesses(list, CLAIMANT_ONE_WITNESSES_ID, CLAIMANTS);
            }
            if (isAdmin
                || (caseData.getApplicant1DQ() != null && shouldAddExperts(caseData.getApplicant1DQ().getExperts()))
                || (caseData.getApplicant2DQ() != null && shouldAddExperts(caseData.getApplicant2DQ().getExperts()))) {
                addExperts(list, CLAIMANT_ONE_EXPERTS_ID, CLAIMANTS);
            }
        }
    }

    private static void addDefendant1ExpertsAndWitnesses(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        if (caseData.getRespondent1ResponseDate() != null) {
            // show experts and witnesses if admin user or if it exists
            if (isAdmin
                || caseData.getRespondent1DQ() != null && shouldAddWitnesses(caseData.getRespondent1DQ().getWitnesses())) {
                addWitnesses(list, DEFENDANT_ONE_WITNESSES_ID, DEFENDANT_ONE);
            }
            if (isAdmin
                || caseData.getRespondent1DQ() != null && shouldAddExperts(caseData.getRespondent1DQ().getExperts())) {
                addExperts(list, DEFENDANT_ONE_EXPERTS_ID, DEFENDANT_ONE);
            }
        }
    }

    private static void addDefendant2ExpertsAndWitnesses(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        if (caseData.getRespondent2ResponseDate() != null) {
            // show experts and witnesses if admin user or if it exists
            if (isAdmin
                || caseData.getRespondent2DQ() != null && shouldAddWitnesses(caseData.getRespondent2DQ().getWitnesses())) {
                addWitnesses(list, DEFENDANT_TWO_WITNESSES_ID, DEFENDANT_TWO);
            }
            if (isAdmin
                || caseData.getRespondent2DQ() != null && shouldAddExperts(caseData.getRespondent2DQ().getExperts())) {
                addExperts(list, DEFENDANT_TWO_EXPERTS_ID, DEFENDANT_TWO);
            }
        }
    }

    private static void addDefendantExpertsAndWitnesses1v2SameSolicitor(List<DynamicListElement> list, CaseData caseData, boolean isAdmin) {
        if (caseData.getRespondent1ResponseDate() != null || caseData.getRespondent2ResponseDate() != null) {
            // show experts and witnesses if admin user or if it exists
            if (isAdmin
                || (caseData.getRespondent1DQ() != null && shouldAddWitnesses(caseData.getRespondent1DQ().getWitnesses()))
                || (caseData.getRespondent2DQ() != null && shouldAddWitnesses(caseData.getRespondent2DQ().getWitnesses()))) {
                addWitnesses(list, DEFENDANT_ONE_WITNESSES_ID, DEFENDANTS);
            }
            if (isAdmin
                || (caseData.getRespondent1DQ() != null && shouldAddExperts(caseData.getRespondent1DQ().getExperts()))
                || (caseData.getRespondent2DQ() != null && shouldAddExperts(caseData.getRespondent2DQ().getExperts()))) {
                addExperts(list, DEFENDANT_ONE_EXPERTS_ID, DEFENDANTS);
            }
        }
    }

    private static boolean shouldAddExperts(Experts experts) {
        return experts != null && YES.equals(experts.getExpertRequired())
            && experts.getDetails() != null
            && !experts.getDetails().isEmpty();
    }

    private static boolean shouldAddWitnesses(Witnesses witnesses) {
        return witnesses != null && YES.equals(witnesses.getWitnessesToAppear())
            && witnesses.getDetails() != null
            && !witnesses.getDetails().isEmpty();
    }

    private static boolean shouldAddLitigationFriend(Party.Type partyType) {
        return !(COMPANY.equals(partyType) || ORGANISATION.equals(partyType));
    }

    private static void addLitigationFriend(List<DynamicListElement> list, String id, String party, String firstName, String lastName) {
        list.add(dynamicElementFromCode(id, String.format("%s %s %s %s", party, LITIGATION_FRIEND, firstName, lastName)));
    }

    private static void addOrganisationIndividuals(List<DynamicListElement> list, String id, String party) {
        list.add(dynamicElementFromCode(id, String.format("%s %s", party, ORG_INDIVIDUALS)));
    }

    private static void addLegalRepIndividuals(List<DynamicListElement> list, String id, String party) {
        list.add(dynamicElementFromCode(id, String.format("%s %s", party, LEGAL_REP_INDIVIDUALS)));
    }

    private static void addWitnesses(List<DynamicListElement> list, String id, String party) {
        list.add(dynamicElementFromCode(id, String.format("%s %s", party, WITNESSES)));
    }

    private static void addExperts(List<DynamicListElement> list, String id, String party) {
        list.add(dynamicElementFromCode(id, String.format("%s %s", party, EXPERTS)));
    }
}
