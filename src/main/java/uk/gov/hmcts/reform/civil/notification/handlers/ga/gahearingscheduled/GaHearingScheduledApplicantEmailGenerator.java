package uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService.REFERENCE_TEMPLATE_HEARING;

/**
 * The hearing notice to the applicant, which is one recipient.
 *
 * <p>Template choice and properties come from {@link HearingScheduledNotificationService}, the same
 * code the retired handler used, so migrating the BPMN moves who sends the email rather than which
 * email is sent.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GaHearingScheduledApplicantEmailGenerator extends GaEmailDTOGenerator {

    private static final String APPLICANT = "applicant";

    private final HearingScheduledNotificationService hearingScheduledNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final GaForLipService gaForLipService;

    @Override
    public Boolean getShouldNotify(GaNotificationContext context) {
        GASolicitorDetailsGAspec applicant = context.gaCaseData().getGeneralAppApplnSolicitor();
        if (applicant == null) {
            // the retired handler logged and returned rather than failing, so neither does this
            log.warn("No applicant solicitor to send the hearing notice to for Case ID: {}",
                     context.getCcdCaseReference());
            return false;
        }
        return true;
    }

    @Override
    protected String getEmailAddress(GaNotificationContext context) {
        return context.gaCaseData().getGeneralAppApplnSolicitor().getEmail();
    }

    @Override
    protected String getEmailTemplateId(GaNotificationContext context) {
        return gaForLipService.isLipApp(context.gaCaseData())
            ? hearingScheduledNotificationService.getLiPApplicantTemplates(context.gaCaseData())
            : notificationsProperties.getHearingNoticeTemplate();
    }

    @Override
    protected String buildEmailReference(GaNotificationContext context) {
        return String.format(REFERENCE_TEMPLATE_HEARING,
                             context.gaCaseData().getGeneralAppParentCaseLink().getCaseReference());
    }

    @Override
    public Map<String, String> addProperties(GaNotificationContext context) {
        // copied, because the service builds into a field it reuses between calls
        return new HashMap<>(hearingScheduledNotificationService.addPropertiesByType(
            context.gaCaseData(), context.mainCaseData(), APPLICANT));
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties,
                                                      GaNotificationContext context) {
        return properties;
    }
}
