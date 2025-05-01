package uk.gov.hmcts.reform.civil.notification.handlers.defresponse.unspec.otherresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecasetransferoffline.DefRespCaseOfflineHelper.caseOfflineNotificationProperties;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

@Component
public class DefRespCaseOfflineRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public DefRespCaseOfflineRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        if (is1v1Or2v1Case(caseData)) {
            return notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
        } else {
            return notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
        }
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = true;
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                        isRespondent1, organisationService));
        properties.putAll(caseOfflineNotificationProperties(caseData));
        return properties;
    }
}
