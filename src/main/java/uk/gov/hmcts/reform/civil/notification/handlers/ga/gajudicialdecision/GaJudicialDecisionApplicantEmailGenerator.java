package uk.gov.hmcts.reform.civil.notification.handlers.ga.gajudicialdecision;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GaJudicialDecisionApplicantEmailGenerator extends GaEmailDTOGenerator {

    private final GaJudicialDecisionEmailHelper helper;

    @Override
    public Boolean getShouldNotify(GaNotificationContext context) {
        return helper.shouldNotify(context.gaCaseData(), true);
    }

    @Override
    protected String getEmailAddress(GaNotificationContext context) {
        return context.gaCaseData().getGeneralAppApplnSolicitor().getEmail();
    }

    @Override
    protected String getEmailTemplateId(GaNotificationContext context) {
        return helper.template(context.gaCaseData(), true);
    }

    @Override
    protected String buildEmailReference(GaNotificationContext context) {
        return String.format(GaJudicialDecisionEmailHelper.REFERENCE_TEMPLATE,
                             context.gaCaseData().getGeneralAppParentCaseLink().getCaseReference());
    }

    @Override
    public Map<String, String> addProperties(GaNotificationContext context) {
        return helper.properties(context, true);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, GaNotificationContext context) {
        return properties;
    }
}
