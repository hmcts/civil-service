package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isTwoVOne;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_TWO;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class CarmRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "mediation-update-defendant-notification-LR-%s";
    private final NotificationsProperties notificationsProperties;
    private static final String DEFENDANTS_TEXT = "'s claim against you";

    public CarmRespSolOneEmailDTOGenerator(OrganisationService organisationService,
                                           NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        if (MediationSuccessfulNotifyParties.toString().equals(taskId)) {
            if (isTwoVOne(caseData)) {
                return notificationsProperties.getNotifyTwoVOneDefendantSuccessfulMediation();
            }
            return notificationsProperties.getNotifyLrDefendantSuccessfulMediation();
        }

        if (findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_ONE))
                || (isOneVTwoLegalRep(caseData)
                && findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_TWO)))) {
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
        String partyName = caseData.getApplicant1().getPartyName();

        if (isTwoVOne(caseData)) {
            partyName = String.format("%s and %s", partyName, caseData.getApplicant2().getPartyName());
            properties.putAll(Map.of(
                    CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                            true, organisationService),
                    CLAIMANT_NAME_ONE, caseData.getApplicant1().getPartyName(),
                    CLAIMANT_NAME_TWO, caseData.getApplicant2().getPartyName(),
                    PARTY_NAME, partyName + DEFENDANTS_TEXT
            ));
        } else {
            properties.putAll(Map.of(
                    CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                            true, organisationService),
                    CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
                    PARTY_NAME, partyName + DEFENDANTS_TEXT
            ));
        }
        return properties;
    }
}
