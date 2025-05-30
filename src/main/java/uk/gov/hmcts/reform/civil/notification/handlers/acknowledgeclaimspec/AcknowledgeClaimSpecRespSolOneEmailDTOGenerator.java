package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.TemplateCommonPropertiesHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
public class AcknowledgeClaimSpecRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "acknowledge-claim-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public AcknowledgeClaimSpecRespSolOneEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService,
            TemplateCommonPropertiesHelper templateCommonPropertiesHelper
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.templateCommonPropertiesHelper = templateCommonPropertiesHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorAcknowledgeClaimForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String orgId = caseData.getRespondent1OrganisationPolicy()
                .getOrganisation()
                .getOrganisationID();
        Optional<Organisation> maybeOrg = organisationService.findOrganisationById(orgId);
        String orgName = maybeOrg.map(Organisation::getName)
                .orElse(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, orgName);

        String deadline = formatLocalDate(
                caseData.getRespondent1ResponseDeadline().toLocalDate(),
                DATE
        );
        properties.put(RESPONSE_DEADLINE, deadline);

        return properties;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return Boolean.TRUE;
    }
}
