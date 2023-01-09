package uk.gov.hmcts.reform.civil.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;

@Component
public class OrgPolicyValidator {

    public static final String ERROR_SAME_SOLICITOR_ORGANISATION =
        "The legal representative details for the claimant and defendant are the same.  "
            + "Please amend accordingly.";

    public List<String> validate(OrganisationPolicy organisationPolicy, YesOrNo solicitorFirmRegistered) {
        List<String> errors = new ArrayList<>();

        if (solicitorFirmRegistered == YesOrNo.YES && (organisationPolicy == null
            || organisationPolicy.getOrganisation() == null
            || organisationPolicy.getOrganisation().getOrganisationID() == null)) {
            errors.add("No Organisation selected");
        }

        return errors;
    }

    public List<String> validateSolicitorOrganisations(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
            || ONE_V_TWO_ONE_LEGAL_REP.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
            || TWO_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (caseData.getRespondent1OrganisationPolicy() != null
                && caseData.getApplicant1OrganisationPolicy() != null
                && caseData.getRespondent1Represented() == YesOrNo.YES
                && caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID().equals(
                caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID())) {
                errors.add(ERROR_SAME_SOLICITOR_ORGANISATION);
            }
        }
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (caseData.getApplicant1OrganisationPolicy() != null
                && ((caseData.getRespondent1OrganisationPolicy() != null
                && caseData.getRespondent1Represented() == YesOrNo.YES
                && caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID().equals(
                caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID()))
                || (caseData.getRespondent2OrganisationPolicy() != null
                && caseData.getRespondent2Represented() == YesOrNo.YES
                && caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID().equals(
                caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID())))) {
                errors.add(ERROR_SAME_SOLICITOR_ORGANISATION);
            }

        }
        return errors;
    }
}
