package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class RequestJudgementByAdmissionRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    protected RequestJudgementByAdmissionRespSolOneEmailDTOGenerator(
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorCcjNotificationTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "request-judgement-by-admission-respondent-notification-%s";
    }
}
