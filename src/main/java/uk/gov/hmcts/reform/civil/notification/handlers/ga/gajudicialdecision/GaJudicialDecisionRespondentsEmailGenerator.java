package uk.gov.hmcts.reform.civil.notification.handlers.ga.gajudicialdecision;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GaJudicialDecisionRespondentsEmailGenerator extends GaEmailDTOGenerator {

    private final GaJudicialDecisionEmailHelper helper;

    @Override
    public Boolean getShouldNotify(GaNotificationContext context) {
        return helper.shouldNotify(context.gaCaseData(), false);
    }

    @Override
    public Set<EmailDTO> buildEmailDTOs(GaNotificationContext context, String taskId) {
        Set<EmailDTO> emails = new HashSet<>();
        context.gaCaseData().getGeneralAppRespondentSolicitors().forEach(respondent -> {
            EmailDTO email = new EmailDTO();
            email.setTargetEmail(respondent.getValue().getEmail());
            email.setEmailTemplate(getEmailTemplateId(context));
            email.setReference(buildEmailReference(context));
            email.setParameters(addProperties(context));
            emails.add(email);
        });
        return emails;
    }

    @Override
    protected String getEmailAddress(GaNotificationContext context) {
        return context.gaCaseData().getGeneralAppRespondentSolicitors().getFirst().getValue().getEmail();
    }

    @Override
    protected String getEmailTemplateId(GaNotificationContext context) {
        return helper.template(context.gaCaseData(), false);
    }

    @Override
    protected String buildEmailReference(GaNotificationContext context) {
        return String.format(GaJudicialDecisionEmailHelper.REFERENCE_TEMPLATE,
                             context.gaCaseData().getGeneralAppParentCaseLink().getCaseReference());
    }

    @Override
    public Map<String, String> addProperties(GaNotificationContext context) {
        return helper.properties(context, false);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, GaNotificationContext context) {
        return properties;
    }
}
