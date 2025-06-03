package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimantResponseAgreedRepaymentRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    protected ClaimantResponseAgreedRepaymentRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorCcjNotificationTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-agree-repayment-respondent-notification-%s";
    }
}
