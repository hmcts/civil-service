package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Component
public class OtherPartyQueryResponseClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RespondToQueryHelper respondToQueryHelper;

    public OtherPartyQueryResponseClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                           RespondToQueryHelper respondToQueryHelper) {
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryHelper = respondToQueryHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getQueryLipWelshPublicResponseReceived()
            : notificationsProperties.getQueryLipPublicResponseReceived();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        return respondToQueryHelper.addLipOtherPartyProperties(
            properties,
            caseData,
            caseData.getApplicant1().getPartyName()
        );
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return Boolean.TRUE.equals(super.getShouldNotify(caseData))
            && respondToQueryHelper.getResponseQueryContext(caseData)
            .map(context -> isRespondentSolicitorOne(context.getRoles())
                || isRespondentSolicitorTwo(context.getRoles())
                || isLIPDefendant(context.getRoles()))
            .orElse(false);
    }
}
