package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class AcknowledgeClaimSpecEmailGenerator extends AllPartiesEmailGenerator {

    public AcknowledgeClaimSpecEmailGenerator(
            AcknowledgeClaimSpecAppSolOneEmailDTOGenerator appSolOne,
            AcknowledgeClaimSpecRespSolOneEmailDTOGenerator respSolOne,
            AcknowledgeClaimSpecRespSolTwoEmailDTOGenerator respSolTwo
    ) {
        super(appSolOne, respSolOne, respSolTwo, null, null);
    }
}