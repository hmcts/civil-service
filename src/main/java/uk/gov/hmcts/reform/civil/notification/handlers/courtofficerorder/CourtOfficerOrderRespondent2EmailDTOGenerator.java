package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class CourtOfficerOrderRespondent2EmailDTOGenerator  extends EmailDTOGenerator {

    OrganisationService organisationService;
    NotificationsProperties notificationsProperties;

    /**
     *  If Resp 2 is lip the case will not reach court officer order state.
     *  So it will be solicitor email in this case
     */
    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondentSolicitor2EmailAddress();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "generate-order-notification-%s";
    }

    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = false;
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                            isRespondent1, organisationService));
        return properties;
    }
}
