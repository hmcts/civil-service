package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class CourtOfficerOrderRespondentEmailDTOGenerator  extends EmailDTOGenerator {

    OrganisationService organisationService;
    NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailAddress(CaseData caseData) {
        //It checks if resp is lip or not and then returns email
        return caseData.getRespondent1Email();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            if (caseData.isRespondentResponseBilingual()) {
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
        if (caseData.isRespondent1LiP()) {
            properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
            properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        } else {
            boolean isRespondent1 = true;
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                            isRespondent1, organisationService));
        }
        return properties;
    }
}
