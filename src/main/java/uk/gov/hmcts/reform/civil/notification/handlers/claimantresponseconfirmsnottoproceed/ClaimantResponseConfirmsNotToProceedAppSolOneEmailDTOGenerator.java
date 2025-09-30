package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseConfirmsNotToProceedAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    protected ClaimantResponseConfirmsNotToProceedAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            return caseData.isPartAdmitPayImmediatelyAccepted() ? notificationsProperties.getNotifyRespondentSolicitorPartAdmitPayImmediatelyAcceptedSpec()
                : notificationsProperties.getClaimantSolicitorConfirmsNotToProceedSpec();
        }

        return notificationsProperties.getClaimantSolicitorConfirmsNotToProceed();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-not-to-proceed-respondent-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));

        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {

            if (caseData.isPartAdmitPayImmediatelyAccepted()) {
                OrganisationPolicy organisationPolicy = caseData.getRespondent1OrganisationPolicy();
                properties.put(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(organisationPolicy, organisationService));
            } else {
                properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            }
        }

        return properties;
    }
}
