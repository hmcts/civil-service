package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed.ClaimDismissedEmailTemplater;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class CourtOfficerOrderApplicantEmailDTOGenerator extends EmailDTOGenerator {

    OrganisationService organisationService;

    public CourtOfficerOrderApplicantEmailDTOGenerator(OrganisationService organisationService,
                                                    NotificationsProperties notificationsProperties) {
        super(notificationsProperties);
        this.organisationService = organisationService;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getApplicant1LipOrSolicitorEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            if (caseData.isClaimantBilingual()) {
                return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            }
            return notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getGenerateOrderNotificationTemplate();
        }

    }

    @Override
    protected String getReferenceTemplate() {
        return "generate-order-notification-%s";
    }

    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
            properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        } else {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        }
        return properties;
    }
}
