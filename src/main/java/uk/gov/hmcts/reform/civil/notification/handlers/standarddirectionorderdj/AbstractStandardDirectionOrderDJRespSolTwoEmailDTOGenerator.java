package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

/**
 * Abstract base class for Standard Direction Order DJ respondent solicitor two email generators.
 * Extracts common functionality to avoid code duplication.
 */
public abstract class AbstractStandardDirectionOrderDJRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    protected final StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper;

    protected AbstractStandardDirectionOrderDJRespSolTwoEmailDTOGenerator(
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
        return YesOrNo.YES.equals(caseData.getAddRespondent2())
            && isOneVTwoTwoLegalRep(caseData)
            && !YesOrNo.YES.equals(caseData.getRespondent2Represented());
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(StandardDirectionOrderDJBaseEmailDTOGenerator.LEGAL_ORG_NAME,
                       getLegalOrganizationNameForRespondent(caseData, false, organisationService));
        templateHelper.addClaimReferenceNumber(properties, caseData);
        return properties;
    }
}
