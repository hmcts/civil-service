package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

@Component
public class AcknowledgeClaimUnspecRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    protected static final String REF_SOL_ONE_REF_TEMPLATE = "acknowledge-claim-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final AcknowledgeClaimUnspecHelper acknowledgeClaimUnspecHelper;

    public AcknowledgeClaimUnspecRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                             OrganisationService organisationService,
                                                             AcknowledgeClaimUnspecHelper acknowledgeClaimUnspecHelper) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.acknowledgeClaimUnspecHelper = acknowledgeClaimUnspecHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorAcknowledgeClaim();
    }

    @Override
    protected String getReferenceTemplate() {
        return REF_SOL_ONE_REF_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        return acknowledgeClaimUnspecHelper.addTemplateProperties(properties, caseData);
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return acknowledgeClaimUnspecHelper.isRespondentOneAcknowledged(caseData);
    }
}
