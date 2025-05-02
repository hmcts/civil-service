package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespEmailHelper;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespRespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

public class SpecOneDefRespRespSolTwoEmailDTOGenerator extends SpecDefRespRespSolTwoEmailDTOGenerator {

    public SpecOneDefRespRespSolTwoEmailDTOGenerator(SpecDefRespEmailHelper specRespEmailHelper,
                                                     OrganisationService organisationService) {
        super(specRespEmailHelper, organisationService);
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.getRespondent2DQ() != null
            && caseData.getRespondent2ClaimResponseTypeForSpec() != null;
    }
}
