package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimantResponseNotAgreedRepaymentRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {
    protected ClaimantResponseNotAgreedRepaymentRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantLrTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-reject-repayment-respondent-notification-%s";
    }
}
