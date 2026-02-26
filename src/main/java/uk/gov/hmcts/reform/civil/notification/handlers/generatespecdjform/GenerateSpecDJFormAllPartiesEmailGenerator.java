package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class GenerateSpecDJFormAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public GenerateSpecDJFormAllPartiesEmailGenerator(
        GenerateSpecDJFormReceivedAppSolEmailDTOGenerator appSolReceivedEmailDTOGenerator,
        GenerateSpecDJFormReceivedRespSolOneEmailDTOGenerator respSolOneReceivedEmailDTOGenerator,
        GenerateSpecDJFormReceivedRespSolTwoEmailDTOGenerator respSolTwoEmailReceivedDTOGenerator,
        GenerateSpecDJFormRecievedClaimantEmailDTOGenerator claimantEmailReceivedDTOGenerator,
        GenerateSpecDJFormReceivedDefendantEmailDTOGenerator defendantEmaiReceivedlDTOGenerator,
        GenerateSpecDJFormRequestedAppSolEmailDTOGenerator appSolEmailRequestedDTOGenerator,
        GenerateSpecDJFormRequestedRespSolOneEmailDTOGenerator respSolRequestedOneEmailDTOGenerator,
        GenerateSpecDJFormRequestedRespSolTwoEmailDTOGenerator respSolRequestedTwoEmailDTOGenerator,
        GenerateSpecDJFormRequestedClaimantEmailDTOGenerator claimantEmailRequestedDTOGenerator,
        GenerateSpecDJFormRequestedDefendantEmailDTOGenerator defendantEmailRequestedDTOGenerator

    ) {
        super(List.of(
            appSolReceivedEmailDTOGenerator,
            respSolOneReceivedEmailDTOGenerator,
            respSolTwoEmailReceivedDTOGenerator,
            claimantEmailReceivedDTOGenerator,
            defendantEmaiReceivedlDTOGenerator,
            appSolEmailRequestedDTOGenerator,
            respSolRequestedOneEmailDTOGenerator,
            respSolRequestedTwoEmailDTOGenerator,
            claimantEmailRequestedDTOGenerator,
            defendantEmailRequestedDTOGenerator
        ));
    }
}
