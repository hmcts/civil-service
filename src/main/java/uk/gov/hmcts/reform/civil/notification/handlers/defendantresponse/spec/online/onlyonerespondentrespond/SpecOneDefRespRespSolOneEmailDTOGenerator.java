package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespEmailHelper;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class SpecOneDefRespRespSolOneEmailDTOGenerator extends SpecDefRespRespSolOneEmailDTOGenerator {

    public SpecOneDefRespRespSolOneEmailDTOGenerator(SpecDefRespEmailHelper specRespEmailHelper,
                                                     OrganisationService organisationService) {
        super(specRespEmailHelper, organisationService);
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.getRespondent1DQ() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null;
    }
}
