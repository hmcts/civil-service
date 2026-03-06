package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

/**
 * Abstract base class for Standard Direction Order DJ applicant solicitor email generators.
 * Extracts common functionality to avoid code duplication.
 */
public abstract class AbstractStandardDirectionOrderDJAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected final StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper;

    protected AbstractStandardDirectionOrderDJAppSolEmailDTOGenerator(
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
    public Boolean getShouldNotify(CaseData caseData) {
        return !caseData.isApplicant1NotRepresented();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(StandardDirectionOrderDJBaseEmailDTOGenerator.LEGAL_ORG_NAME,
                       getApplicantLegalOrganizationName(caseData, organisationService));
        templateHelper.addClaimReferenceNumber(properties, caseData);
        return properties;
    }
}
