package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class CaseTakenOfflineAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-taken-offline-applicant-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public CaseTakenOfflineAppSolOneEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && ONE_V_ONE.equals(getMultiPartyScenario(caseData))
                && bothPartiesRepresented(caseData)
                && caseData.getApplicant1ResponseDeadline() != null
                ? notificationsProperties.getSolicitorCaseTakenOfflineNoApplicantResponse()
                : notificationsProperties.getSolicitorCaseTakenOffline();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    private boolean bothPartiesRepresented(CaseData caseData) {
        return YES.equals(caseData.getRespondent1Represented())
                && (caseData.getApplicant1Represented() == null || YES.equals(caseData.getApplicant1Represented()));
    }
}
