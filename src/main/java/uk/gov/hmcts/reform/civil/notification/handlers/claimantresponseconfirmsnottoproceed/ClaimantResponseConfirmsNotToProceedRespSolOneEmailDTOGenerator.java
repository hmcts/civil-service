package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseConfirmsNotToProceedRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {
    protected ClaimantResponseConfirmsNotToProceedRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            return caseData.isPartAdmitPayImmediatelyAccepted() ? notificationsProperties.getNotifyRespondentSolicitorPartAdmitPayImmediatelyAcceptedSpec() :
                notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec();
        }

        return notificationsProperties.getClaimantSolicitorConfirmsNotToProceed();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-not-to-proceed-respondent-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        OrganisationPolicy organisationPolicy = caseData.getRespondent1OrganisationPolicy();
        String respondentLegalOrgName = getRespondentLegalOrganizationName(organisationPolicy, organisationService);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, respondentLegalOrgName);

        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            if (caseData.isPartAdmitPayImmediatelyAccepted()) {
                properties.put(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, respondentLegalOrgName);
            } else {
                properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            }
        }
        return properties;
    }
}
