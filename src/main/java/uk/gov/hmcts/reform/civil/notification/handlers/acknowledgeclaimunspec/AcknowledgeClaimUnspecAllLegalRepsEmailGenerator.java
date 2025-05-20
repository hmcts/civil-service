package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class AcknowledgeClaimUnspecAllLegalRepsEmailGenerator extends AllPartiesEmailGenerator {

    public AcknowledgeClaimUnspecAllLegalRepsEmailGenerator(
        AcknowledgeClaimUnspecAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
        AcknowledgeClaimUnspecRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
        AcknowledgeClaimUnspecRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator
    ) {
        super(List.of(appSolOneEmailGenerator,
              respSolOneEmailGenerator,
              respSolTwoEmailGenerator));
    }

}
