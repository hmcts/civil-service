package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class CaseProceedsInCasemanRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-proceeds-in-caseman-respondent-notification-%s";

    protected CaseProceedsInCasemanRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
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
}
