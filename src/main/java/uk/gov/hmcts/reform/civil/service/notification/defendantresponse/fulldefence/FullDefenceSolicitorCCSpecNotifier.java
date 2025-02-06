package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@AllArgsConstructor
public abstract class FullDefenceSolicitorCCSpecNotifier extends FullDefenceSolicitorNotifier {

    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    protected String getTemplateForSpecOtherThan1v2DS(CaseData caseData) {
        String emailTemplate;
        if ((caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == BY_SET_DATE
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            &&
            (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
        ) {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction();
        } else {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        }
        return emailTemplate;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            );
        } else {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            );
        }
    }

    protected String getLegalOrganisationName(CaseData caseData) {
        String organisationID;
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            organisationID = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            organisationID = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }

        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

}
