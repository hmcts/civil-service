package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@Component
public class CarmDisabledRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "mediation-update-defendant-two-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public CarmDisabledRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                   NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        if (MediationSuccessfulNotifyParties.toString().equals(taskId)) {
            return notificationsProperties.getNotifyLrDefendantSuccessfulMediation();
        }
        return notificationsProperties.getMediationUnsuccessfulLRTemplate();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return getEmailTemplateId(caseData, null);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        return buildDefendantTwoNotificationProperties(properties, caseData);
    }
}
