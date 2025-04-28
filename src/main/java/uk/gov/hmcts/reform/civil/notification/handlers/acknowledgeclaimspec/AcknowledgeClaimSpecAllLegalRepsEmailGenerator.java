package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;

@Component
public class AcknowledgeClaimSpecAllLegalRepsEmailGenerator
        extends AllLegalRepsEmailGenerator {

    public AcknowledgeClaimSpecAllLegalRepsEmailGenerator(
            AcknowledgeClaimSpecAppSolOneEmailDTOGenerator appSolOne,
            AcknowledgeClaimSpecRespSolOneEmailDTOGenerator respSolOne,
            AcknowledgeClaimSpecRespSolTwoEmailDTOGenerator respSolTwo
    ) {
        super(appSolOne, respSolOne, respSolTwo);
    }

    @Override
    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return true;
    }
}
