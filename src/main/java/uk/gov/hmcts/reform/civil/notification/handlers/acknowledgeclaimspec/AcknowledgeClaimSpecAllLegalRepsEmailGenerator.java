package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class AcknowledgeClaimSpecAllLegalRepsEmailGenerator extends AllPartiesEmailGenerator {

    public AcknowledgeClaimSpecAllLegalRepsEmailGenerator(
            AcknowledgeClaimSpecAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
            AcknowledgeClaimSpecRespSolOneEmailDTOGenerator respSolOneEmailGenerator
    ) {
        super(List.of(appSolOneEmailGenerator, respSolOneEmailGenerator));
    }
}
