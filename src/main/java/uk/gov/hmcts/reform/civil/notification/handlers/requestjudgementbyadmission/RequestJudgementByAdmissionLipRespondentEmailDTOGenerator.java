package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class RequestJudgementByAdmissionLipRespondentEmailDTOGenerator extends DefendantEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "request-judgement-by-admission-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RequestJudgementByAdmissionHelper helper;

    protected RequestJudgementByAdmissionLipRespondentEmailDTOGenerator(
        NotificationsProperties notificationsProperties,
        RequestJudgementByAdmissionHelper helper) {
        this.notificationsProperties = notificationsProperties;
        this.helper = helper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyRespondentLipRequestJudgementByAdmissionNotificationTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isLipvLipOneVOne();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        return helper.addLipProperties(properties, caseData);
    }
}
