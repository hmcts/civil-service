package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_TWO;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class CarmRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "mediation-update-defendant-notification-LR-%s";
    private final NotificationsProperties notificationsProperties;
    private static final String DEFENDANTS_TEXT = "'s claim against you";

    public CarmRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                           NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        if (MediationSuccessfulNotifyParties.toString().equals(taskId)) {
            return notificationsProperties.getNotifyLrDefendantSuccessfulMediation();
        }

        if (findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_TWO))) {
            return notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate();
        } else {
            return notificationsProperties.getMediationUnsuccessfulLRTemplate();
        }
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
        properties.putAll(Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                             false, organisationService),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            PARTY_NAME, caseData.getApplicant1().getPartyName() + DEFENDANTS_TEXT
        ));
        return properties;
    }
}
