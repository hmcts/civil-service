package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

public class NotifyClaimDetailsAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    public NotifyClaimDetailsAppSolEmailDTOGenerator(OrganisationService organisationService,
                                              NotifyClaimDetailsHelper notifyClaimDetailsHelper) {
        super(organisationService);
        this.notifyClaimDetailsHelper = notifyClaimDetailsHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notifyClaimDetailsHelper.getEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return NotifyClaimDetailsHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(notifyClaimDetailsHelper.getCustomProperties(caseData));
        return properties;
    }
}
