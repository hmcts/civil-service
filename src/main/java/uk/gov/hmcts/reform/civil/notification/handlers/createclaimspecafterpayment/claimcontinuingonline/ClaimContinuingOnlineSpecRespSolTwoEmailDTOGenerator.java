package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        if (caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
        return caseData.getRespondentSolicitor2EmailAddress();
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return YesOrNo.YES.equals(caseData.getAddRespondent2());
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        if (caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
            properties.put(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                    true, organisationService));
        } else {
            properties.put(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                    false, organisationService));
        }
        properties.put(CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                formatLocalDate(caseData.getRespondent2ResponseDeadline().toLocalDate(), DATE)
        );
        return properties;
    }
}
