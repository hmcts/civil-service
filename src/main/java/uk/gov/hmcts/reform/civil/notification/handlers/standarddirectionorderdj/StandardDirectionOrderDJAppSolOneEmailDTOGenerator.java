package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StandardDirectionOrderDJAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-claimant-%s";
    private final StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper;

    public StandardDirectionOrderDJAppSolOneEmailDTOGenerator(
        OrganisationService organisationService,
        StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper
    ) {
        super(organisationService);
        this.templateHelper = templateHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return templateHelper.getEmailTemplateId(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_SDO_DJ;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return !caseData.isApplicant1NotRepresented();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(StandardDirectionOrderDJEmailDTOGeneratorBase.LEGAL_ORG_NAME,
                       getApplicantLegalOrganizationName(caseData, organisationService));
        templateHelper.addClaimReferenceNumber(properties, caseData);
        return properties;
    }
}
