package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
public class AcknowledgeClaimSpecAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "acknowledge-claim-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public AcknowledgeClaimSpecAppSolOneEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getApplicantSolicitorAcknowledgeClaimForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> props,
            CaseData caseData
    ) {
        Map<String, String> updated = super.addCustomProperties(props, caseData);
        updated.put(ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE));
        return updated;
    }
}
