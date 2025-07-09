package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingTime;

@Component
public class HearingProcessDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_DEFENDANT_TEMPLATE = "notification-of-hearing-lip-%s";

    private final NotificationsProperties notificationsProperties;

    protected HearingProcessDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh()
            : notificationsProperties.getHearingNotificationLipDefendantTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_DEFENDANT_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(HEARING_DATE, getFormattedHearingDate(caseData.getHearingDate()));
        properties.put(HEARING_TIME, getFormattedHearingTime(caseData.getHearingTimeHourMinute()));
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getRespondent1().getPartyName());
        return properties;
    }
}
