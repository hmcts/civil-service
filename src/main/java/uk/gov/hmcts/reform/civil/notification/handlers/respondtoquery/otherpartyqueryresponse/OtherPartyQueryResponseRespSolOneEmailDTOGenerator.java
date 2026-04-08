package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Component
public class OtherPartyQueryResponseRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RespondToQueryHelper respondToQueryHelper;

    public OtherPartyQueryResponseRespSolOneEmailDTOGenerator(OrganisationService organisationService,
                                                              NotificationsProperties notificationsProperties,
                                                              RespondToQueryHelper respondToQueryHelper) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryHelper = respondToQueryHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getQueryLrPublicResponseReceived();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String orgName = getLegalOrganizationNameForRespondent(caseData, true, organisationService);
        respondToQueryHelper.addCustomProperties(properties, caseData, orgName, false);
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        if (!Boolean.TRUE.equals(super.getShouldNotify(caseData))) {
            return false;
        }

        return respondToQueryHelper.getResponseQueryContext(caseData)
            .map(context -> shouldNotifyForRoles(caseData, context.getRoles()))
            .orElse(false);
    }

    private boolean shouldNotifyForRoles(CaseData caseData, List<String> roles) {
        if (isApplicantSolicitor(roles) || isLIPClaimant(roles)) {
            return !respondToQueryHelper.isUnspecClaimNotReadyForNotification(caseData, roles);
        }
        if (isRespondentSolicitorTwo(roles)) {
            return true;
        }
        return false;
    }
}
