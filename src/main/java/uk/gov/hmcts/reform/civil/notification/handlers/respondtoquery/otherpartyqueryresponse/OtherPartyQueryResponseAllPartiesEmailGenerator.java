package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class OtherPartyQueryResponseAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public OtherPartyQueryResponseAllPartiesEmailGenerator(
        OtherPartyQueryResponseAppSolEmailDTOGenerator appSolEmailDTOGenerator,
        OtherPartyQueryResponseRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        OtherPartyQueryResponseRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
        OtherPartyQueryResponseClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        OtherPartyQueryResponseDefendantEmailDTOGenerator defendantEmailDTOGenerator
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
