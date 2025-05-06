package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class AcknowledgeClaimSpecRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "acknowledge-claim-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public AcknowledgeClaimSpecRespSolOneEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorAcknowledgeClaimForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties,
            CaseData caseData
    ) {
        Map<String, String> updated = super.addCustomProperties(properties, caseData);
        updated.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        updated.put(RESPONSE_DEADLINE,
                formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE)
        );
        return updated;
    }
}
