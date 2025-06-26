package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.claimNotified;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.takenOfflineAfterClaimNotified;

@Component
public class CaseProceedsInCasemanRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-proceeds-in-caseman-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected CaseProceedsInCasemanRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isLipvLROneVOne() ? notificationsProperties.getSolicitorCaseTakenOfflineForSpec() :
            notificationsProperties.getSolicitorCaseTakenOffline();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    public Boolean getShouldNotify(CaseData caseData) {
        return claimNotified.test(caseData) || (takenOfflineAfterClaimNotified.test(caseData) && caseData.isLipvLROneVOne()) ? Boolean.TRUE : Boolean.FALSE;
    }
}
