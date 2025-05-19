package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_TWO;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class CarmAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "mediation-update-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public CarmAppSolOneEmailDTOGenerator(OrganisationService organisationService,
                                          NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return getEmailTemplateId(caseData, null);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        if (MediationSuccessfulNotifyParties.toString().equals(taskId)) {
            if (isOneVTwoTwoLegalRep(caseData) || isOneVTwoLegalRep(caseData)) {
                return
                    notificationsProperties.getNotifyOneVTwoClaimantSuccessfulMediation();
            }
            return notificationsProperties.getNotifyLrClaimantSuccessfulMediation();
        }

        if (findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_CLAIMANT_TWO))) {
            return notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate();
        } else {
            return notificationsProperties.getMediationUnsuccessfulLRTemplate();
        }
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String partyName = "your claim against " + caseData.getRespondent1().getPartyName();
        if (null != caseData.getRespondent2()) {
            partyName = String.format("%s and %s", partyName, caseData.getRespondent2().getPartyName());
        }
        properties.put(PARTY_NAME, partyName);
        if (isOneVTwoTwoLegalRep(caseData) || isOneVTwoLegalRep(caseData)) {
            properties.putAll(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService),
                DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()),
                DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2())
            ));
        } else {
            properties.putAll(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService),
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            ));
        }
        return properties;
    }
}
