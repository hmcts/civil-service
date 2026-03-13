package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;

@Component
public class RespondToQueryDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RespondToQueryHelper respondToQueryHelper;

    public RespondToQueryDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                    RespondToQueryHelper respondToQueryHelper) {
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryHelper = respondToQueryHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getQueryLipWelshPublicResponseReceived()
            : notificationsProperties.getQueryLipPublicResponseReceived();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        respondToQueryHelper.addLipOtherPartyProperties(properties, caseData, caseData.getRespondent1().getPartyName());
        respondToQueryHelper.addQueryDateProperty(properties, caseData);
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return Boolean.TRUE.equals(super.getShouldNotify(caseData))
            && respondToQueryHelper.getResponseQueryContext(caseData)
            .map(context -> isLIPDefendant(context.getRoles()))
            .orElse(false);
    }
}
