package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class DiscontinueClaimPartiesAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public DiscontinueClaimPartiesAllPartiesEmailGenerator(
        DiscontinueClaimPartiesAppSolOneEmailDTOGenerator discontinueClaimPartiesAppSolOneEmailDTOGenerator,
        DiscontinueClaimPartiesRespSolOneEmailDTOGenerator discontinueClaimPartiesRespSolOneEmailDTOGenerator,
        DiscontinueClaimPartiesRespSolTwoEmailDTOGenerator discontinueClaimPartiesRespSolTwoEmailDTOGenerator
    ) {
        super(List.of(discontinueClaimPartiesAppSolOneEmailDTOGenerator,
                      discontinueClaimPartiesRespSolOneEmailDTOGenerator,
                      discontinueClaimPartiesRespSolTwoEmailDTOGenerator));
    }

}
