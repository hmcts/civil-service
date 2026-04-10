package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class RespondToQueryAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public RespondToQueryAllPartiesEmailGenerator(
        RespondToQueryAppSolEmailDTOGenerator appSolEmailDTOGenerator,
        RespondToQueryRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        RespondToQueryRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
        RespondToQueryClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        RespondToQueryDefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(List.of(
            appSolEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator,
            claimantEmailDTOGenerator,
            defendantEmailDTOGenerator
        ));
    }
}
