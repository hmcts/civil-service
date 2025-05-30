package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimantResponseConfirmsToProceedAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final ClaimantResponseConfirmsToProceedEmailHelper claimantResponseConfirmsToProceedEmailHelper;

    protected ClaimantResponseConfirmsToProceedAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService,
                                                                          ClaimantResponseConfirmsToProceedEmailHelper claimantResponseConfirmsToProceedEmailHelper) {
        super(notificationsProperties, organisationService);
        this.claimantResponseConfirmsToProceedEmailHelper = claimantResponseConfirmsToProceedEmailHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return claimantResponseConfirmsToProceedEmailHelper.getTemplate(caseData, true);
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-to-proceed-respondent-notification-%s";
    }
}
