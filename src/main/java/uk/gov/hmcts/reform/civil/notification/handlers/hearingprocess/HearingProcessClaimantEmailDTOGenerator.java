package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

public class HearingProcessClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_CLAIMANT_TEMPLATE = "notification-of-hearing-lip-%s";

    private final NotificationsProperties notificationsProperties;

    protected HearingProcessClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual() ? notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh()
            : notificationsProperties.getHearingNotificationLipDefendantTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_CLAIMANT_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(HEARING_DATE, NotificationUtils.getFormattedHearingDate(caseData.getHearingDate()));
        properties.put(HEARING_TIME, NotificationUtils.getFormattedHearingTime(caseData.getHearingTimeHourMinute()));
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getApplicant1().getPartyName());
        return properties;
    }
}
