package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class RequestJudgementByAdmissionAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    protected RequestJudgementByAdmissionAppSolOneEmailDTOGenerator(
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
        return "request-judgement-by-admission-applicant-notification-%s";
    }
}