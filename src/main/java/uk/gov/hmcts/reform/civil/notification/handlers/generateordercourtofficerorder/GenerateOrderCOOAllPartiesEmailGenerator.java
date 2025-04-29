package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import lombok.Setter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Setter
@Component
public class GenerateOrderCOOAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public GenerateOrderCOOAllPartiesEmailGenerator(
        GenerateOrderCOOAppSolEmailDTOGenerator appSolEmailDTOGenerator,
        GenerateOrderCOOResp1EmailDTOGenerator resp1EmailDTOGenerator,
        GenerateOrderCOOResp2EmailDTOGenerator resp2EmailDTOGenerator,
        GenerateOrderCOOClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        GenerateOrderCOODefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(appSolEmailDTOGenerator,
              resp1EmailDTOGenerator,
              resp2EmailDTOGenerator,
              claimantEmailDTOGenerator,
              defendantEmailDTOGenerator);
    }
}
