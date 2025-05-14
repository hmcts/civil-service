package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

@Component
public class NotifyClaimAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotifyClaimHelper notifyClaimHelper;

    public NotifyClaimAppSolEmailDTOGenerator(OrganisationService organisationService,
                                              NotifyClaimHelper notifyClaimHelper) {
        super(organisationService);
        this.notifyClaimHelper = notifyClaimHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notifyClaimHelper.getNotifyClaimEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return NotifyClaimHelper.NOTIFY_REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(notifyClaimHelper.retrieveCustomProperties(caseData));
        return properties;
    }
}
