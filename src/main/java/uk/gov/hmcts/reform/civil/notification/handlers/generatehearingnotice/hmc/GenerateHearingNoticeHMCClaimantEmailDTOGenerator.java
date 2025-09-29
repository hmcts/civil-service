package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.hmc;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@AllArgsConstructor
public class GenerateHearingNoticeHMCClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REF_TEMPLATE = "notification-of-hearing-lip-%s";
    private final NotificationsProperties notificationsProperties;
    private final HearingNoticeCamundaService camundaService;

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
        LocalDateTime hearingStartDateTime = camundaService
            .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingStartDateTime();

        String hearingTime = NotificationUtils.getFormattedHearingTime(hearingStartDateTime.toLocalTime().toString());
        String hearingDate = NotificationUtils.getFormattedHearingDate(caseData.getHearingDate());

        properties.put(HEARING_DATE, hearingDate);
        properties.put(HEARING_TIME, hearingTime);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getApplicant1().getPartyName());
        return properties;
    }
}
