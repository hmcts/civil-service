package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class COOAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public COOAllPartiesEmailGenerator(
        COOAppSolEmailDTOGenerator appSolEmailDTOGenerator,
        COOResp1EmailDTOGenerator resp1EmailDTOGenerator,
        COOResp2EmailDTOGenerator resp2EmailDTOGenerator,
        COOClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        COODefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(appSolEmailDTOGenerator,
              resp1EmailDTOGenerator,
              resp2EmailDTOGenerator,
              claimantEmailDTOGenerator,
              defendantEmailDTOGenerator);
    }
}
