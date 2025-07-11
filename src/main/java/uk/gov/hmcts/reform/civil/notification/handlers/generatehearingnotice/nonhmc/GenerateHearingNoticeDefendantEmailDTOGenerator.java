package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.nonhmc;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

@Component
@AllArgsConstructor
public class GenerateHearingNoticeDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_HEARING_LIP = "notification-of-hearing-lip-%s";
    private final NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
                ? notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh()
                : notificationsProperties.getHearingNotificationLipDefendantTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_HEARING_LIP;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(HEARING_DATE, NotificationUtils.getFormattedHearingDate(caseData.getHearingDate()));
        properties.put(HEARING_TIME, NotificationUtils.getFormattedHearingTime(caseData.getHearingTimeHourMinute()));
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getRespondent1().getPartyName());
        return properties;
    }
}
