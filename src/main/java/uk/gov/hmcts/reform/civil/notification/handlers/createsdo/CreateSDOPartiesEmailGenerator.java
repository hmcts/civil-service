package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import java.util.List;

@Component
public class CreateSDOPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public CreateSDOPartiesEmailGenerator(CreateSDOAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
                                          CreateSDORespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
                                          CreateSDORespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
                                          CreateSDOClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                          CreateSDODefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(appSolOneEmailDTOGenerator, respSolOneEmailDTOGenerator, respSolTwoEmailDTOGenerator, claimantEmailDTOGenerator, defendantEmailDTOGenerator));
    }
}
