package uk.gov.hmcts.reform.civil.notification.handlers.ga.gaapplicationcreated;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralApplicationCreationNotificationService;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isNotificationCriteriaSatisfied;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isUrgentApplnNotificationCriteriaSatisfied;

@Component
@RequiredArgsConstructor
public class GaApplicationCreatedRespondentsEmailGenerator extends GaEmailDTOGenerator {

    private final NotificationsProperties templates;
    private final GaForLipService gaForLipService;
    private final GeneralApplicationCreationNotificationService creationNotificationService;

    @Override
    public Boolean getShouldNotify(GaNotificationContext context) {
        var data = context.gaCaseData();
        return isNotificationCriteriaSatisfied(data)
            || (isUrgentApplnNotificationCriteriaSatisfied(data) && creationNotificationService.isFeePaid(data));
    }

    @Override
    public Set<EmailDTO> buildEmailDTOs(GaNotificationContext context, String taskId) {
        return context.gaCaseData().getGeneralAppRespondentSolicitors().stream()
            .filter(respondent -> respondent != null && respondent.getValue() != null
                && StringUtils.isNotBlank(respondent.getValue().getEmail()))
            .map(respondent -> new EmailDTO(respondent.getValue().getEmail(), getEmailTemplateId(context),
                addProperties(context), buildEmailReference(context)))
            .collect(Collectors.toSet());
    }

    @Override
    protected String getEmailAddress(GaNotificationContext context) {
        return context.gaCaseData().getGeneralAppRespondentSolicitors().getFirst().getValue().getEmail();
    }

    @Override
    protected String getEmailTemplateId(GaNotificationContext context) {
        var data = context.gaCaseData();
        if (gaForLipService.isLipResp(data)) {
            return data.isRespondentBilingual() ? templates.getLipGeneralAppRespondentEmailTemplateInWelsh()
                : templates.getLipGeneralAppRespondentEmailTemplate();
        }
        return isUrgentApplnNotificationCriteriaSatisfied(data) ? templates.getUrgentGeneralAppRespondentEmailTemplate()
            : templates.getGeneralApplicationRespondentEmailTemplate();
    }

    @Override
    protected String buildEmailReference(GaNotificationContext context) {
        return "general-application-respondent-notification-" + super.buildEmailReference(context);
    }

    @Override
    public Map<String, String> addProperties(GaNotificationContext context) {
        return creationNotificationService.addProperties(context.gaCaseData(), context.mainCaseData());
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, GaNotificationContext context) {
        return properties;
    }
}
