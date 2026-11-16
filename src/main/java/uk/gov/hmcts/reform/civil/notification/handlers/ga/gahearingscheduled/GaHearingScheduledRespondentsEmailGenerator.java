package uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService.REFERENCE_TEMPLATE_HEARING;

/**
 * The hearing notice to the respondents, which is a collection rather than a fixed party.
 *
 * <p>General Application respondents are a list, so this overrides
 * {@link GaEmailDTOGenerator#buildEmailDTOs} and returns one {@link EmailDTO} per solicitor. When
 * the list is empty and the respondent is a LiP, it falls back to the single LiP email, which is
 * what the retired handler did.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GaHearingScheduledRespondentsEmailGenerator extends GaEmailDTOGenerator {

    private static final String RESPONDENT = "respondent";

    private final HearingScheduledNotificationService hearingScheduledNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final GaForLipService gaForLipService;

    @Override
    public Boolean getShouldNotify(GaNotificationContext context) {
        GeneralApplicationCaseData caseData = context.gaCaseData();
        List<Element<GASolicitorDetailsGAspec>> respondents = caseData.getGeneralAppRespondentSolicitors();

        if (respondents != null && !respondents.isEmpty()) {
            return true;
        }
        if (gaForLipService.isLipResp(caseData)
            && lipRespondentEmail(context) != null) {
            return true;
        }
        log.warn("No respondent solicitor to send the hearing notice to for Case ID: {}",
                 context.getCcdCaseReference());
        return false;
    }

    @Override
    public Set<EmailDTO> buildEmailDTOs(GaNotificationContext context, String taskId) {
        GeneralApplicationCaseData caseData = context.gaCaseData();
        List<Element<GASolicitorDetailsGAspec>> respondents = caseData.getGeneralAppRespondentSolicitors();

        Set<EmailDTO> emails = new HashSet<>();
        if (respondents == null || respondents.isEmpty()) {
            emails.add(email(context, lipRespondentEmail(context)));
            return emails;
        }

        respondents.stream()
            .filter(r -> r != null && r.getValue() != null && r.getValue().getEmail() != null)
            .forEach(r -> emails.add(email(context, r.getValue().getEmail())));
        return emails;
    }

    private EmailDTO email(GaNotificationContext context, String recipient) {
        EmailDTO dto = new EmailDTO();
        dto.setTargetEmail(recipient);
        dto.setEmailTemplate(getEmailTemplateId(context));
        dto.setParameters(addProperties(context));
        dto.setReference(buildEmailReference(context));
        return dto;
    }

    private String lipRespondentEmail(GaNotificationContext context) {
        return hearingScheduledNotificationService.getLipRespondentEmail(
            context.mainCaseData(), context.gaCaseData());
    }

    @Override
    protected String getEmailAddress(GaNotificationContext context) {
        // unused: buildEmailDTOs sets the recipient per solicitor rather than one address
        return lipRespondentEmail(context);
    }

    @Override
    protected String getEmailTemplateId(GaNotificationContext context) {
        return gaForLipService.isLipResp(context.gaCaseData())
            ? hearingScheduledNotificationService.getLiPRespondentTemplate(context.gaCaseData())
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
            context.gaCaseData(), context.mainCaseData(), RESPONDENT));
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties,
                                                      GaNotificationContext context) {
        return properties;
    }
}
