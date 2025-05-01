package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
public class CaseProceedsInCasemanClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-proceeds-in-caseman-applicant-notification-%s";

    protected CaseProceedsInCasemanClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        super(notificationsProperties);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual() ? notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate() :
            notificationsProperties.getClaimantLipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
