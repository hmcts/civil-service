package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Flags;
import uk.gov.hmcts.reform.civil.model.DQPartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Witness;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

public class FlagsUtils {

    public static String RESPONDENT_SOLICITOR_ONE_WITNESS = "Respondent solicitor 1 witness";
    public static String RESPONDENT_SOLICITOR_ONE_EXPERT = "Respondent solicitor 1 expert";
    public static String RESPONDENT_SOLICITOR_TWO_WITNESS = "Respondent solicitor 2 witness";
    public static String RESPONDENT_SOLICITOR_TWO_EXPERT = "Respondent solicitor 2 expert";
    public static String APPLICANT_SOLICITOR_WITNESS = "Applicant solicitor witness";
    public static String APPLICANT_SOLICITOR_EXPERT = "Applicant solicitor expert";

    private FlagsUtils() {
        //NO-OP
    }

    private static List<Element<DQPartyFlagStructure>> getTopLevelFieldForWitnessesWithFlagsStructure(
        List<Witness> witnessList,
        String roleOnCase) {
        List<Element<DQPartyFlagStructure>> list = new ArrayList<>();
        for (Witness witness : witnessList) {
            DQPartyFlagStructure build = createDQPartiesCaseFlagsField(witness.getFirstName(), witness.getLastName(), roleOnCase);
            list.add(element(build));
        }
        return list;
    }

    private static List<Element<DQPartyFlagStructure>> getTopLevelFieldForExpertsWithFlagsStructure(
        List<Expert> expertList,
        String roleOnCase) {
        List<Element<DQPartyFlagStructure>> list = new ArrayList<>();
        for (Expert expert : expertList) {
            DQPartyFlagStructure build = createDQPartiesCaseFlagsField(expert.getFirstName(), expert.getLastName(), roleOnCase);
            list.add(element(build));
        }
        return list;
    }

    private static DQPartyFlagStructure createDQPartiesCaseFlagsField(String firstName, String lastName, String roleOnCase) {
        return DQPartyFlagStructure.builder()
            .firstName(firstName)
            .lastName(lastName)
            .flags(createCaseFlagsField(firstName, lastName, roleOnCase))
            .build();
    }

    private static Flags createCaseFlagsField(String firstName, String lastName, String roleOnCase) {
        return Flags.builder()
                       .partyName(String.format("%s %s", firstName, lastName))
                       .roleOnCase(roleOnCase)
                       .details(List.of())
                       .build();
    }

    public static void addRespondentDQPartiesFlagStructure(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {
        addRespondent1ExpertAndWitnessFlagsStructure(builder, caseData);
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            addRespondent2ExpertAndWitnessFlagsStructure(builder, caseData);
        }
    }

    private static void addRespondent2ExpertAndWitnessFlagsStructure(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {
        List<Witness> respondent2Witnesses = new ArrayList<>();
        if (caseData.getRespondent2DQ().getWitnesses() != null) {
            respondent2Witnesses = unwrapElements(caseData.getRespondent2DQ().getWitnesses().getDetails());
        }

        List<Expert> respondent2Experts = new ArrayList<>();
        if (caseData.getRespondent2DQ().getExperts() != null) {
            respondent2Experts = unwrapElements(caseData.getRespondent2DQ().getExperts().getDetails());
        }

        builder.respondentSolicitor2Witnesses(getTopLevelFieldForWitnessesWithFlagsStructure(respondent2Witnesses, RESPONDENT_SOLICITOR_TWO_WITNESS));
        builder.respondentSolicitor2Experts(getTopLevelFieldForExpertsWithFlagsStructure(respondent2Experts, RESPONDENT_SOLICITOR_TWO_EXPERT));
    }

    private static void addRespondent1ExpertAndWitnessFlagsStructure(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {
        List<Witness> respondent1Witnesses = new ArrayList<>();
        if (caseData.getRespondent1DQ().getWitnesses() != null) {
            respondent1Witnesses = unwrapElements(caseData.getRespondent1DQ().getWitnesses().getDetails());
        }

        List<Expert> respondent1Experts = new ArrayList<>();
        if (caseData.getRespondent1DQ().getExperts() != null) {
            respondent1Experts = unwrapElements(caseData.getRespondent1DQ().getExperts().getDetails());
        }
        builder.respondentSolicitor1Witnesses(getTopLevelFieldForWitnessesWithFlagsStructure(respondent1Witnesses, RESPONDENT_SOLICITOR_ONE_WITNESS));
        builder.respondentSolicitor1Experts(getTopLevelFieldForExpertsWithFlagsStructure(respondent1Experts, RESPONDENT_SOLICITOR_ONE_EXPERT));
    }

    public static void addApplicantExpertAndWitnessFlagsStructure(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        List<Witness> applicant1Witnesses = new ArrayList<>();
        if (caseData.getApplicant1DQ().getWitnesses() != null) {
            applicant1Witnesses.addAll(unwrapElements(caseData.getApplicant1DQ().getWitnesses().getDetails()));
        }

        List<Expert> applicant1Experts = new ArrayList<>();
        if (caseData.getApplicant1DQ().getExperts() != null) {
            applicant1Experts.addAll(unwrapElements(caseData.getApplicant1DQ().getExperts().getDetails()));
        }

        if (getMultiPartyScenario(caseData) == TWO_V_ONE) {
            if (caseData.getApplicant2DQ().getWitnesses() != null) {
                applicant1Witnesses.addAll(unwrapElements(caseData.getApplicant2DQ().getWitnesses().getDetails()));
            }
            if (caseData.getApplicant2DQ().getExperts() != null) {
                applicant1Experts.addAll(unwrapElements(caseData.getApplicant2DQ().getExperts().getDetails()));
            }
        }

        updatedData.applicantSolicitorWitnesses(getTopLevelFieldForWitnessesWithFlagsStructure(applicant1Witnesses, APPLICANT_SOLICITOR_WITNESS));
        updatedData.applicantSolicitorExperts(getTopLevelFieldForExpertsWithFlagsStructure(applicant1Experts, APPLICANT_SOLICITOR_EXPERT));
    }
}
