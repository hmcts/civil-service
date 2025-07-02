package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.nonhmc;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

@Component
@AllArgsConstructor
public class GenerateHearingNoticeClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REF_TEMPLATE = "notification-of-hearing-lip-%s";
    private final NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
                ? notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh()
                : notificationsProperties.getHearingNotificationLipDefendantTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REF_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String hearingTime = NotificationUtils.getFormattedHearingTime(caseData.getHearingTimeHourMinute());
        String hearingDate = NotificationUtils.getFormattedHearingDate(caseData.getHearingDate());

        properties.put(HEARING_DATE, hearingDate);
        properties.put(HEARING_TIME, hearingTime);
        return properties;
    }
}
