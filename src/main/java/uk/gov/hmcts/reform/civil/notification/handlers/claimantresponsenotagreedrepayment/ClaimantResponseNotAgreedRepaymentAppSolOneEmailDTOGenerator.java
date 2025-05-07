package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimantResponseNotAgreedRepaymentAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected ClaimantResponseNotAgreedRepaymentAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimantLrTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-reject-repayment-respondent-notification-%s";
    }
}
