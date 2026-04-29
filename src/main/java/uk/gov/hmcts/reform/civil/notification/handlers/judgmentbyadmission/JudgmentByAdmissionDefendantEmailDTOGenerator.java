package uk.gov.hmcts.reform.civil.notification.handlers.judgmentbyadmission;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import org.springframework.stereotype.Component;

@Component
public class JudgmentByAdmissionDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_DEFENDANT_TEMPLATE = "defendant-judgment-by-admission-%s";

    private final NotificationsProperties notificationsProperties;

    protected JudgmentByAdmissionDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantLIPJudgmentByAdmissionTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_DEFENDANT_TEMPLATE;
    }
}
