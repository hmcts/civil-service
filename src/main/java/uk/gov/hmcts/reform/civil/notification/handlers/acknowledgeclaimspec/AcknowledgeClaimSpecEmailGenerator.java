package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class AcknowledgeClaimSpecEmailGenerator extends AllPartiesEmailGenerator {

    public AcknowledgeClaimSpecEmailGenerator(
            AcknowledgeClaimSpecAppSolOneEmailDTOGenerator appSolOne,
            AcknowledgeClaimSpecRespSolOneEmailDTOGenerator respSolOne,
            AcknowledgeClaimSpecRespSolTwoEmailDTOGenerator respSolTwo
    ) {
        super(List.of(appSolOne, respSolOne, respSolTwo));
    }
}