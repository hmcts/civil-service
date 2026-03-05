package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

/**
 * Abstract base class for Standard Direction Order DJ respondent solicitor one email generators.
 * Extracts common functionality to avoid code duplication.
 */
public abstract class AbstractStandardDirectionOrderDJRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    protected final StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper;

    protected AbstractStandardDirectionOrderDJRespSolOneEmailDTOGenerator(
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
        return !caseData.isRespondent1NotRepresented();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(StandardDirectionOrderDJBaseEmailDTOGenerator.LEGAL_ORG_NAME,
                       getLegalOrganizationNameForRespondent(caseData, true, organisationService));
        templateHelper.addClaimReferenceNumber(properties, caseData);
        return properties;
    }
}
